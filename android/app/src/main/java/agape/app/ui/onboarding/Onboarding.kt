package agape.app.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.data.CalendarRite
import agape.app.data.Tradition
import agape.app.data.TranslationID
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.ui.components.AgapeButton
import agape.app.ui.components.AgapeButtonKind
import agape.app.ui.components.AgapeCard
import agape.app.ui.components.ArtView
import agape.app.ui.components.GlassScrollMenu
import agape.app.ui.components.Kicker
import agape.app.ui.components.appColumn
import kotlinx.coroutines.launch

@Composable
fun IntroView() {
    val store = LocalStore.current
    val pages = listOf(
        Triple("galilee", store.t("introKicker") to "AGAPE", store.t("introLead")),
        Triple("church", store.t("bible") to store.t("bible"), store.t("introBible")),
        Triple("tabor", store.t("church") to store.t("church"), store.t("introChurch")),
        Triple("night", store.t("prayer") to store.t("prayer"), store.t("introPrayer")),
        Triple("wilderness", store.t("calendar") to store.t("calendar"), store.t("introPlan"))
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(AppColor.bg())) {
        HorizontalPager(pager, Modifier.weight(1f)) { index ->
            val item = pages[index]
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(280.dp)) {
                    ArtView(item.first, Modifier.fillMaxSize())
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, AppColor.bg()), startY = 120f)
                        )
                    )
                }
                Column(Modifier.appColumn().padding(top = AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    if (index == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(store.t("languagePulse"), style = AppType.sans(12.sp).copy(letterSpacing = 1.8.sp), color = AppColor.muted())
                            GlassScrollMenu(
                                listOf("en" to "English", "nl" to "Nederlands"),
                                store.state.locale.name
                            ) { store.applyLocale(it) }
                        }
                    }
                    Kicker(item.second.first)
                    Text(item.second.second, style = AppType.display(34.sp), color = AppColor.fg())
                    Text(item.third, style = AppType.serif(18.sp), color = AppColor.accent(), lineHeight = 23.sp)
                }
            }
        }
        Column(Modifier.appColumn().padding(top = AppSpacing.sm, bottom = AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.indices.forEach { i ->
                    val w by animateDpAsState(if (i == pager.currentPage) 18.dp else 6.dp, label = "dot")
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(w)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == pager.currentPage) AppColor.accent() else AppColor.fg().copy(0.22f))
                    )
                }
            }
            AgapeButton(
                title = if (pager.currentPage == pages.lastIndex) store.t("introContinue") else store.t("introNext"),
                kind = AgapeButtonKind.primary
            ) {
                if (pager.currentPage == pages.lastIndex) {
                    store.update { copy(seenIntro = true) }
                    store.persist()
                } else {
                    scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }
            }
        }
    }
}

@Composable
fun SetupView() {
    val store = LocalStore.current
    Column(
        Modifier
            .fillMaxSize()
            .background(AppColor.bg()).safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .appColumn()
            .padding(vertical = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Kicker(store.t("setupTitle"))
        Text(store.t("setupLead"), style = AppType.serif(20.sp), color = AppColor.accent())
        setupSection(store.t("calendarRite"), store.t("calendarHint")) {
            GlassScrollMenu(
                listOf("julian" to store.t("julian"), "gregorian" to store.t("gregorian")),
                store.state.calendarRite.name
            ) {
                store.update { copy(calendarRite = CalendarRite.valueOf(it)) }
                store.persist()
            }
        }
        if (store.state.locale.name == "nl") {
            setupSection(store.t("translation"), null) {
                GlassScrollMenu(
                    listOf("sv" to "SV", "hsv" to "SV · modern"),
                    store.state.translation.name
                ) {
                    store.update { copy(translation = TranslationID.valueOf(it)) }
                    store.persist()
                }
            }
        } else {
            setupSection(store.t("translation"), null) {
                Text(store.t("kjvLead"), style = AppType.sans(15.sp, FontWeight.Normal), color = AppColor.muted())
            }
        }
        AgapeButton(store.t("setupContinue"), AgapeButtonKind.primary) {
            store.update { copy(seenSetup = true) }
            store.persist()
            store.refreshAlarms()
        }
    }
}

@Composable
private fun setupSection(title: String, lead: String?, content: @Composable () -> Unit) {
    AgapeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title.uppercase(), style = AppType.sans(12.sp).copy(letterSpacing = 1.6.sp), color = AppColor.muted())
            if (lead != null) Text(lead, style = AppType.sans(15.sp, FontWeight.Normal), color = AppColor.muted())
            content()
        }
    }
}
