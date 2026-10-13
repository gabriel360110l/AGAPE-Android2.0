package agape.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.text.Normalizer

object BibleDatabase {
    private var db: SQLiteDatabase? = null
    private val chapterCache = LinkedHashMap<String, List<VerseRecord>>()
    private val lock = Any()

    fun open(context: Context) {
        synchronized(lock) {
            db?.close()
            db = SQLiteDatabase.openDatabase(
                AssetPack.databaseFile(context, "bible.sqlite").path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
    }

    fun verses(bookId: String, chapter: Int): List<VerseRecord> {
        synchronized(lock) {
            val key = "$bookId:$chapter"
            chapterCache[key]?.let { return it }
            val handle = db ?: return emptyList()
            val rows = mutableListOf<VerseRecord>()
            handle.rawQuery(
                "SELECT verse, sv, hsv, kjv FROM verses WHERE book_id = ? AND chapter = ? ORDER BY verse",
                arrayOf(bookId, chapter.toString())
            ).use { c ->
                val iVerse = c.getColumnIndexOrThrow("verse")
                val iSv = c.getColumnIndexOrThrow("sv")
                val iHsv = c.getColumnIndexOrThrow("hsv")
                val iKjv = c.getColumnIndexOrThrow("kjv")
                while (c.moveToNext()) {
                    rows += VerseRecord(
                        bookId = bookId,
                        chapter = chapter,
                        verse = c.getInt(iVerse),
                        sv = c.getString(iSv).orEmpty(),
                        hsv = c.getString(iHsv).orEmpty(),
                        kjv = c.getString(iKjv).orEmpty()
                    )
                }
            }
            if (chapterCache.size > 48) chapterCache.clear()
            chapterCache[key] = rows
            return rows
        }
    }

    fun verse(bookId: String, chapter: Int, verse: Int): VerseRecord? =
        verses(bookId, chapter).firstOrNull { it.verse == verse }

    fun search(query: String, english: Boolean = false, limit: Int = 50): List<SearchHit> {
        val folded = fold(query)
        if (folded.length < 2) return emptyList()
        val fts = searchFts(folded, english, limit)
        if (fts.isNotEmpty()) return fts
        return searchLike(folded, english, limit)
    }

    private fun searchFts(folded: String, english: Boolean, limit: Int): List<SearchHit> {
        synchronized(lock) {
            val handle = db ?: return emptyList()
            val tokens = folded.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }
            if (tokens.isEmpty()) return emptyList()
            val match = tokens.joinToString(" AND ") { "$it*" }
            val sql = if (english) {
                "SELECT book_id, chapter, verse, kjv FROM verses_fts_en WHERE verses_fts_en MATCH ? LIMIT ?"
            } else {
                "SELECT book_id, chapter, verse, sv FROM verses_fts WHERE verses_fts MATCH ? LIMIT ?"
            }
            return try {
                handle.rawQuery(sql, arrayOf(match, limit.toString())).use { collectHits(it) }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun searchLike(folded: String, english: Boolean, limit: Int): List<SearchHit> {
        synchronized(lock) {
            val handle = db ?: return emptyList()
            val column = if (english) "folded_en" else "folded"
            val textColumn = if (english) "kjv" else "sv"
            val like = "%${escapeLike(folded)}%"
            val sql = "SELECT book_id, chapter, verse, $textColumn FROM verses WHERE $column LIKE ? ESCAPE '\\' LIMIT ?"
            return try {
                handle.rawQuery(sql, arrayOf(like, limit.toString())).use { collectHits(it) }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun collectHits(c: android.database.Cursor): List<SearchHit> {
        val hits = mutableListOf<SearchHit>()
        val iBook = 0
        while (c.moveToNext()) {
            val bookId = c.getString(iBook) ?: continue
            if (ContentCatalog.book(bookId)?.scope == CanonScope.hidden) continue
            hits += SearchHit(
                bookId = bookId,
                chapter = c.getInt(1),
                verse = c.getInt(2),
                text = c.getString(3).orEmpty()
            )
        }
        return hits
    }

    fun fold(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{M}+"), "").lowercase()
    }

    private fun escapeLike(s: String) =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
