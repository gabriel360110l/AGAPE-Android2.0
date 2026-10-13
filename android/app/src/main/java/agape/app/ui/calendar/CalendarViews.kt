package agape.app.ui.calendar

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import agape.app.data.*
import agape.app.store.LocalStore
import agape.app.theme.*
import agape.app.ui.components.*
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.openReader
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun LocalDate.millis() = ChurchCalendar.civil(year, monthValue, dayOfMonth)
private fun AppLocale.dateLocale() = if (this == AppLocale.en) Locale.UK else Locale("nl", "NL")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHomeView(nav: NavHostController) {
    val store = LocalStore.current
    val context = LocalContext.current
    val today = LocalDate.now()
    var selectedKey by rememberSaveable { mutableStateOf(today.toString()) }
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    val selected = LocalDate.parse(selectedKey)
    val month = YearMonth.parse(monthKey)
    val locale = store.state.locale
    val rite = store.state.calendarRite
    val week = store.state.calendarView == "week"
    val cells = remember(monthKey, selectedKey, week) {
        if (week) CalendarDates.week(selected) else CalendarDates.month(month)
    }
    val days = remember(cells, rite, locale) { cells.map { ChurchCalendar.calendarFor(it.millis(), rite, locale) } }
    val detail = remember(selectedKey, rite, locale) { ChurchCalendar.calendarFor(selected.millis(), rite, locale) }
    val upcoming = remember(today, rite, locale) {
        (1..90).map { ChurchCalendar.calendarFor(today.plusDays(it.toLong()).millis(), rite, locale) }
            .filter { it.rank == "great" || it.rank == "major" }.distinctBy { it.title }.take(5)
    }
    fun select(date: LocalDate) { selectedKey = date.toString(); monthKey = YearMonth.from(date).toString() }
    fun move(amount: Long) {
        val date = if (week) selected.plusWeeks(amount) else month.plusMonths(amount).atDay(1)
        if (date.year in 1901..2099) select(date)
    }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn(spacing = 24.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Kicker(store.t("churchYear"))
                    Text(store.t("calendar"), style = AppType.display(34.sp), color = AppColor.fg())
                    Text(store.t("calendarLead"), style = AppType.sans(15.sp), color = AppColor.muted(), lineHeight = 22.sp)
                }
                IconButton(onClick = { showInfo = true }) { Icon(Icons.Outlined.Info, store.t("calendarInfo"), tint = AppColor.accent()) }
            }
            GlassScrollMenu(listOf("month" to store.t("monthView"), "week" to store.t("weekView")), store.state.calendarView) {
                store.update { copy(calendarView = it) }
            }
            AgapeCard(padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { move(-1) }, enabled = if (week) selected.minusWeeks(1).year >= 1901 else month.minusMonths(1).year >= 1901) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, store.t("previousPeriod"), tint = AppColor.accent())
                        }
                        TextButton(onClick = {
                            DatePickerDialog(context, { _, y, m, d -> select(LocalDate.of(y, m + 1, d)) },
                                selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
                                datePicker.minDate = ChurchCalendar.civil(1901, 1, 1)
                                datePicker.maxDate = ChurchCalendar.civil(2099, 12, 31)
                            }.show()
                        }, modifier = Modifier.weight(1f)) {
                            Text(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", locale.dateLocale())).replaceFirstChar { it.uppercase() },
                                style = AppType.serif(20.sp), textAlign = TextAlign.Center, color = AppColor.fg())
                        }
                        IconButton(onClick = { move(1) }, enabled = if (week) selected.plusWeeks(1).year <= 2099 else month.plusMonths(1).year <= 2099) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, store.t("nextPeriod"), tint = AppColor.accent())
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        DayOfWeek.entries.forEach { day ->
                            Text(day.getDisplayName(java.time.format.TextStyle.SHORT, locale.dateLocale()).take(2),
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = AppType.sans(12.sp), color = AppColor.muted())
                        }
                    }
                    cells.chunked(7).forEachIndexed { row, dates ->
                        Row(Modifier.fillMaxWidth()) {
                            dates.forEachIndexed { col, date ->
                                val item = days[row * 7 + col]
                                val chosen = date == selected
                                val isToday = date == today
                                val inMonth = YearMonth.from(date) == month
                                val fg = if (chosen) AppColor.accentFg() else if (inMonth || week) AppColor.fg() else AppColor.faint()
                                val label = Readings.formatDate(date.millis(), locale) + ", " + item.title + if (isToday) ", " + store.t("today") else ""
                                Column(Modifier.weight(1f).heightIn(min = 52.dp).clip(AppRadius.mediumShape())
                                    .background(if (chosen) AppColor.accent() else Color.Transparent)
                                    .then(if (isToday && !chosen) Modifier.border(1.dp, AppColor.accent(), AppRadius.mediumShape()) else Modifier)
                                    .semantics(mergeDescendants = true) { this.selected = chosen; contentDescription = label }
                                    .clickable(enabled = date.year in 1901..2099, role = Role.Button) { select(date); Haptics.select(context) }
                                    .padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text("${date.dayOfMonth}", color = fg, style = AppType.sans(15.sp, if (isToday || chosen) FontWeight.Bold else FontWeight.Medium))
                                    Box(Modifier.size(4.dp).clip(CircleShape).background(
                                        if (item.rank in listOf("great", "major")) fg else Color.Transparent))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("• " + store.t("calendarFeastDot"), modifier = Modifier.weight(1f), style = AppType.sans(12.sp), color = AppColor.muted())
                        TextButton(onClick = { select(today) }) { Text(store.t("today")) }
                    }
                }
            }
            CalendarDayCard(detail)
            SuggestedReadings(nav, selected.millis())
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(store.t("upcomingFeasts"))
                Text(store.t("upcoming90Days"), style = AppType.sans(13.sp), color = AppColor.muted())
                if (upcoming.isEmpty()) Text(store.t("noUpcomingFeasts"), style = AppType.sans(15.sp), color = AppColor.muted())
                upcoming.forEach { day ->
                    BoxClick({ select(LocalDate.parse(day.ymd)) }) {
                        AgapeCard(padding = 16.dp) {
                            Text(Readings.formatDate(day.dateMillis, locale), style = AppType.sans(12.sp), color = AppColor.accent())
                            Text(day.title, style = AppType.serif(20.sp), color = AppColor.fg(), modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }
    }
    if (showInfo) ModalBottomSheet(onDismissRequest = { showInfo = false }, containerColor = AppColor.surface()) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(store.t("calendarInfo"), style = AppType.display(26.sp), color = AppColor.fg())
            Text(store.t("calendarInfoBody"), style = AppType.sans(16.sp), color = AppColor.fg(), lineHeight = 25.sp)
            GlassScrollMenu(listOf("julian" to store.t("oldCalendar"), "gregorian" to store.t("newCalendar")), rite.name) {
                store.update { copy(calendarRite = CalendarRite.valueOf(it)) }
            }
            Text(store.t(if (rite == CalendarRite.julian) "riteBodyJulian" else "riteBodyGregorian"),
                style = AppType.sans(15.sp), color = AppColor.muted(), lineHeight = 23.sp)
            Text(store.t("readingsHint"), style = AppType.sans(14.sp), color = AppColor.muted(), lineHeight = 22.sp)
            TextButton(onClick = { showInfo = false }) { Text(store.t("close")) }
        }
    }
}

@Composable
private fun CalendarDayCard(day: CalendarDay) {
    val store = LocalStore.current
    AgapeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Kicker(Readings.formatDate(day.dateMillis, store.state.locale), AppColor.accent())
            Text(day.title, style = AppType.display(26.sp), color = AppColor.fg())
            Text(day.menologionLabel, style = AppType.sans(13.sp), color = AppColor.muted())
            if (day.season.isNotBlank()) Text(day.season, style = AppType.sans(14.sp), color = AppColor.accent())
            if (day.teaser.isNotBlank()) Text(day.teaser, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 26.sp)
            if (day.explanation.isNotBlank() && day.explanation != day.teaser)
                Text(day.explanation, style = AppType.sans(15.sp), color = AppColor.muted(), lineHeight = 23.sp)
        }
    }
}

@Composable
private fun SuggestedReadings(nav: NavHostController, date: Long) {
    val store = LocalStore.current
    val readings = remember(date) { Readings.forDay(date) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(store.t("readings"))
        Text(store.t("readingsHint"), style = AppType.sans(13.sp), color = AppColor.muted(), lineHeight = 20.sp)
        AgapeCard(padding = 16.dp) {
            listOf("lawProphets" to readings.first, "gospel" to readings.second, "apostle" to readings.third).forEachIndexed { index, (label, ref) ->
                BoxClick({ nav.openReader(ref) }) {
                    ReadingRow(store.t(label), "${store.bookName(ref.bookId)} ${ref.chapter}", store.isRead(ref.bookId, ref.chapter), index < 2)
                }
            }
        }
    }
}

@Composable
fun CalendarDayView(nav: NavHostController, ymd: String) {
    val store = LocalStore.current
    val date = ChurchCalendar.parseYmd(ymd) ?: System.currentTimeMillis()
    val day = ChurchCalendar.calendarFor(date, store.state.calendarRite, store.state.locale)
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn { CalendarDayCard(day); SuggestedReadings(nav, date) }
    }
}

@Composable
fun ReadingsView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader(store.t("today"), store.t("readings"))
            SuggestedReadings(nav, System.currentTimeMillis())
        }
    }
}
