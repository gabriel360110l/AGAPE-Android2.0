package agape.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream

data class PatristicQuote(
    val fatherName: String,
    val workTitle: String = "",
    val quoteText: String
)

data class VerseCommentary(
    val verseNumber: Int,
    val summaryNl: String,
    val patristicQuotes: List<PatristicQuote> = emptyList(),
    val crossReferences: List<String> = emptyList()
)

data class ChapterExplanation(
    val bookId: String,
    val chapter: Int,
    val title: String,
    val overview: String
)

data class BibleVerseUiModel(
    val bookId: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val commentary: VerseCommentary? = null
)

data class AgpeyaSectionModel(
    val id: String,
    val type: String,
    val titleNl: String,
    val rubricNl: String? = null,
    val bodyNl: String? = null,
    val kyrieCount: Int = 0
)

data class AgpeyaHourModel(
    val id: String,
    val titleNl: String,
    val copticTitle: String,
    val themeNl: String,
    val artwork: String,
    val sections: List<AgpeyaSectionModel>
)

data class PrayerModel(
    val id: String,
    val category: String,
    val titleNl: String,
    val sourceFather: String,
    val rubricNl: String,
    val contentNl: String
)

data class MarkdownSection(
    val heading: String,
    val level: Int,
    val body: String
)

data class TraditionDocument(
    val id: String,
    val title: String,
    val sections: List<MarkdownSection>
)

class AgapeRepository(private val context: Context) {

    private val commentaryCache = LruCache<String, Map<Int, VerseCommentary>>(24)

    private fun openBibleDatabase(): SQLiteDatabase? {
        return runCatching {
            val dbFile = context.getDatabasePath("bible.sqlite")
            if (!dbFile.exists()) {
                dbFile.parentFile?.mkdirs()
                context.assets.open("bible.sqlite").use { input ->
                    FileOutputStream(dbFile).use { output -> input.copyTo(output) }
                }
            }
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        }.getOrNull()
    }

    suspend fun getChapterWithCommentary(
        bookId: String,
        bookNumber: Int,
        chapter: Int
    ): List<BibleVerseUiModel> = withContext(Dispatchers.IO) {
        val commentaryMap = loadCommentaryMap(bookId, chapter)
        val result = mutableListOf<BibleVerseUiModel>()
        val db = openBibleDatabase()

        if (db != null) {
            runCatching {
                db.rawQuery(
                    "SELECT verse, text FROM verses WHERE book_number = ? AND chapter = ? ORDER BY verse ASC",
                    arrayOf(bookNumber.toString(), chapter.toString())
                ).use { cursor ->
                    val vIdx = cursor.getColumnIndex("verse")
                    val tIdx = cursor.getColumnIndex("text")
                    while (cursor.moveToNext()) {
                        val vNum = if (vIdx >= 0) cursor.getInt(vIdx) else result.size + 1
                        val vText = if (tIdx >= 0) cursor.getString(tIdx) else ""
                        result.add(
                            BibleVerseUiModel(
                                bookId = bookId,
                                chapter = chapter,
                                verseNumber = vNum,
                                text = vText,
                                commentary = commentaryMap[vNum]
                            )
                        )
                    }
                }
            }
            db.close()
        }

        if (result.isEmpty()) {
            result.addAll(
                listOf(
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 1,
                        text = "In het begin was het Woord, en het Woord was bij God, en het Woord was God.",
                        commentary = commentaryMap[1] ?: VerseCommentary(
                            verseNumber = 1,
                            summaryNl = "Johannes opent zijn Evangelie met de eeuwige godheid van de Logos (Christus) vóór alle tijden.",
                            patristicQuotes = listOf(
                                PatristicQuote(
                                    fatherName = "H. Athanasius van Alexandrië",
                                    workTitle = "Tegen de Arianen",
                                    quoteText = "Het Woord is niet geschapen uit het niets, maar eeuwig uit het wezen van de Vader voortgekomen: Licht uit Licht."
                                ),
                                PatristicQuote(
                                    fatherName = "H. Johannes Chrysostomus",
                                    workTitle = "Homilieën over Johannes",
                                    quoteText = "Terwijl de andere evangelisten beginnen bij de menswording in de tijd, voert Johannes ons direct op naar de eeuwige troon van de Vader."
                                )
                            )
                        )
                    ),
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 2,
                        text = "Dit was in het begin bij God.",
                        commentary = commentaryMap[2]
                    ),
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 3,
                        text = "Alles is door Hem ontstaan, en zonder Hem is niets ontstaan van wat bestaat.",
                        commentary = commentaryMap[3] ?: VerseCommentary(
                            verseNumber = 3,
                            summaryNl = "De Zoon is de mede-Schepper van zowel de zichtbare als de onzichtbare wereld.",
                            patristicQuotes = listOf(
                                PatristicQuote(
                                    fatherName = "H. Cyrillus van Alexandrië",
                                    workTitle = "Commentaar op het Johannes-evangelie",
                                    quoteText = "De Vader schept alles door de Zoon in de Heilige Geest; daarom is het Woord zelf boven de gehele schepping verheven."
                                )
                            )
                        )
                    ),
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 4,
                        text = "In Hem was leven, en het leven was het licht der mensen;",
                        commentary = commentaryMap[4]
                    ),
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 5,
                        text = "en het licht schijnt in de duisternis, en de duisternis heeft het niet gegrepen.",
                        commentary = commentaryMap[5]
                    ),
                    BibleVerseUiModel(
                        bookId = "JHN",
                        chapter = 1,
                        verseNumber = 14,
                        text = "En het Woord is vlees geworden en heeft onder ons gewoond, en wij hebben Zijn heerlijkheid aanschouwd, een heerlijkheid als van de Eniggeborene des Vaders, vol van genade en waarheid.",
                        commentary = commentaryMap[14] ?: VerseCommentary(
                            verseNumber = 14,
                            summaryNl = "Het centrale mysterie van de Incarnatie: God wordt mens opdat de mens deel krijgt aan Gods onvergankelijke leven (theosis).",
                            patristicQuotes = listOf(
                                PatristicQuote(
                                    fatherName = "H. Athanasius van Alexandrië",
                                    workTitle = "Over de Menswording van het Woord (De Incarnatione)",
                                    quoteText = "Hij werd wat wij zijn, opdat Hij ons zou maken tot wat Hij is."
                                )
                            )
                        )
                    )
                )
            )
        }
        result
    }

    private fun loadCommentaryMap(bookId: String, chapter: Int): Map<Int, VerseCommentary> {
        val key = "${bookId}_$chapter"
        commentaryCache.get(key)?.let { return it }

        val parsed = mutableMapOf<Int, VerseCommentary>()
        runCatching {
            val possiblePaths = listOf(
                "commentary/${bookId.lowercase()}.json",
                "commentary/${bookId.uppercase()}.json"
            )
            val raw = possiblePaths.firstNotNullOfOrNull { path ->
                runCatching { context.assets.open(path).bufferedReader().use { it.readText() } }.getOrNull()
            } ?: return emptyMap()

            val rootObj = JSONObject(raw)
            val chArray: JSONArray? = rootObj.optJSONArray(chapter.toString())
                ?: rootObj.optJSONObject("chapters")?.optJSONArray(chapter.toString())

            if (chArray != null) {
                for (i in 0 until chArray.length()) {
                    val item = chArray.getJSONObject(i)
                    val vNum = item.optInt("verse", item.optInt("verseNumber", i + 1))
                    val summary = item.optString("text", item.optString("summaryNl", item.optString("commentary", "")))
                    if (summary.isNotBlank()) {
                        parsed[vNum] = VerseCommentary(verseNumber = vNum, summaryNl = summary)
                    }
                }
            }
        }
        commentaryCache.put(key, parsed)
        return parsed
    }

    suspend fun loadAgpeyaHours(): List<AgpeyaHourModel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<AgpeyaHourModel>()
        runCatching {
            val raw = context.assets.open("agpeya-hours.json").bufferedReader().use { it.readText() }
            val root = JSONObject(raw)
            val hoursArr = root.optJSONArray("hours") ?: JSONArray()
            for (i in 0 until hoursArr.length()) {
                val h = hoursArr.getJSONObject(i)
                val secArr = h.optJSONArray("sections") ?: JSONArray()
                val sections = mutableListOf<AgpeyaSectionModel>()
                for (j in 0 until secArr.length()) {
                    val s = secArr.getJSONObject(j)
                    sections.add(
                        AgpeyaSectionModel(
                            id = s.optString("id", "sec_$j"),
                            type = s.optString("type", "prayer"),
                            titleNl = s.optString("title_nl", ""),
                            rubricNl = s.optString("rubric_nl").takeIf { it.isNotBlank() },
                            bodyNl = s.optString("body_nl").takeIf { it.isNotBlank() },
                            kyrieCount = s.optInt("kyrie_count", 0)
                        )
                    )
                }
                list.add(
                    AgpeyaHourModel(
                        id = h.optString("id", "prime"),
                        titleNl = h.optString("title_nl", "Het Eerste Uur"),
                        copticTitle = h.optString("coptic_title", "Ⲓⲧⲟ ⲁ̀"),
                        themeNl = h.optString("theme_nl", ""),
                        artwork = h.optString("artwork", "Art/galilee.jpg"),
                        sections = sections
                    )
                )
            }
        }
        list
    }

    suspend fun loadPrayers(): List<PrayerModel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PrayerModel>()
        runCatching {
            val raw = context.assets.open("prayers.json").bufferedReader().use { it.readText() }
            val root = JSONObject(raw)
            val arr = root.optJSONArray("prayers") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                list.add(
                    PrayerModel(
                        id = p.optString("id", "prayer_$i"),
                        category = p.optString("category", "Gebed"),
                        titleNl = p.optString("title_nl", ""),
                        sourceFather = p.optString("source_father", ""),
                        rubricNl = p.optString("rubric_nl", ""),
                        contentNl = p.optString("content_nl", "")
                    )
                )
            }
        }
        list
    }

    suspend fun loadTraditionMarkdown(fileName: String, title: String): TraditionDocument =
        withContext(Dispatchers.IO) {
            val rawMd = runCatching {
                context.assets.open("docs/$fileName").bufferedReader().use { it.readText() }
            }.getOrDefault("")

            val sections = mutableListOf<MarkdownSection>()
            var currentHeading = title
            var currentLevel = 1
            val currentBody = StringBuilder()

            rawMd.lines().forEach { line ->
                if (line.startsWith("#")) {
                    if (currentBody.isNotBlank()) {
                        sections.add(MarkdownSection(currentHeading, currentLevel, currentBody.toString().trim()))
                        currentBody.clear()
                    }
                    currentLevel = line.takeWhile { it == '#' }.length
                    currentHeading = line.trimStart('#', ' ')
                } else {
                    currentBody.appendLine(line)
                }
            }
            if (currentBody.isNotBlank()) {
                sections.add(MarkdownSection(currentHeading, currentLevel, currentBody.toString().trim()))
            }

            if (sections.isEmpty()) {
                sections.add(
                    MarkdownSection(
                        heading = title,
                        level = 1,
                        body = "Zorg dat het bestand docs/$fileName naar android/app/src/main/assets/docs/$fileName is gekopieerd."
                    )
                )
            }
            TraditionDocument(id = fileName, title = title, sections = sections)
        }
}