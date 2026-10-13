package agape.app.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import java.util.TimeZone

class CalendarDatesTest {
    @Test fun leapMonthHasContinuousMondayAlignedGrid() {
        val cells = CalendarDates.month(YearMonth.of(2028, 2))
        assertEquals(42, cells.size)
        assertEquals(DayOfWeek.MONDAY, cells.first().dayOfWeek)
        assertEquals(29, cells.count { it.monthValue == 2 })
        assertTrue(cells.zipWithNext().all { (a, b) -> b == a.plusDays(1) })
    }
    @Test fun weekCrossesYearWithoutLosingDays() {
        val cells = CalendarDates.week(LocalDate.of(2027, 1, 1))
        assertEquals(LocalDate.of(2026, 12, 28), cells.first())
        assertEquals(LocalDate.of(2027, 1, 3), cells.last())
    }
    @Test fun rejectsImpossibleAndMalformedDates() {
        assertNull(ChurchCalendar.parseYmd("2026-02-30"))
        assertNull(ChurchCalendar.parseYmd("not-a-date"))
        assertNotNull(ChurchCalendar.parseYmd("2028-02-29"))
    }
    @Test fun dayAdditionSurvivesBothAmsterdamClockChanges() {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
            for ((month, day) in listOf(3 to 29, 10 to 25)) {
                val start = ChurchCalendar.civil(2026, month, day)
                val next = ChurchCalendar.addDays(start, 1)
                assertEquals(LocalDate.of(2026, month, day).plusDays(1).toString(), ChurchCalendar.ymd(next))
                assertEquals(start, ChurchCalendar.addDays(next, -1))
            }
        } finally { TimeZone.setDefault(previous) }
    }
    @Test fun paschaMatchesPublishedDates2026To2030() {
        val dates = listOf("2026-04-12", "2027-05-02", "2028-04-16", "2029-04-08", "2030-04-28")
        dates.forEach { assertEquals(it, ChurchCalendar.ymd(ChurchCalendar.orthodoxPascha(it.take(4).toInt()))) }
    }
    @Test fun oldCalendarNativityPreparationContinuesIntoJanuary() {
        assertTrue(ChurchCalendar.journeyGate("reis-advent", CalendarRite.julian, AppLocale.nl, ChurchCalendar.civil(2027, 1, 5))!!.open)
        assertFalse(ChurchCalendar.journeyGate("reis-advent", CalendarRite.julian, AppLocale.nl, ChurchCalendar.civil(2027, 1, 7))!!.open)
    }
    @Test fun christmasJourneyAndCalendarConversionHandleYearBoundary() {
        assertTrue(ChurchCalendar.journeyGate("reis-kerst", CalendarRite.julian, AppLocale.nl, ChurchCalendar.civil(2027, 1, 7))!!.open)
        assertEquals(12 to 25, ChurchCalendar.liturgicalMonthDay(ChurchCalendar.civil(2027, 1, 7), CalendarRite.julian))
    }
}
