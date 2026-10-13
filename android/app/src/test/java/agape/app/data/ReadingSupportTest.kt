package agape.app.data

import org.junit.Assert.*
import org.junit.Test

class ReadingSupportTest {
    private val book = BookMeta("johannes", "Johannes", "Joh.", 21, BookGroup.evangelie, Testament.nt)
    private val original = ChapterGuide("johannes", 3, "Old title", "Original summary", "Purpose", "Context", listOf("Theme"), emptyList(), emptyList(), "Interpretations", false)
    @Test fun concreteExplanationReplacesGenericProseAndProvidesVerseSections() {
        val section = GuideSection(1, 21, "Birth", "Explanation")
        val explanation = GuideExplanation("New title", "New summary", "New context", listOf(section), listOf("Question?"))
        val result = ReadingSupport.apply(original, book, AppLocale.en, explanation, true)
        assertEquals("New summary", result.summary)
        assertEquals(listOf(section), result.sections)
        assertFalse(result.readingAid)
        assertTrue(result.purpose.isEmpty())
    }
    @Test fun genericTextIsClearlyMarkedAsReadingSupportInBothLanguages() {
        for (locale in AppLocale.entries) {
            val result = ReadingSupport.apply(original, book, locale, null, true)
            assertTrue(result.readingAid)
            assertNotEquals(original.summary, result.summary)
            assertEquals(3, result.questions.size)
        }
    }
    @Test fun existingSpecificCommentaryIsPreserved() {
        val result = ReadingSupport.apply(original, book, AppLocale.nl, null, false)
        assertEquals(original.summary, result.summary)
        assertEquals("Johannes 3", result.title)
        assertFalse(result.readingAid)
    }
    @Test fun readingInstructionsReflectTheLiteraryGenre() {
        val prayer = book.copy(id = "psalmen", group = BookGroup.wijsheid)
        val epistle = book.copy(id = "romeinen", group = BookGroup.paulus)
        assertNotEquals(ReadingSupport.apply(original, prayer, AppLocale.nl, null, true).summary,
            ReadingSupport.apply(original, epistle, AppLocale.nl, null, true).summary)
    }
}
