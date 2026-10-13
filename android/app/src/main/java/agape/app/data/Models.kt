package agape.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Tradition { catholic, orthodox }

@Serializable
enum class Testament { ot, nt, dc, vader }

@Serializable
enum class CanonScope { both, orthodox, hidden }

@Serializable
enum class BookGroup {
    @SerialName("wet") wet,
    @SerialName("geschiedenis") geschiedenis,
    @SerialName("wijsheid") wijsheid,
    @SerialName("grote-profeten") groteProfeten,
    @SerialName("kleine-profeten") kleineProfeten,
    @SerialName("evangelie") evangelie,
    @SerialName("handelingen") handelingen,
    @SerialName("paulus") paulus,
    @SerialName("katholiek") katholiek,
    @SerialName("openbaring") openbaring,
    @SerialName("anagignoskomena") anagignoskomena,
    @SerialName("vader") vader
}

@Serializable
enum class TranslationID { sv, hsv, kjv }

@Serializable
enum class NightMode { auto, day, night }

@Serializable
enum class CalendarRite { julian, gregorian }

@Serializable
enum class AppLocale { nl, en }

@Serializable
enum class MarkTint { cream, gold, sky, green, rose, amber }

@Serializable
data class BookMeta(
    val id: String,
    val name: String,
    val shortName: String,
    val chapters: Int,
    val group: BookGroup,
    val testament: Testament,
    val greek: String = "",
    val author: String = "",
    val teaser: String = "",
    val intro: String = "",
    val authorEn: String? = null,
    val teaserEn: String? = null,
    val introEn: String? = null,
    val scope: CanonScope = CanonScope.both
) {
    fun inTradition(tradition: Tradition): Boolean = when (scope) {
        CanonScope.hidden -> false
        CanonScope.orthodox -> tradition == Tradition.orthodox
        CanonScope.both -> true
    }
}

@Serializable
data class BookGroupMeta(
    val id: BookGroup,
    val title: String,
    val testament: Testament
)

@Serializable
data class QuoteItem(
    val bookId: String,
    val book: String = "",
    val chapter: Int,
    val verse: Int,
    val text: String
) {
    val id: String get() = "$bookId:$chapter:$verse"
}

@Serializable
data class DailyWord(
    val dutch: String,
    val original: String = "",
    val translit: String = "",
    val meaning: String = "",
    val bookId: String = "",
    val book: String = "",
    val chapter: Int = 1,
    val verse: Int = 1,
    val english: String? = null,
    val meaningEn: String? = null
) {
    val id: String get() = dutch
}

@Serializable
data class PrayerItem(
    val id: String,
    val title: String,
    val kicker: String = "",
    val body: List<String> = emptyList(),
    val titleEn: String? = null,
    val kickerEn: String? = null,
    val bodyEn: List<String>? = null
)

@Serializable
data class PlanDay(
    val bookId: String,
    val chapter: Int,
    val label: String = ""
)

@Serializable
data class ReadingPlan(
    val id: String,
    val title: String,
    val teaser: String = "",
    val titleEn: String? = null,
    val teaserEn: String? = null,
    val days: List<PlanDay> = emptyList()
)

@Serializable
data class FatherQuote(
    val father: String,
    val source: String = "",
    val text: String = "",
    val bookId: String? = null,
    val fatherEn: String? = null,
    val sourceEn: String? = null,
    val textEn: String? = null
) {
    val id: String get() = father + source
}

@Serializable
data class GlossaryEntry(
    val id: String,
    val term: String,
    val greek: String = "",
    val translit: String = "",
    val body: String = "",
    val termEn: String? = null,
    val bodyEn: String? = null
)

@Serializable
data class TopicVerse(
    val bookId: String,
    val book: String = "",
    val chapter: Int,
    val verse: Int,
    val text: String = ""
)

@Serializable
data class Topic(
    val id: String,
    val title: String,
    val teaser: String = "",
    val titleEn: String? = null,
    val teaserEn: String? = null,
    val verses: List<TopicVerse> = emptyList()
)

@Serializable
data class HarmonyRef(
    val bookId: String,
    val name: String = "",
    val chapter: Int,
    val verses: String? = null
)

@Serializable
data class HarmonyEvent(
    val id: String,
    val title: String,
    val kicker: String = "",
    val teaser: String = "",
    val titleEn: String? = null,
    val kickerEn: String? = null,
    val teaserEn: String? = null,
    val refs: List<HarmonyRef> = emptyList()
)

@Serializable
data class AgpeyaHour(
    val id: String,
    val title: String,
    val latin: String = "",
    val clock: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val theme: String = "",
    val teaser: String = "",
    val teaserEn: String? = null,
    val alarm: Boolean = false
)

@Serializable
data class AgpeyaBlock(
    val kicker: String = "",
    val title: String? = null,
    val lines: List<String> = emptyList(),
    val kickerEn: String? = null,
    val titleEn: String? = null,
    val linesEn: List<String>? = null,
    val repetitions: Int? = null
)

@Serializable
data class LocalizedPair(
    val nl: String = "",
    val en: String = ""
) {
    fun text(locale: AppLocale) = if (locale == AppLocale.en) en else nl
}

@Serializable
data class ChurchNode(
    val id: String,
    val parentId: String? = null,
    val title: LocalizedPair = LocalizedPair(),
    val teaser: LocalizedPair = LocalizedPair(),
    val children: List<String> = emptyList()
)

@Serializable
data class ChurchMeaning(
    val id: String,
    val original: String? = null,
    val translit: String? = null,
    val body: LocalizedPair = LocalizedPair()
)

@Serializable
data class MenologionEntry(
    val month: Int,
    val day: Int,
    val title: String,
    val teaser: String = "",
    val explanation: String = "",
    val rank: String = "saint",
    val titleEn: String? = null,
    val teaserEn: String? = null,
    val explanationEn: String? = null
) {
    fun title(locale: AppLocale) = if (locale == AppLocale.en) titleEn ?: title else title
    fun teaser(locale: AppLocale) = if (locale == AppLocale.en) teaserEn ?: teaser else teaser
    fun explanation(locale: AppLocale) = if (locale == AppLocale.en) explanationEn ?: explanation else explanation
}

@Serializable
data class CommentarySection(val verses: String = "", val title: String = "", val body: String = "")

@Serializable
data class WordStudy(
    val dutch: String = "",
    val original: String = "",
    val translit: String = "",
    val lang: String = "",
    val meaning: String = ""
)

@Serializable
data class ReferenceNote(val ref: String = "", val note: String = "")

@Serializable
data class FatherNote(val father: String = "", val note: String = "")

@Serializable
data class ChapterCommentary(
    val summary: String = "",
    val meaning: String = "",
    val sections: List<CommentarySection> = emptyList(),
    val words: List<WordStudy> = emptyList(),
    val ot: List<ReferenceNote> = emptyList(),
    val fathers: List<FatherNote> = emptyList(),
    val liturgy: String? = null,
    val refs: List<ReferenceNote> = emptyList()
)

@Serializable
data class SavedVerse(
    val key: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val text: String = ""
) {
    val id: String get() = key
}

@Serializable
data class HistoryItem(
    val bookId: String,
    val chapter: Int,
    val at: String
) {
    val id: String get() = "$bookId-$chapter-$at"
}

@Serializable
data class ChapterRef(
    val bookId: String,
    val chapter: Int,
    val verse: Int? = null
) {
    val id: String get() = "$bookId:$chapter:${verse ?: 0}"
}

@Serializable
data class TranslationMeta(
    val id: String,
    val short: String = "",
    val name: String = "",
    val year: String = "",
    val teaser: String = "",
    val nameEn: String? = null,
    val teaserEn: String? = null
)

object VerseKey {
    fun make(bookId: String, chapter: Int, verse: Int) = "$bookId:$chapter:$verse"
}

data class VerseRecord(
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val sv: String,
    val hsv: String,
    val kjv: String
) {
    fun text(translation: TranslationID) = when (translation) {
        TranslationID.hsv -> hsv
        TranslationID.kjv -> kjv
        TranslationID.sv -> sv
    }
}

data class SearchHit(
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val text: String
) {
    val id: String get() = "$bookId:$chapter:$verse"
}

data class RelatedPassage(
    val bookId: String,
    val chapter: Int,
    val verse: Int?,
    val label: String
) {
    val id: String get() = "$bookId:$chapter:${verse ?: 0}:$label"
}

data class ChurchFatherCommentary(
    val father: String,
    val fatherDescription: String,
    val workTitle: String,
    val approximateDate: String,
    val commentary: String,
    val whyItMatters: String,
    val sourceReference: String,
    val relatedVerses: String,
    val paraphrase: Boolean
) {
    val id: String get() = father + workTitle + relatedVerses
}

data class ChapterGuide(
    val bookId: String,
    val chapter: Int,
    val title: String,
    val summary: String,
    val purpose: String,
    val context: String,
    val themes: List<String>,
    val related: List<RelatedPassage>,
    val fathers: List<ChurchFatherCommentary>,
    val interpretations: String,
    val deuterocanonical: Boolean,
    val sections: List<GuideSection> = emptyList(),
    val questions: List<String> = emptyList(),
    val readingAid: Boolean = false
) {
    val id: String get() = "$bookId:$chapter"
}

data class GuideSearchHit(
    val bookId: String,
    val chapter: Int,
    val title: String,
    val snippet: String
) {
    val id: String get() = "$bookId:$chapter"
}

data class CalendarDay(
    val dateMillis: Long,
    val ymd: String,
    val title: String,
    val saint: String,
    val teaser: String,
    val explanation: String,
    val tone: String,
    val season: String,
    val rank: String,
    val menologionLabel: String
)

data class PrayerPoint(
    val id: String,
    val group: PrayerPointGroup,
    val titleNL: String,
    val titleEN: String,
    val bodyNL: String,
    val bodyEN: String,
    val refs: List<ChapterRef>
) {
    fun title(locale: AppLocale) = if (locale == AppLocale.en) titleEN else titleNL
    fun body(locale: AppLocale) = if (locale == AppLocale.en) bodyEN else bodyNL
}

enum class PrayerPointGroup {
    personal, people, daily, world;

    fun title(locale: AppLocale): String = when (this to locale) {
        personal to AppLocale.nl -> "Persoonlijk"
        personal to AppLocale.en -> "Personal"
        people to AppLocale.nl -> "Mensen om mij heen"
        people to AppLocale.en -> "Those around me"
        daily to AppLocale.nl -> "Dagelijks leven"
        daily to AppLocale.en -> "Daily life"
        world to AppLocale.nl -> "Wereld"
        else -> "The world"
    }
}

data class PrayerAudience(
    val id: String,
    val titleNL: String,
    val titleEN: String,
    val asksName: Boolean,
    val leadNL: String,
    val leadEN: String,
    val tipsNL: List<String>,
    val tipsEN: List<String>,
    val prayerNL: String,
    val prayerEN: String,
    val relatedPointId: String?
) {
    fun title(locale: AppLocale) = if (locale == AppLocale.en) titleEN else titleNL
    fun lead(locale: AppLocale) = if (locale == AppLocale.en) leadEN else leadNL
    fun tips(locale: AppLocale) = if (locale == AppLocale.en) tipsEN else tipsNL
    fun prayer(locale: AppLocale, name: String): String {
        val raw = if (locale == AppLocale.en) prayerEN else prayerNL
        val who = name.trim()
        return if (who.isEmpty()) {
            raw.replace("{naam}", "deze persoon").replace("{name}", "this person")
        } else {
            raw.replace("{naam}", who).replace("{name}", who)
        }
    }
}

@Serializable
data class UserState(
    var last: ChapterRef? = null,
    var profileName: String = "",
    var read: Map<String, List<Int>> = emptyMap(),
    var fontScale: Double = 1.0,
    var redLetter: Boolean = true,
    var calendarRite: CalendarRite = CalendarRite.julian,
    var translation: TranslationID = TranslationID.sv,
    var locale: AppLocale = AppLocale.en,
    var nightMode: NightMode = NightMode.auto,
    var prayerAlarms: Boolean = true,
    var alarmHours: Map<String, Boolean> = emptyMap(),
    var planDone: Map<String, List<Int>> = emptyMap(),
    var prayerCount: Int = 0,
    var prayerSessions: Map<String, Int> = emptyMap(),
    var prayerTodayCount: Int = 0,
    var prayerTodayYmd: String? = null,
    var streak: Int = 0,
    var lastReadDay: String? = null,
    var history: List<HistoryItem> = emptyList(),
    var seenIntro: Boolean = false,
    var seenSetup: Boolean = false,
    var tradition: Tradition = Tradition.orthodox,
    var calendarView: String = "month",
    var calendarMinor: Boolean = false,
    var recentQueries: List<String> = emptyList(),
    var colors: Map<String, MarkTint> = emptyMap(),
    var saved: List<SavedVerse> = emptyList(),
    var notes: Map<String, String> = emptyMap(),
    var snippets: Map<String, SavedVerse> = emptyMap(),
    var verseNumbers: Boolean = true,
    var lineHeight: Double = 1.0,
    var hapticsEnabled: Boolean = true
)

@Serializable
data class CanonFile(val books: List<BookMeta>, val groups: List<BookGroupMeta>)

@Serializable
data class I18nFile(
    val nl: Map<String, String> = emptyMap(),
    val en: Map<String, String> = emptyMap(),
    val bookNameEn: Map<String, String> = emptyMap()
)

@Serializable
data class PrayerIDs(val home: List<String> = emptyList(), val seven: List<String> = emptyList())

@Serializable
data class MenoFile(
    val great: List<MenologionEntry>? = null,
    val entries: List<MenologionEntry> = emptyList()
)
