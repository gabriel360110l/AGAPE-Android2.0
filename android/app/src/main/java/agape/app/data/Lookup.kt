package agape.app.data

import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

object ScriptureLookup {
    private val aliases = mapOf(
        "gen" to "genesis", "genesis" to "genesis", "gn" to "genesis",
        "ex" to "exodus", "exo" to "exodus", "exodus" to "exodus",
        "lev" to "leviticus", "leviticus" to "leviticus",
        "num" to "numeri", "numeri" to "numeri",
        "deut" to "deuteronomium", "deuteronomium" to "deuteronomium", "dt" to "deuteronomium",
        "joz" to "jozua", "jozua" to "jozua", "jos" to "jozua",
        "rich" to "richteren", "richt" to "richteren", "richteren" to "richteren", "ri" to "richteren",
        "ruth" to "ruth", "rt" to "ruth",
        "1sam" to "1samuel", "1samuel" to "1samuel", "2sam" to "2samuel", "2samuel" to "2samuel",
        "1kon" to "1koningen", "1koningen" to "1koningen", "1ki" to "1koningen",
        "2kon" to "2koningen", "2koningen" to "2koningen",
        "1kron" to "1kronieken", "1kronieken" to "1kronieken",
        "2kron" to "2kronieken", "2kronieken" to "2kronieken",
        "ezra" to "ezra", "ezr" to "ezra", "neh" to "nehemia", "nehemia" to "nehemia",
        "esth" to "esther", "esther" to "esther", "ester" to "esther",
        "job" to "job", "ps" to "psalmen", "psalm" to "psalmen", "psalmen" to "psalmen", "pss" to "psalmen",
        "spr" to "spreuken", "spreuken" to "spreuken", "prov" to "spreuken",
        "pred" to "prediker", "prediker" to "prediker", "eccl" to "prediker",
        "hoogl" to "hooglied", "hooglied" to "hooglied", "hl" to "hooglied", "song" to "hooglied",
        "jes" to "jesaja", "jesaja" to "jesaja", "isa" to "jesaja",
        "jer" to "jeremia", "jeremia" to "jeremia",
        "klaagl" to "klaagliederen", "klaagliederen" to "klaagliederen", "klaag" to "klaagliederen",
        "ez" to "ezechiel", "ezechiel" to "ezechiel", "ezech" to "ezechiel", "ezek" to "ezechiel",
        "dan" to "daniel", "daniel" to "daniel",
        "hos" to "hosea", "hosea" to "hosea", "joel" to "joel", "am" to "amos", "amos" to "amos",
        "ob" to "obadja", "obad" to "obadja", "obadja" to "obadja",
        "jona" to "jona", "jon" to "jona", "mi" to "micha", "mic" to "micha", "micha" to "micha",
        "nah" to "nahum", "nahum" to "nahum", "hab" to "habakuk", "habakuk" to "habakuk",
        "sef" to "sefania", "sefanja" to "sefania", "zef" to "sefania", "zefanja" to "sefania",
        "hag" to "haggai", "haggai" to "haggai", "zach" to "zacharia", "zacharia" to "zacharia", "zech" to "zacharia",
        "mal" to "maleachi", "maleachi" to "maleachi",
        "mt" to "matteus", "mat" to "matteus", "matt" to "matteus", "matteus" to "matteus", "mattheus" to "matteus",
        "mc" to "marcus", "mar" to "marcus", "marc" to "marcus", "marcus" to "marcus", "mark" to "marcus",
        "lc" to "lucas", "luc" to "lucas", "luk" to "lucas", "lukas" to "lucas", "lucas" to "lucas", "luke" to "lucas",
        "joh" to "johannes", "jn" to "johannes", "johannes" to "johannes", "john" to "johannes",
        "hand" to "handelingen", "handelingen" to "handelingen", "hnd" to "handelingen", "acts" to "handelingen", "ac" to "handelingen",
        "rom" to "romeinen", "roma" to "romeinen", "romeinen" to "romeinen",
        "1kor" to "1korintiers", "1co" to "1korintiers", "kor" to "1korintiers", "1korintiers" to "1korintiers",
        "2kor" to "2korintiers", "2co" to "2korintiers", "2korintiers" to "2korintiers",
        "gal" to "galaten", "galaten" to "galaten",
        "ef" to "efeziers", "efeze" to "efeziers", "efeziers" to "efeziers",
        "fil" to "filippenzen", "filip" to "filippenzen", "filippenzen" to "filippenzen",
        "kol" to "kolossenzen", "kolossenzen" to "kolossenzen", "col" to "kolossenzen",
        "1thess" to "1thessalonicenzen", "1th" to "1thessalonicenzen", "1thessalonicenzen" to "1thessalonicenzen",
        "2thess" to "2thessalonicenzen", "2th" to "2thessalonicenzen", "2thessalonicenzen" to "2thessalonicenzen",
        "1tim" to "1timotheus", "1ti" to "1timotheus", "1timotheus" to "1timotheus",
        "2tim" to "2timotheus", "2ti" to "2timotheus", "2timotheus" to "2timotheus",
        "tit" to "titus", "titus" to "titus", "filem" to "filemon", "filemon" to "filemon", "phm" to "filemon",
        "heb" to "hebreen", "hebr" to "hebreen", "hebreen" to "hebreen",
        "jak" to "jakobus", "jakobus" to "jakobus", "jas" to "jakobus",
        "1pet" to "1petrus", "1pe" to "1petrus", "1petrus" to "1petrus",
        "2pet" to "2petrus", "2pe" to "2petrus", "2petrus" to "2petrus",
        "1joh" to "1johannes", "1jn" to "1johannes", "1johannes" to "1johannes",
        "2joh" to "2johannes", "2jn" to "2johannes", "2johannes" to "2johannes",
        "3joh" to "3johannes", "3jn" to "3johannes", "3johannes" to "3johannes",
        "jud" to "judas", "judas" to "judas", "jude" to "judas",
        "op" to "openbaring", "openb" to "openbaring", "openbaring" to "openbaring", "rev" to "openbaring", "apoc" to "openbaring",
        "tob" to "tobit", "tobit" to "tobit", "judith" to "judith", "jdt" to "judith",
        "wijsh" to "wijsheid", "wijsheid" to "wijsheid", "wis" to "wijsheid",
        "sir" to "sirach", "sirach" to "sirach", "bar" to "baruch", "baruch" to "baruch",
        "1makk" to "1maccabeen", "1macc" to "1maccabeen", "1maccabeen" to "1maccabeen",
        "2makk" to "2maccabeen", "2macc" to "2maccabeen", "2maccabeen" to "2maccabeen",
        "3makk" to "3maccabeen", "3macc" to "3maccabeen", "3maccabeen" to "3maccabeen",
        "1esdras" to "1esdras", "3ezra" to "1esdras", "2esdras" to "2esdras", "4ezra" to "2esdras",
        "manasse" to "gebed-manasse", "man" to "gebed-manasse", "psalm151" to "psalm151", "ps151" to "psalm151",
        "susanna" to "susanna", "sus" to "susanna", "bel" to "bel", "azarja" to "gebed-azaria", "azaria" to "gebed-azaria"
    )

    fun parse(query: String): ChapterRef? {
        val folded = fold(query)
        val match = Regex("^([1-3]?\\s*[a-z]+)\\s+(\\d+)(?:\\s*[:.]\\s*([\\d,\\-\\s]+))?$").find(folded) ?: return null
        val alias = match.groupValues[1].replace(" ", "")
        val bookId = aliases[alias] ?: return null
        val book = ContentCatalog.book(bookId) ?: return null
        val chapter = match.groupValues[2].toIntOrNull() ?: return null
        if (chapter !in 1..book.chapters) return null
        val extra = match.groupValues.getOrNull(3).orEmpty()
        val verse = extra.split(",").firstOrNull()?.split("-")?.firstOrNull()?.trim()?.toIntOrNull()
        return ChapterRef(bookId, chapter, verse)
    }

    private fun fold(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(".", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

object Readings {
    fun forDay(dateMillis: Long = System.currentTimeMillis()): Triple<ChapterRef, ChapterRef, ChapterRef> {
        val n = maxOf(0, dayOfYear(dateMillis) - 1)
        fun pool(groups: List<BookGroup>): List<ChapterRef> =
            ContentCatalog.books.filter { it.group in groups }
                .flatMap { book -> (1..book.chapters).map { ChapterRef(book.id, it) } }
        val ot = pool(listOf(BookGroup.wet, BookGroup.geschiedenis, BookGroup.wijsheid, BookGroup.groteProfeten, BookGroup.kleineProfeten))
        val gospel = pool(listOf(BookGroup.evangelie))
        val apostle = pool(listOf(BookGroup.paulus, BookGroup.katholiek, BookGroup.handelingen, BookGroup.openbaring))
        return Triple(
            ot[n % maxOf(ot.size, 1)],
            gospel[n % maxOf(gospel.size, 1)],
            apostle[n % maxOf(apostle.size, 1)]
        )
    }

    fun dayOfYear(dateMillis: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDate(dateMillis: Long, locale: AppLocale): String {
        val loc = if (locale == AppLocale.en) Locale.UK else Locale("nl", "NL")
        val fmt = java.text.SimpleDateFormat("EEEE d MMMM yyyy", loc)
        return fmt.format(java.util.Date(dateMillis))
    }
}
