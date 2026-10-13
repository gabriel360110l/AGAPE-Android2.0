package agape.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.Calendar
import java.util.TimeZone

object ContentCatalog {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    lateinit var books: List<BookMeta> private set
    lateinit var groups: List<BookGroupMeta> private set
    lateinit var quotes: List<QuoteItem> private set
    lateinit var words: List<DailyWord> private set
    lateinit var prayers: List<PrayerItem> private set
    lateinit var homePrayerIDs: List<String> private set
    lateinit var sevenPrayerIDs: List<String> private set
    lateinit var plans: List<ReadingPlan> private set
    lateinit var fathers: List<FatherQuote> private set
    lateinit var glossary: List<GlossaryEntry> private set
    lateinit var topics: List<Topic> private set
    lateinit var harmony: List<HarmonyEvent> private set
    lateinit var hours: List<AgpeyaHour> private set
    lateinit var offices: Map<String, List<AgpeyaBlock>> private set
    lateinit var church: Map<String, ChurchNode> private set
    lateinit var churchMeanings: Map<String, ChurchMeaning> private set
    lateinit var menologion: Map<String, MenologionEntry> private set
    lateinit var greatFixed: List<MenologionEntry> private set
    lateinit var suggestions: List<LocalizedPair> private set
    lateinit var translations: List<TranslationMeta> private set
    lateinit var strings: Map<String, Map<String, String>> private set
    lateinit var bookNameEn: Map<String, String> private set
    lateinit var redLetter: Map<String, Map<String, List<Int>>> private set

    private val commentaryCache = mutableMapOf<String, Map<String, ChapterCommentary>>()
    private lateinit var appContext: Context

    fun load(context: Context) {
        appContext = context.applicationContext
        val canon = loadJson<CanonFile>("canon")
        books = canon.books
        groups = canon.groups
        quotes = loadJson("quotes")
        words = loadJson("words")
        prayers = loadJson("prayers")
        val ids = loadJson<PrayerIDs>("prayer-ids")
        homePrayerIDs = ids.home
        sevenPrayerIDs = ids.seven
        plans = loadJson("plans")
        fathers = loadJson("fathers")
        glossary = loadJson("glossary")
        topics = loadJson("topics")
        harmony = loadJson("harmony")
        hours = loadJson("agpeya-hours")
        offices = loadJson("agpeya-offices")
        church = loadJson<List<ChurchNode>>("church").associateBy { it.id }
        churchMeanings = loadJson<List<ChurchMeaning>>("church-meanings").associateBy { it.id }
        val meno = loadJson<MenoFile>("menologion")
        menologion = meno.entries.associateBy { "${it.month}-${it.day}" }
        greatFixed = meno.great ?: meno.entries.filter { it.rank == "great" || it.rank == "major" }
        suggestions = loadJson("search-suggestions")
        translations = loadJson("translations")
        val i18n = loadJson<I18nFile>("i18n")
        strings = mapOf("nl" to i18n.nl, "en" to i18n.en)
        bookNameEn = i18n.bookNameEn
        redLetter = loadJson("red-letter")
    }

    private inline fun <reified T> loadJson(name: String): T {
        val text = appContext.assets.open("json/$name.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    fun book(id: String) = books.firstOrNull { it.id == id }
    @Suppress("UNUSED_PARAMETER")
    fun books(forTradition: Tradition) = books.filter { it.scope != CanonScope.hidden }
    fun scriptureBooks(forTradition: Tradition) = books(forTradition).filter { it.testament != Testament.vader }

    fun allChapters(tradition: Tradition): List<ChapterRef> =
        scriptureBooks(tradition).flatMap { book ->
            (1..maxOf(book.chapters, 1)).map { ChapterRef(book.id, it) }
        }

    fun firstChapter(tradition: Tradition) =
        allChapters(tradition).firstOrNull() ?: ChapterRef("genesis", 1)

    fun chapterIndex(ref: ChapterRef, tradition: Tradition) =
        allChapters(tradition).indexOfFirst { it.bookId == ref.bookId && it.chapter == ref.chapter }.takeIf { it >= 0 }

    fun translationsFor(locale: AppLocale) =
        translations.filter { if (locale == AppLocale.en) it.id == "kjv" else it.id == "sv" || it.id == "hsv" }

    fun bookName(book: BookMeta, locale: AppLocale) =
        if (locale == AppLocale.en) bookNameEn[book.id] ?: book.name else book.name

    fun t(key: String, locale: AppLocale): String {
        val value = strings[locale.name]?.get(key)
        if (!value.isNullOrEmpty()) return value
        if (locale == AppLocale.en) return strings["en"]?.get(key) ?: key
        return strings["nl"]?.get(key) ?: key
    }

    fun pick(dutch: String, english: String?, locale: AppLocale) =
        if (locale == AppLocale.en && !english.isNullOrEmpty()) english else dutch

    fun bookTeaser(book: BookMeta, locale: AppLocale) = pick(book.teaser, book.teaserEn, locale)
    fun bookIntro(book: BookMeta, locale: AppLocale) = pick(book.intro, book.introEn, locale)
    fun bookAuthor(book: BookMeta, locale: AppLocale) = pick(book.author, book.authorEn, locale)
    fun searchSuggestions(locale: AppLocale) = suggestions.map { it.text(locale) }
    fun planTitle(plan: ReadingPlan, locale: AppLocale) = pick(plan.title, plan.titleEn, locale)
    fun planTeaser(plan: ReadingPlan, locale: AppLocale) = pick(plan.teaser, plan.teaserEn, locale)
    fun planDayLabel(day: PlanDay, locale: AppLocale): String {
        val b = book(day.bookId) ?: return day.label
        return "${bookName(b, locale)} ${day.chapter}"
    }
    fun topicTitle(topic: Topic, locale: AppLocale) = pick(topic.title, topic.titleEn, locale)
    fun topicTeaser(topic: Topic, locale: AppLocale) = pick(topic.teaser, topic.teaserEn, locale)
    fun fatherName(quote: FatherQuote, locale: AppLocale) = pick(quote.father, quote.fatherEn, locale)
    fun fatherSource(quote: FatherQuote, locale: AppLocale) = pick(quote.source, quote.sourceEn, locale)
    fun fatherText(quote: FatherQuote, locale: AppLocale) = pick(quote.text, quote.textEn, locale)
    fun glossaryTerm(entry: GlossaryEntry, locale: AppLocale) = pick(entry.term, entry.termEn, locale)
    fun glossaryBody(entry: GlossaryEntry, locale: AppLocale) = pick(entry.body, entry.bodyEn, locale)
    fun harmonyTitle(event: HarmonyEvent, locale: AppLocale) = pick(event.title, event.titleEn, locale)
    fun harmonyKicker(event: HarmonyEvent, locale: AppLocale) = pick(event.kicker, event.kickerEn, locale)
    fun harmonyTeaser(event: HarmonyEvent, locale: AppLocale) = pick(event.teaser, event.teaserEn, locale)
    fun wordLemma(word: DailyWord, locale: AppLocale) = pick(word.dutch, word.english, locale)
    fun wordMeaning(word: DailyWord, locale: AppLocale) = pick(word.meaning, word.meaningEn, locale)
    fun hourTeaser(hour: AgpeyaHour, locale: AppLocale) = pick(hour.teaser, hour.teaserEn, locale)
    fun blockKicker(block: AgpeyaBlock, locale: AppLocale) = pick(block.kicker, block.kickerEn, locale)
    fun blockTitle(block: AgpeyaBlock, locale: AppLocale): String? {
        val title = block.title ?: return null
        return pick(title, block.titleEn, locale)
    }
    fun blockLines(block: AgpeyaBlock, locale: AppLocale) =
        if (locale == AppLocale.en) block.linesEn ?: block.lines else block.lines
    fun translationName(item: TranslationMeta, locale: AppLocale) = pick(item.name, item.nameEn, locale)
    fun translationTeaser(item: TranslationMeta, locale: AppLocale) = pick(item.teaser, item.teaserEn, locale)
    fun prayer(id: String) = prayers.firstOrNull { it.id == id }
    fun prayerTitle(p: PrayerItem, locale: AppLocale) = if (locale == AppLocale.en) p.titleEn ?: p.title else p.title
    fun prayerKicker(p: PrayerItem, locale: AppLocale) = if (locale == AppLocale.en) p.kickerEn ?: p.kicker else p.kicker
    fun prayerBody(p: PrayerItem, locale: AppLocale) = if (locale == AppLocale.en) p.bodyEn ?: p.body else p.body

    fun hourTitle(hour: AgpeyaHour, locale: AppLocale): String {
        if (locale != AppLocale.en) return hour.title
        return mapOf(
            "lauden" to "Prime", "terts" to "Terce", "sext" to "Sext", "none" to "None",
            "vespers" to "Vespers", "completen" to "Compline", "voorhangsel" to "Veil", "middernacht" to "Midnight"
        )[hour.id] ?: hour.title
    }

    fun hourTheme(hour: AgpeyaHour, locale: AppLocale): String {
        if (locale != AppLocale.en) return hour.theme
        return mapOf(
            "lauden" to "Resurrection", "terts" to "Holy Spirit", "sext" to "Crucifixion",
            "none" to "Death on the Cross", "vespers" to "Descent from the Cross",
            "completen" to "Burial", "voorhangsel" to "Examination", "middernacht" to "The Bridegroom"
        )[hour.id] ?: hour.theme
    }

    fun groupTitle(id: BookGroup, locale: AppLocale): String {
        val key = when (id) {
            BookGroup.wet -> "groupLaw"
            BookGroup.geschiedenis -> "groupHistory"
            BookGroup.wijsheid -> "groupWritings"
            BookGroup.groteProfeten -> "groupMajorProphets"
            BookGroup.kleineProfeten -> "groupTwelve"
            BookGroup.evangelie -> "groupGospels"
            BookGroup.handelingen -> "groupActs"
            BookGroup.paulus -> "groupPaul"
            BookGroup.katholiek -> "groupCatholic"
            BookGroup.openbaring -> "groupRev"
            BookGroup.anagignoskomena -> "deuterocanon"
            BookGroup.vader -> "fathers"
        }
        return t(key, locale)
    }

    fun isWordsOfChrist(bookId: String, chapter: Int, verse: Int) =
        redLetter[bookId]?.get(chapter.toString())?.contains(verse) == true

    fun currentHour(atMillis: Long = System.currentTimeMillis()): AgpeyaHour {
        val cal = Calendar.getInstance()
        cal.timeInMillis = atMillis
        val h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
        val id = when {
            h >= 23 || h < 4.5 -> "middernacht"
            h < 8 -> "lauden"
            h < 11 -> "terts"
            h < 14 -> "sext"
            h < 16 -> "none"
            h < 17.5 -> "vespers"
            h < 20.5 -> "completen"
            else -> "voorhangsel"
        }
        return hours.firstOrNull { it.id == id } ?: hours.first()
    }

    fun quoteOfDay(dateMillis: Long = System.currentTimeMillis()): QuoteItem {
        val day = utcDay(dateMillis)
        return quotes[kotlin.math.abs(day) % maxOf(quotes.size, 1)]
    }

    fun verseBackground(dateMillis: Long = System.currentTimeMillis()): String {
        val names = listOf("church", "galilee", "wilderness", "tomb", "tabor", "night")
        val day = utcDay(dateMillis)
        return names[kotlin.math.abs(day) % names.size]
    }

    fun commentary(bookId: String, chapter: Int): ChapterCommentary? {
        synchronized(commentaryCache) {
            if (bookId !in commentaryCache) {
                commentaryCache[bookId] = loadCommentary(bookId)
            }
            return commentaryCache[bookId]?.get(chapter.toString())
        }
    }

    fun churchChildren(id: String): List<ChurchNode> {
        val node = church[id] ?: return emptyList()
        return node.children.mapNotNull { church[it] }
    }

    fun churchMeaning(id: String) = churchMeanings[id]

    fun adjacent(bookId: String, chapter: Int, delta: Int, tradition: Tradition): ChapterRef? {
        val ordered = scriptureBooks(tradition)
        val index = ordered.indexOfFirst { it.id == bookId }
        if (index < 0) return null
        val book = ordered[index]
        val next = chapter + delta
        if (next in 1..book.chapters) return ChapterRef(bookId, next)
        val neighborIndex = index + delta
        if (neighborIndex !in ordered.indices) return null
        val neighbor = ordered[neighborIndex]
        return ChapterRef(neighbor.id, if (delta > 0) 1 else neighbor.chapters)
    }

    fun randomChapter(tradition: Tradition): ChapterRef {
        val pool = books(tradition)
        val book = pool.randomOrNull() ?: books.first()
        return ChapterRef(book.id, (1..maxOf(book.chapters, 1)).random())
    }

    private fun utcDay(millis: Long): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return (cal.timeInMillis / 86_400_000L).toInt()
    }

    private fun loadCommentary(bookId: String): Map<String, ChapterCommentary> {
        return try {
            val text = appContext.assets.open("commentary/commentary-$bookId.json").bufferedReader().use { it.readText() }
            val element = json.parseToJsonElement(text)
            if (element is JsonObject) {
                element.mapValues { json.decodeFromJsonElement<ChapterCommentary>(it.value) }
            } else emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
