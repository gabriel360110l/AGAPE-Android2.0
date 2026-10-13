package agape.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JourneyStep(
    val bookId: String,
    val chapter: Int,
    val titleNL: String,
    val titleEN: String,
    val contextNL: String,
    val contextEN: String,
    val questionNL: String,
    val questionEN: String
) {
    fun title(locale: AppLocale) = if (locale == AppLocale.en) titleEN else titleNL
    fun context(locale: AppLocale) = if (locale == AppLocale.en) contextEN else contextNL
    fun question(locale: AppLocale) = if (locale == AppLocale.en) questionEN else questionNL
}

@Serializable
data class Journey(
    val id: String,
    val group: String,
    val titleNL: String,
    val titleEN: String,
    val teaserNL: String,
    val teaserEN: String,
    val steps: List<JourneyStep>
) {
    fun title(locale: AppLocale) = if (locale == AppLocale.en) titleEN else titleNL
    fun teaser(locale: AppLocale) = if (locale == AppLocale.en) teaserEN else teaserNL
}

object JourneyCatalog {
    private val json = Json { ignoreUnknownKeys = true }
    var all: List<Journey> = emptyList()
        private set

    fun load(context: Context) {
        val raw = context.assets.open("json/journeys.json").bufferedReader().readText()
        all = json.decodeFromString(raw)
    }

    fun find(id: String) = all.firstOrNull { it.id == id }

    fun grouped(): List<Pair<String, List<Journey>>> {
        val order = listOf("season", "story", "book")
        return order.mapNotNull { group ->
            val items = all.filter { it.group == group }
            if (items.isEmpty()) null else group to items
        }
    }

    fun groupTitle(group: String, locale: AppLocale) = when (group) {
        "season" -> if (locale == AppLocale.en) "Seasons" else "Seizoenen"
        "story" -> if (locale == AppLocale.en) "Stories" else "Verhalen"
        else -> if (locale == AppLocale.en) "A book" else "Een boek"
    }

    fun fromPlan(plan: ReadingPlan): Journey {
        val steps = plan.days.mapIndexed { index, day ->
            val previous = plan.days.getOrNull(index - 1)?.label
            val contextNL = if (previous != null) {
                "Gisteren was $previous. Vandaag ga je verder met ${day.label}. Het is de volgende bladzijde van hetzelfde plan, geen losse tekst."
            } else {
                "Hier begint het plan. ${day.label} is de eerste bladzijde. Wat je vandaag leest, draagt de dagen die volgen."
            }
            val contextEN = if (previous != null) {
                "Yesterday was $previous. Today you continue with ${day.label}. It is the next page of the same plan, not a loose text."
            } else {
                "The plan begins here. ${day.label} is the first page. What you read today carries the days that follow."
            }
            JourneyStep(
                bookId = day.bookId, chapter = day.chapter,
                titleNL = day.label, titleEN = day.label,
                contextNL = contextNL, contextEN = contextEN,
                questionNL = "Wat uit dit hoofdstuk neem je mee naar de volgende dag?",
                questionEN = "What from this chapter will you carry into the next day?"
            )
        }
        return Journey(
            id = plan.id, group = "book",
            titleNL = plan.title, titleEN = plan.titleEn ?: plan.title,
            teaserNL = plan.teaser, teaserEN = plan.teaserEn ?: plan.teaser,
            steps = steps
        )
    }
}
