package agape.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

object CalendarDates {
    fun month(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L until 42L).map { first.plusDays(it) }
    }
    fun week(date: LocalDate): List<LocalDate> {
        val first = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0L until 7L).map { first.plusDays(it) }
    }
}
