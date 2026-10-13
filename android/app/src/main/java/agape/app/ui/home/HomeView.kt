package agape.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.ChapterRef
import agape.app.data.PrayerPoints
import agape.app.data.Readings
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.AppTypography
import agape.app.theme.LocalReduceMotion
import agape.app.ui.components.AppListRow
import agape.app.ui.components.AppScreenColumn
import agape.app.ui.components.GlassIconButton
import agape.app.ui.components.HeroBand
import agape.app.ui.components.Kicker
import agape.app.ui.components.PrimaryChip
import agape.app.ui.components.ReadingRow
import agape.app.ui.components.SectionHeader
import agape.app.ui.components.StatRow
import agape.app.ui.navigation.encode
import agape.app.ui.navigation.openReader
import android.content.Intent

@Composable
fun HomeView(nav: NavHostController, onImmersive: (Boolean) -> Unit = {}) {
    val store = LocalStore.current
    val reduce = LocalReduceMotion.current
    var heroReady by remember { mutableStateOf(false) }
    val quote = store.catalog.quoteOfDay()
    val quoteText = store.displayedVerse(quote.bookId, quote.chapter, quote.verse, quote.text)
    val readings = Readings.forDay()
    val prayer = PrayerPoints.ofDay()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        store.refreshPrayerDay()
        heroReady = true
        onImmersive(false)
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppColor.bg())
            .verticalScroll(rememberScrollState())
    ) {
        HeroBand(store.catalog.verseBackground(), minHeight = 400.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                Row {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        Kicker(store.t("today"), AppColor.onPhoto.copy(0.78f))
                        if (store.state.profileName.isNotBlank()) Text(
                            store.t("welcomeName") + " " + store.state.profileName,
                            style = AppType.serif(24.sp), color = AppColor.onPhoto
                        )
                        Text(
                            Readings.formatDate(System.currentTimeMillis(), store.state.locale).replaceFirstChar { it.uppercase() },
                            style = AppTypography.secondary(),
                            color = AppColor.onPhoto.copy(0.92f)
                        )
                    }
                    GlassIconButton(Icons.Outlined.HelpOutline, if (store.state.locale == agape.app.data.AppLocale.en) "How to pray" else "Hoe je bidt", onPhoto = true) {
                        nav.navigate("prayHelp")
                    }
                    GlassIconButton(Icons.Outlined.Settings, store.t("settings"), onPhoto = true) {
                        nav.navigate("settings")
                    }
                }
                Text("“$quoteText”", style = AppType.serif(26.sp), color = AppColor.onPhoto, lineHeight = 30.sp)
                Text(
                    store.displayedRef(quote.bookId, quote.chapter, quote.verse),
                    style = AppTypography.headline(),
                    color = AppColor.onPhoto
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.button)) {
                    Box(Modifier.clickable { nav.openReader(ChapterRef(quote.bookId, quote.chapter, quote.verse)) }) {
                        PrimaryChip(store.t("read"), filled = true, onPhoto = true)
                    }
                    Box(Modifier.clickable {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "“$quoteText” — ${store.displayedRef(quote.bookId, quote.chapter, quote.verse)}")
                        }
                        context.startActivity(Intent.createChooser(share, store.t("share")))
                    }) {
                        PrimaryChip(store.t("share"), filled = false, onPhoto = true)
                    }
                }
            }
        }
        AppScreenColumn {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                SectionHeader(store.t("lezen"))
                val last = store.state.last
                if (last != null && store.catalog.book(last.bookId) != null) {
                    BoxClick({ nav.openReader(last) }) {
                        AppListRow(store.t("openBible"), "${store.bookName(last.bookId)} ${last.chapter}", showSeparator = false)
                    }
                } else {
                    BoxClick({ nav.openReader(store.catalog.firstChapter(store.state.tradition)) }) {
                        AppListRow(store.t("openBible"), showSeparator = false)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                SectionHeader(store.t("progress"))
                StatRow(
                    listOf(
                        "${store.state.streak}" to store.t("streak"),
                        "${store.chaptersReadCount()}/${store.totalChapters()}" to store.t("chaptersRead"),
                        "${store.state.prayerTodayCount}" to store.t("prayerCount")
                    )
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                SectionHeader(store.t("readings"), store.t("allReadings")) { nav.navigate("readings") }
                Column {
                    BoxClick({ nav.openReader(readings.first) }) {
                        ReadingRow(store.t("lawProphets"), "${store.bookName(readings.first.bookId)} ${readings.first.chapter}", store.isRead(readings.first.bookId, readings.first.chapter), true)
                    }
                    BoxClick({ nav.openReader(readings.second) }) {
                        ReadingRow(store.t("gospel"), "${store.bookName(readings.second.bookId)} ${readings.second.chapter}", store.isRead(readings.second.bookId, readings.second.chapter), true)
                    }
                    BoxClick({ nav.openReader(readings.third) }) {
                        ReadingRow(store.t("apostle"), "${store.bookName(readings.third.bookId)} ${readings.third.chapter}", store.isRead(readings.third.bookId, readings.third.chapter), false)
                    }
                }
            }
            agape.app.ui.journey.JourneyHomeSection(nav)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                SectionHeader(store.t("prayer"), store.t("all")) { nav.navigate("prayerHome") }
                BoxClick({ nav.navigate("prayerPoint/${prayer.id}") }) {
                    AppListRow(prayer.title(store.state.locale), store.t("prayerOfDay"), showSeparator = false)
                }
            }
            if (store.state.history.isNotEmpty() || store.state.saved.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                    SectionHeader(store.t("recent"))
                    val last = store.state.last
                    if (last != null && store.catalog.book(last.bookId) != null) {
                        BoxClick({ nav.openReader(last) }) {
                            AppListRow(store.t("continue"), "${store.bookName(last.bookId)} ${last.chapter}")
                        }
                    }
                    store.state.history.take(4).forEach { item ->
                        BoxClick({ nav.openReader(ChapterRef(item.bookId, item.chapter)) }) {
                            AppListRow("${store.bookName(item.bookId)} ${item.chapter}")
                        }
                    }
                    if (store.state.saved.isNotEmpty()) {
                        BoxClick({ nav.navigate("saved") }) {
                            AppListRow(store.t("saved"), "${store.state.saved.size}", showSeparator = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoxClick(onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(agape.app.theme.AppRadius.mediumShape()).clickable(onClick = onClick)) { content() }
}
