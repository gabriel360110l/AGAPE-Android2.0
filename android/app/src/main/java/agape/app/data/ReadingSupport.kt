package agape.app.data

import kotlinx.serialization.Serializable

@Serializable
data class GuideSection(val start: Int, val end: Int, val title: String, val body: String)

@Serializable
data class GuideExplanation(
    val title: String, val summary: String, val context: String,
    val sections: List<GuideSection>, val questions: List<String>
)

@Serializable
data class BilingualExplanation(val bookId: String, val chapter: Int, val nl: GuideExplanation, val en: GuideExplanation)

object ReadingSupport {
    fun apply(guide: ChapterGuide, book: BookMeta, locale: AppLocale, explanation: GuideExplanation?, generic: Boolean): ChapterGuide {
        if (explanation != null) return guide.copy(
            title = explanation.title, summary = explanation.summary, context = explanation.context,
            purpose = "", themes = emptyList(), interpretations = "",
            sections = explanation.sections, questions = explanation.questions
        )
        if (!generic) return guide.copy(title = if (locale == AppLocale.nl) "${book.name} ${guide.chapter}" else guide.title)
        val en = locale == AppLocale.en
        val guidance = when {
            book.id == "psalmen" || book.id == "psalm151" || book.id == "gebed-manasse" -> if (en)
                "Read this prayer slowly. Notice who speaks, what is addressed to God, and whether lament, gratitude or trust changes during the poem. Poetic images express experience; they are not always literal promises."
                else "Lees dit gebed langzaam. Let op wie spreekt, wat tot God wordt gezegd en of klacht, dankbaarheid of vertrouwen veranderen. Poëtische beelden verwoorden ervaringen; ze zijn niet altijd letterlijke beloften."
            book.group == BookGroup.wijsheid || book.id == "wijsheid" || book.id == "sirach" -> if (en)
                "Look for comparisons, contrasts and recurring words. Wisdom texts may offer observations, questions or dialogue rather than a rule for every situation. In a dialogue, distinguish the speaker’s view from the conclusion of the whole book."
                else "Let op vergelijkingen, tegenstellingen en terugkerende woorden. Wijsheidsteksten geven waarnemingen, vragen of gesprekken, niet steeds een regel voor elke situatie. Onderscheid in een dialoog de mening van een spreker van de conclusie van het hele boek."
            book.group == BookGroup.groteProfeten || book.group == BookGroup.kleineProfeten -> if (en)
                "Ask who is being addressed and what situation the message confronts. Distinguish accusation, warning, lament and hope. Read symbolic language within the surrounding passages before linking it to events outside the text."
                else "Vraag wie wordt aangesproken en op welke situatie de boodschap ingaat. Onderscheid aanklacht, waarschuwing, klacht en hoop. Lees beeldspraak samen met de omliggende gedeelten voordat je haar verbindt met gebeurtenissen buiten de tekst."
            book.group == BookGroup.evangelie -> if (en)
                "Follow the encounter or teaching step by step. Who speaks to Jesus, how does he respond, and how do the listeners react? In a parable, look for the main contrast and its audience before assigning a meaning to every detail."
                else "Volg de ontmoeting of het onderwijs stap voor stap. Wie spreekt Jezus aan, hoe reageert Hij en wat doen de luisteraars? Zoek bij een gelijkenis eerst het belangrijkste contrast en het publiek, voordat je elk detail een betekenis geeft."
            book.group == BookGroup.paulus || book.group == BookGroup.katholiek -> if (en)
                "Follow the argument across paragraph boundaries. Words such as therefore, but and because connect the reasoning. Ask what situation in the community is being addressed and how the practical advice follows from the preceding message."
                else "Volg het betoog over de grenzen van alinea’s heen. Woorden als daarom, maar en want verbinden de redenering. Vraag welke situatie in de gemeente wordt besproken en hoe de praktische raad voortkomt uit het voorafgaande."
            book.group == BookGroup.openbaring -> if (en)
                "Read the visions as symbolic language addressed to communities called to remain faithful. Notice images of worship, judgement and hope. Compare repeated symbols within the book; avoid treating each image as a date or modern headline."
                else "Lees de visioenen als beeldtaal voor gemeenten die worden opgeroepen trouw te blijven. Let op aanbidding, oordeel en hoop. Vergelijk terugkerende symbolen binnen het boek; behandel niet elk beeld als een datum of een hedendaags nieuwsbericht."
            book.group == BookGroup.wet -> if (en)
                "Identify whether this passage tells a story, gives a command or describes worship. For a command, ask who receives it and how it relates to the covenant. A narrated action is not automatically an example to imitate."
                else "Bepaal of dit gedeelte een verhaal vertelt, een gebod geeft of de eredienst beschrijft. Vraag bij een gebod wie het ontvangt en hoe het bij het verbond hoort. Een beschreven daad is niet automatisch een voorbeeld om na te volgen."
            else -> if (en)
                "Follow the people, setting and sequence of events. Distinguish what the narrator reports from what a character claims. Ask what changes by the end and read the preceding and following chapter to understand the wider story."
                else "Volg de personen, de plaats en de volgorde van gebeurtenissen. Onderscheid wat de verteller beschrijft van wat een personage beweert. Vraag wat aan het einde veranderd is en lees het vorige en volgende hoofdstuk voor het grotere verhaal."
        }
        return guide.copy(
            title = if (en) "Reading support" else "Hulp bij het lezen",
            summary = guidance, purpose = "", context = "", themes = emptyList(), interpretations = "",
            questions = if (en) listOf("Which words or contrasts recur?", "What becomes clearer when you read the surrounding passages?", "What question or prayer would you like to take with you?")
                else listOf("Welke woorden of tegenstellingen keren terug?", "Wat wordt duidelijker als je de omliggende gedeelten leest?", "Welke vraag of welk gebed wil je meenemen?"),
            readingAid = true
        )
    }
}
