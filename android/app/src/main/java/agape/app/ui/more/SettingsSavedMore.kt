package agape.app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.CalendarRite
import agape.app.data.ChapterRef
import agape.app.data.NightMode
import agape.app.data.Tradition
import agape.app.data.TranslationID
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.Haptics
import agape.app.ui.components.AgapeButton
import agape.app.ui.components.AgapeButtonKind
import agape.app.ui.components.AgapeCard
import agape.app.ui.components.EmptyStateView
import agape.app.ui.components.GlassScrollMenu
import agape.app.ui.components.Kicker
import agape.app.ui.components.appColumn
import agape.app.ui.components.dotColor
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.openReader

@Composable
fun SettingsView(nav: NavHostController) {
    val store = LocalStore.current
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Kicker(store.t("settings"))
        Text(store.t("settings"), style = AppType.display(36.sp), color = AppColor.fg())
        group(store.t("language")) {
            GlassScrollMenu(listOf("nl" to "Nederlands", "en" to "English"), store.state.locale.name) { store.applyLocale(it) }
        }
        group(store.t("localProfile")) {
            Text(store.state.profileName.ifBlank { store.t("profileUnnamed") }, style = AppType.display(24.sp), color = AppColor.fg())
            Text(store.t("profilePrivacy"), style = AppType.sans(15.sp), color = AppColor.muted())
            AgapeButton(store.t("editProfile"), AgapeButtonKind.outline) { nav.navigate("profile") }
        }
        group(store.t("translation")) {
            val options = store.catalog.translationsFor(store.state.locale)
            if (store.state.locale.name == "nl") {
                GlassScrollMenu(options.map { it.id to it.short }, store.state.translation.name) {
                    store.update { copy(translation = TranslationID.valueOf(it)) }; store.persist()
                }
            }
            options.forEach { tr ->
                Text("${store.catalog.translationName(tr, store.state.locale)} · ${tr.year}. ${store.catalog.translationTeaser(tr, store.state.locale)}", style = AppType.sans(13.sp), color = AppColor.muted())
            }
        }
        group(store.t("fontSize")) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AgapeButton(store.t("smaller"), AgapeButtonKind.outline) { store.setFont(-0.08) }
                AgapeButton(store.t("larger"), AgapeButtonKind.outline) { store.setFont(0.08) }
            }
            Text(store.t("fontPreview"), style = AppType.serif((19 * store.state.fontScale).sp), color = AppColor.fg(), lineHeight = (25 * store.state.fontScale * store.state.lineHeight).sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(store.t("lineSpacing"), style = AppType.sans(14.sp), color = AppColor.muted(), modifier = Modifier.weight(1f))
                Slider(
                    modifier = Modifier.weight(1f),
                    value = store.state.lineHeight.toFloat(),
                    onValueChange = { v -> store.update { copy(lineHeight = v.toDouble()) }; store.persist() },
                    valueRange = 0.85f..1.35f, steps = 9,
                    colors = SliderDefaults.colors(thumbColor = AppColor.accent(), activeTrackColor = AppColor.accent())
                )
            }
            ToggleRow(store.t("verseNumbers"), store.state.verseNumbers) { store.update { copy(verseNumbers = it) }; store.persist(); Haptics.light(context) }
        }
        group(store.t("reading")) {
            GlassScrollMenu(
                listOf("auto" to store.t("autoNight"), "day" to store.t("dayMode"), "night" to store.t("night")),
                store.state.nightMode.name
            ) { store.update { copy(nightMode = NightMode.valueOf(it)) }; store.persist() }
            ToggleRow(store.t("redLetter"), store.state.redLetter) { store.update { copy(redLetter = it) }; store.persist(); Haptics.light(context) }
            ToggleRow(store.t("haptics"), store.state.hapticsEnabled) {
                store.update { copy(hapticsEnabled = it) }; Haptics.isEnabled = it; store.persist()
            }
        }
        group(store.t("calendarRite")) {
            GlassScrollMenu(listOf("julian" to store.t("julian"), "gregorian" to store.t("gregorian")), store.state.calendarRite.name) {
                store.update { copy(calendarRite = CalendarRite.valueOf(it)) }; store.persist()
            }
        }
        group(store.t("prayerTimes")) {
            Text(store.t("prayerTimesLead"), style = AppType.sans(15.sp), color = AppColor.muted())
            ToggleRow(store.t("prayerTimes"), store.state.prayerAlarms) {
                store.update { copy(prayerAlarms = it) }; store.persist(); store.refreshAlarms(); Haptics.light(context)
            }
            store.catalog.hours.forEach { hour ->
                ToggleRow(store.catalog.hourTitle(hour, store.state.locale), store.state.alarmHours[hour.id] ?: hour.alarm) {
                    store.update { copy(alarmHours = alarmHours + (hour.id to it)) }; store.persist(); store.refreshAlarms()
                }
            }
        }
        group(store.t("ticks")) {
            Text(store.t("ticksLead"), style = AppType.sans(15.sp), color = AppColor.muted())
            Text("${store.chaptersReadCount()} / ${store.totalChapters()}", style = AppType.display(22.sp), color = AppColor.fg())
            if (confirmClear) {
                AgapeButton(store.t("clearTicksYes"), AgapeButtonKind.primary) { store.clearRead(); confirmClear = false }
                AgapeButton(store.t("clearTicksNo"), AgapeButtonKind.outline) { confirmClear = false }
            } else {
                AgapeButton(store.t("clearTicks"), AgapeButtonKind.outline) { confirmClear = true }
            }
        }
        group(store.t("about")) {
            Text(store.t("aboutBody"), style = AppType.sans(15.sp), color = AppColor.muted())
            Text(store.t("aboutLocal"), style = AppType.sans(15.sp), color = AppColor.muted())
            Text(store.t("more"), color = AppColor.accent(), modifier = Modifier.clickable { nav.navigate("more") })
        }
    }
}

@Composable
private fun group(title: String, content: @Composable () -> Unit) {
    AgapeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title.uppercase(), style = AppType.sans(12.sp).copy(letterSpacing = 1.4.sp), color = AppColor.muted())
            content()
        }
    }
}

@Composable
private fun ToggleRow(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = AppType.sans(16.sp), color = AppColor.fg(), modifier = Modifier.weight(1f))
        Switch(value, onChange, colors = SwitchDefaults.colors(checkedTrackColor = AppColor.accent()))
    }
}

@Composable
fun SavedView(nav: NavHostController) {
    val store = LocalStore.current
    var tab by remember { mutableStateOf("saved") }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(store.t("saved"))
        Text(store.t("saved"), style = AppType.display(34.sp), color = AppColor.fg())
        GlassScrollMenu(listOf("saved" to store.t("saved"), "marks" to store.t("highlights"), "notes" to store.t("notes")), tab) { tab = it }
        when (tab) {
            "saved" -> {
                if (store.state.saved.isEmpty()) EmptyStateView(store.t("saved"), store.t("bookmarksEmpty"), Icons.Outlined.BookmarkBorder)
                store.state.saved.forEach { item ->
                    BoxClick({ nav.openReader(ChapterRef(item.bookId, item.chapter, item.verse)) }) {
                        AgapeCard {
                            Text("${store.bookName(item.bookId)} ${item.chapter}:${item.verse}", style = AppType.display(20.sp), color = AppColor.accent())
                            Text(store.displayedVerse(item.bookId, item.chapter, item.verse, item.text), style = AppType.serif(17.sp), color = AppColor.fg(), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            "marks" -> {
                val items = store.state.snippets.values.sortedBy { it.key }
                if (items.isEmpty()) EmptyStateView(store.t("highlights"), store.t("highlightsEmpty"), Icons.Outlined.EditNote)
                items.forEach { item ->
                    BoxClick({ nav.openReader(ChapterRef(item.bookId, item.chapter, item.verse)) }) {
                        AgapeCard {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                androidx.compose.foundation.layout.Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(store.state.colors[item.key]?.dotColor() ?: AppColor.dotGold))
                                Text("${store.bookName(item.bookId)} ${item.chapter}:${item.verse}", style = AppType.display(20.sp), color = AppColor.fg())
                            }
                            Text(store.displayedVerse(item.bookId, item.chapter, item.verse, item.text), style = AppType.serif(17.sp), color = AppColor.muted(), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            else -> {
                val notes = store.state.notes.keys.sorted()
                if (notes.isEmpty()) EmptyStateView(store.t("notes"), store.t("allNotesEmpty"), Icons.Outlined.EditNote)
                notes.forEach { key ->
                    val parts = key.split(":")
                    if (parts.size == 3) {
                        val ref = ChapterRef(parts[0], parts[1].toIntOrNull() ?: 1, parts[2].toIntOrNull())
                        BoxClick({ nav.openReader(ref) }) {
                            AgapeCard {
                                Text("${store.bookName(ref.bookId)} ${ref.chapter}:${ref.verse ?: 0}", style = AppType.display(20.sp), color = AppColor.accent())
                                Text(store.state.notes[key].orEmpty(), style = AppType.serif(17.sp), color = AppColor.fg(), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoreView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker(store.t("more"))
        Text("AGAPE", style = AppType.display(36.sp).copy(letterSpacing = 6.sp), color = AppColor.fg())
        Text("${store.chaptersReadCount()} / ${store.totalChapters()} · ${store.t("streak")} ${store.state.streak}", style = AppType.sans(15.sp), color = AppColor.muted())
        moreRow(store.t("calendar")) { nav.navigate("calendarHome") }
        moreRow(store.t("readings")) { nav.navigate("readings") }
        moreRow(store.t("prayer")) { nav.navigate("prayerHome") }
        moreRow(store.t("agpeya")) { nav.navigate("agpeya") }
        moreRow(store.t("readingPlan")) { nav.navigate("plans") }
        moreRow(store.t("saved")) { nav.navigate("saved") }
        moreRow(store.t("topics")) { nav.navigate("topics") }
        moreRow(store.t("verses")) { nav.navigate("quotes") }
        moreRow(store.t("church")) { nav.navigate("church/root") }
        moreRow(store.t("harmony")) { nav.navigate("harmony") }
        moreRow(store.t("fathers")) { nav.navigate("fathers") }
        moreRow(store.t("glossary")) { nav.navigate("glossary") }
        moreRow(store.t("settings")) { nav.navigate("settings") }
    }
}

@Composable
private fun moreRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.largeShape()).background(AppColor.surface()).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = AppType.display(24.sp), color = AppColor.fg(), modifier = Modifier.weight(1f).padding(vertical = 12.dp))
        Icon(Icons.Outlined.ChevronRight, null, tint = AppColor.faint())
    }
}
