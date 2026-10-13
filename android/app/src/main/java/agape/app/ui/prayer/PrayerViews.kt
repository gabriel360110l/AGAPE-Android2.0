package agape.app.ui.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.GuidedPrayer
import agape.app.data.PrayerPointGroup
import agape.app.data.PrayerPoints
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.AppTypography
import agape.app.ui.components.AgapeButton
import agape.app.ui.components.AgapeButtonKind
import agape.app.ui.components.AppDivider
import agape.app.ui.components.AppListRow
import agape.app.ui.components.AppScreenColumn
import agape.app.ui.components.EmptyStateView
import agape.app.ui.components.Kicker
import agape.app.ui.components.ScreenHeader
import agape.app.ui.components.SectionHeader
import agape.app.ui.components.appColumn
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.encode
import agape.app.ui.navigation.openReader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder

@Composable
fun PrayerHomeView(nav: NavHostController) {
    val store = LocalStore.current
    val hour = store.catalog.currentHour()
    val jesus = store.catalog.prayer("jezusgebed")
    LaunchedEffect(Unit) { store.refreshPrayerDay() }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader("AGAPE", store.t("prayer"), detail = store.t("prayerHomeLead"))
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Button(onClick = { nav.navigate("guidedWho") }, modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape()) {
                    Text(store.t("startPrayer"), style = AppType.sans(16.sp), modifier = Modifier.padding(vertical = 6.dp))
                }
                Text(store.t("startPrayerSkip"), style = AppType.serifItalic(15.sp), color = AppColor.faint())
            }
            jesus?.let { p ->
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Kicker(store.catalog.prayerKicker(p, store.state.locale))
                    Text(store.catalog.prayerTitle(p, store.state.locale), style = AppTypography.title(), color = AppColor.fg())
                    Text(store.catalog.prayerBody(p, store.state.locale).firstOrNull().orEmpty(), style = AppType.serif(20.sp), color = AppColor.fg(), lineHeight = 26.sp)
                }
            }
            PrayerCounter("jesus", showInvocation = false)
            Text("${store.state.prayerTodayCount} · ${store.t("prayedToday")}",
                style = AppTypography.caption(), color = AppColor.muted())
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl)) {
                PrayerPointGroup.entries.forEach { group ->
                    val subset = PrayerPoints.filtered(group)
                    if (subset.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                            SectionHeader(group.title(store.state.locale))
                            subset.forEachIndexed { i, point ->
                                BoxClick({ nav.navigate("prayerPoint/${point.id}") }) {
                                    AppListRow(point.title(store.state.locale), showSeparator = i < subset.lastIndex)
                                }
                            }
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                SectionHeader(store.t("hours"), store.t("all")) { nav.navigate("agpeya") }
                BoxClick({ nav.navigate("agpeyaHour/${hour.id}") }) {
                    AppListRow(
                        store.catalog.hourTitle(hour, store.state.locale),
                        "${hour.clock} · ${store.catalog.hourTheme(hour, store.state.locale)}",
                        showSeparator = false
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerPointDetailView(nav: NavHostController, id: String) {
    val store = LocalStore.current
    val point = PrayerPoints.point(id) ?: return
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())
            .appColumn().padding(vertical = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        ScreenHeader(point.group.title(store.state.locale), point.title(store.state.locale))
        Text(point.body(store.state.locale), style = AppType.serif(22.sp), color = AppColor.fg(), lineHeight = 29.sp)
        PrayerCounter("point/$id")
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
            SectionHeader(store.t("relatedVerses"))
            point.refs.forEachIndexed { i, ref ->
                val name = store.bookName(ref.bookId)
                val label = if (ref.verse != null) "$name ${ref.chapter}:${ref.verse}" else "$name ${ref.chapter}"
                BoxClick({ nav.openReader(ref) }) { AppListRow(label, showSeparator = i < point.refs.lastIndex) }
            }
        }
    }
}

@Composable
fun AgpeyaIndexView(nav: NavHostController) {
    val store = LocalStore.current
    val current = store.catalog.currentHour()
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader(store.t("hours"), store.t("agpeya"))
            store.catalog.hours.forEachIndexed { i, hour ->
                BoxClick({ nav.navigate("agpeyaHour/${hour.id}") }) {
                    AppListRow(
                        store.catalog.hourTitle(hour, store.state.locale),
                        "${hour.clock} · ${store.catalog.hourTheme(hour, store.state.locale)}",
                        showSeparator = i < store.catalog.hours.lastIndex,
                        emphasize = hour.id == current.id
                    )
                }
            }
        }
    }
}

@Composable
fun AgpeyaHourView(hourId: String) {
    val store = LocalStore.current
    val hour = store.catalog.hours.firstOrNull { it.id == hourId }
    val blocks = store.catalog.offices[hourId].orEmpty()
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())
            .appColumn().padding(vertical = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        hour?.let {
            Kicker("${store.t("agpeya")} · ${it.clock}")
            Text(store.catalog.hourTitle(it, store.state.locale), style = AppTypography.display(), color = AppColor.fg())
            Text(store.catalog.hourTheme(it, store.state.locale), style = AppType.serif(20.sp), color = AppColor.accent())
        }
        blocks.forEachIndexed { index, block ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Kicker(store.catalog.blockKicker(block, store.state.locale))
                store.catalog.blockTitle(block, store.state.locale)?.let {
                    Text(it, style = AppTypography.headline(), color = AppColor.fg())
                }
                store.catalog.blockLines(block, store.state.locale).forEach { line ->
                    Text(line, style = AppType.serif(19.sp), color = AppColor.fg(), lineHeight = 29.sp)
                }
                if (block.repetitions == 41) PrayerCounter("agpeya/$hourId/$index", showInvocation = false)
            }
        }
    }
}

@Composable
fun GuidedPrayerWhoView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader(store.t("prayer"), store.t("prayForWhom"), detail = store.t("prayForWhomLead"))
            GuidedPrayer.audiences.forEachIndexed { i, item ->
                BoxClick({
                    if (item.asksName) nav.navigate("guidedName/${item.id}")
                    else nav.navigate("guidedGuide/${item.id}?name=")
                }) {
                    AppListRow(item.title(store.state.locale), showSeparator = i < GuidedPrayer.audiences.lastIndex)
                }
            }
        }
    }
}

@Composable
fun GuidedPrayerNameView(nav: NavHostController, id: String) {
    val store = LocalStore.current
    val audience = GuidedPrayer.audiences.firstOrNull { it.id == id } ?: return
    var name by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader(audience.title(store.state.locale), store.t("prayNameTitle"), detail = store.t("prayNameLead"))
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(120) },
                label = { Text(store.t("prayNamePlaceholder")) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape()
            )
            AppDivider()
            Button(onClick = { nav.navigate("guidedGuide/${audience.id}?name=${encode(name)}") },
                modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape()) {
                Text(store.t("prayContinue"), modifier = Modifier.padding(vertical = 6.dp))
            }
            Text(
                store.t("praySkipName"),
                style = AppTypography.caption(),
                color = AppColor.muted(),
                modifier = Modifier.fillMaxWidth().height(AppSpacing.control).clickable { nav.navigate("guidedGuide/${audience.id}?name=") }.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun GuidedPrayerGuideView(nav: NavHostController, id: String, name: String) {
    val store = LocalStore.current
    val audience = GuidedPrayer.audiences.firstOrNull { it.id == id } ?: return
    var prayed by remember { mutableStateOf(false) }
    val locale = store.state.locale
    val spoken = audience.prayer(locale, name)
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader(audience.title(locale), store.t("howToPray"), detail = audience.lead(locale))
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                audience.tips(locale).forEachIndexed { i, tip ->
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text("${i + 1}", style = AppTypography.caption(), color = AppColor.faint(), modifier = Modifier.width(18.dp))
                        Text(tip, style = AppType.serif(18.sp), color = AppColor.muted())
                    }
                }
            }
            Column(Modifier.padding(top = AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t("youMaySay"))
                Text(spoken, style = AppType.serif(22.sp), color = AppColor.fg(), lineHeight = 29.sp)
            }
            AgapeButton(if (prayed) store.t("prayedAmen") else store.t("pray"), AgapeButtonKind.primary) {
                if (!prayed) { store.incPrayer(); prayed = true }
            }
            PrayerCounter("guided/$id")
            if (prayed) {
                Text(store.t("prayDone"), style = AppTypography.caption(), color = AppColor.muted(), modifier = Modifier.fillMaxWidth().height(AppSpacing.control).clickable { nav.popBackStack() }.padding(vertical = 12.dp))
            }
            audience.relatedPointId?.let { pid ->
                PrayerPoints.point(pid)?.let { point ->
                    BoxClick({ nav.navigate("prayerPoint/${point.id}") }) {
                        AppListRow(point.title(locale), store.t("relatedPrayer"), showSeparator = false)
                    }
                }
            }
        }
    }
}
