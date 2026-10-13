package agape.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.serialization.json.Json

object ChapterGuideStore {
    private var db: SQLiteDatabase? = null
    private val cache = LinkedHashMap<String, ChapterGuide>()
    private val lock = Any()
    private var explanations = emptyMap<String, BilingualExplanation>()
    private val json = Json { ignoreUnknownKeys = true }

    fun open(context: Context) {
        synchronized(lock) {
            db?.close()
            cache.clear()
            explanations = context.assets.open("json/chapter-explanations.json").bufferedReader().use {
                json.decodeFromString<List<BilingualExplanation>>(it.readText()).associateBy { entry -> "${entry.bookId}:${entry.chapter}" }
            }
            db = SQLiteDatabase.openDatabase(
                AssetPack.databaseFile(context, "guides.sqlite").path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
    }

    fun guide(bookId: String, chapter: Int, locale: AppLocale = AppLocale.en): ChapterGuide? {
        synchronized(lock) {
            val key = "${locale.name}:$bookId:$chapter"
            cache[key]?.let { return it }
            val loaded = loadGuide(bookId, chapter, locale) ?: return null
            if (cache.size > 64) cache.clear()
            cache[key] = loaded
            return loaded
        }
    }

    fun search(query: String, limit: Int = 6, locale: AppLocale = AppLocale.en): List<GuideSearchHit> {
        val tokens = BibleDatabase.fold(query).split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.length >= 3 }
        if (tokens.isEmpty() || limit <= 0) return emptyList()
        synchronized(lock) {
            val found = mutableListOf<GuideSearchHit>()
            explanations.values.forEach { entry ->
                val detail = if (locale == AppLocale.nl) entry.nl else entry.en
                val text = BibleDatabase.fold(detail.title + " " + detail.summary + " " + detail.context + " " + detail.sections.joinToString(" ") { it.body })
                if (tokens.all { it in text }) found += GuideSearchHit(entry.bookId, entry.chapter, detail.title, detail.summary)
            }
            val handle = db ?: return found.take(limit)
            val fields = if (locale == AppLocale.nl) "title_nl || ' ' || summary_nl" else "title || ' ' || summary"
            // Search actual commentary, not repeated template text that is now a reading aid.
            val where = tokens.joinToString(" AND ") { "LOWER($fields) LIKE ?" }
            val args = (tokens.map { "%$it%" } + (limit * 3).toString()).toTypedArray()
            handle.rawQuery("SELECT book_id, chapter FROM guides WHERE summary NOT LIKE '%The names%' AND $where LIMIT ?", args).use { cursor ->
                while (cursor.moveToNext()) {
                    val bookId = cursor.getString(0)
                    val chapter = cursor.getInt(1)
                    if (ContentCatalog.book(bookId)?.scope == CanonScope.hidden) continue
                    val effective = guide(bookId, chapter, locale) ?: continue
                    if (!effective.readingAid) found += GuideSearchHit(bookId, chapter, effective.title, effective.summary)
                }
            }
            return found.distinctBy { it.id }.take(limit)
        }
    }

    private fun loadGuide(bookId: String, chapter: Int, locale: AppLocale): ChapterGuide? {
        val handle = db ?: return null
        val dutch = locale == AppLocale.nl
        val sql = if (dutch) {
            """
            SELECT COALESCE(NULLIF(title_nl, ''), title),
                   COALESCE(NULLIF(summary_nl, ''), summary),
                   COALESCE(NULLIF(purpose_nl, ''), purpose),
                   COALESCE(NULLIF(context_nl, ''), context),
                   COALESCE(NULLIF(themes_nl, ''), themes),
                   COALESCE(NULLIF(related_nl, ''), related),
                   COALESCE(NULLIF(interpretations_nl, ''), interpretations),
                   deuterocanonical, summary
            FROM guides WHERE book_id = ? AND chapter = ?
            """.trimIndent()
        } else {
            """
            SELECT title, summary, purpose, context, themes, related, interpretations, deuterocanonical, summary
            FROM guides WHERE book_id = ? AND chapter = ?
            """.trimIndent()
        }
        handle.rawQuery(sql, arrayOf(bookId, chapter.toString())).use { c ->
            if (!c.moveToFirst()) return null
            val original = ChapterGuide(
                bookId = bookId,
                chapter = chapter,
                title = c.getString(0).orEmpty(),
                summary = c.getString(1).orEmpty(),
                purpose = c.getString(2).orEmpty(),
                context = c.getString(3).orEmpty(),
                themes = decodeStrings(c.getString(4).orEmpty()),
                related = decodeRelated(c.getString(5).orEmpty()),
                fathers = loadFathers(bookId, chapter, locale),
                interpretations = c.getString(6).orEmpty(),
                deuterocanonical = c.getInt(7) != 0
            )
            val book = ContentCatalog.book(bookId) ?: return original
            val supplement = explanations["$bookId:$chapter"]?.let { if (dutch) it.nl else it.en }
            val generic = c.getString(8).orEmpty().contains("The names")
            return ReadingSupport.apply(original, book, locale, supplement, generic)
        }
    }

    private fun loadFathers(bookId: String, chapter: Int, locale: AppLocale): List<ChurchFatherCommentary> {
        val handle = db ?: return emptyList()
        val dutch = locale == AppLocale.nl
        val sql = if (dutch) {
            """
            SELECT father, description, work, date,
                   COALESCE(NULLIF(commentary_nl, ''), commentary),
                   COALESCE(NULLIF(why_nl, ''), why),
                   source, verses, paraphrase
            FROM fathers WHERE book_id = ? AND chapter = ? ORDER BY sort
            """.trimIndent()
        } else {
            """
            SELECT father, description, work, date, commentary, why, source, verses, paraphrase
            FROM fathers WHERE book_id = ? AND chapter = ? ORDER BY sort
            """.trimIndent()
        }
        val rows = mutableListOf<ChurchFatherCommentary>()
        handle.rawQuery(sql, arrayOf(bookId, chapter.toString())).use { c ->
            while (c.moveToNext()) {
                rows += ChurchFatherCommentary(
                    father = c.getString(0).orEmpty(),
                    fatherDescription = c.getString(1).orEmpty(),
                    workTitle = c.getString(2).orEmpty(),
                    approximateDate = c.getString(3).orEmpty(),
                    commentary = c.getString(4).orEmpty(),
                    whyItMatters = c.getString(5).orEmpty(),
                    sourceReference = c.getString(6).orEmpty(),
                    relatedVerses = c.getString(7).orEmpty(),
                    paraphrase = c.getInt(8) != 0
                )
            }
        }
        return rows
    }

    private fun decodeStrings(raw: String): List<String> = try {
        json.decodeFromString<List<String>>(raw).filter { it.isNotEmpty() }
    } catch (_: Exception) {
        emptyList()
    }

    @kotlinx.serialization.Serializable
    private data class RelatedRow(val bookId: String, val chapter: Int, val verse: Int? = null, val label: String = "")

    private fun decodeRelated(raw: String): List<RelatedPassage> = try {
        json.decodeFromString<List<RelatedRow>>(raw).map {
            RelatedPassage(it.bookId, it.chapter, it.verse, it.label)
        }
    } catch (_: Exception) {
        emptyList()
    }
}
