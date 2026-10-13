package agape.app.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.AppLocale
import agape.app.data.ChurchCalendar
import agape.app.data.Journey
import agape.app.data.JourneyCatalog
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.Haptics
import agape.app.ui.components.AgapeCard
import agape.app.ui.components.AppScreenColumn
import agape.app.ui.components.Kicker
import agape.app.ui.navigation.openReader
import agape.app.data.ChapterRef

@Composable
fun JourneyCatalogScreen(nav: NavHostController) {
    val store = LocalStore.current
    val en = store.state.locale == AppLocale.en
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).padding(AppSpacing.horizontal).padding(top = 28.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Kicker(if (en) "A PLAN" else "EEN PLAN")
        Text(if (en) "A road" else "Een weg", style = AppType.display(34.sp), color = AppColor.fg())
        Text(
            if (en) "Ordinary reading keeps your streak. A plan is one story, opened step by step."
            else "Gewoon lezen houdt je reeks bij. Een plan is één verhaal, dat je stap voor stap opent.",
            style = AppType.serif(16.sp), color = AppColor.muted()
        )
        JourneyCatalog.grouped().forEach { (group, items) ->
            Kicker(JourneyCatalog.groupTitle(group, store.state.locale).uppercase())
            items.forEach { journey ->
                RoadCard(nav, journey)
            }
        }
        Kicker(if (en) "CHAPTER BY CHAPTER" else "HOOFDSTUK VOOR HOOFDSTUK")
        store.catalog.plans.forEach { plan ->
            RoadCard(nav, JourneyCatalog.fromPlan(plan))
        }
    }
}

@Composable
fun JourneyHomeSection(nav: NavHostController) {
    val store = LocalStore.current
    val en = store.state.locale == AppLocale.en
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Kicker(if (en) "ROADS" else "WEGEN")
        JourneyCatalog.all.filter { it.group == "season" }.forEach { RoadCard(nav, it) }
        Text(
            if (en) "All roads" else "Alle wegen",
            modifier = Modifier.fillMaxWidth().clickable { nav.navigate("plans") }.padding(vertical = 8.dp),
            style = AppType.serif(16.sp),
            color = AppColor.fg(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RoadCard(nav: NavHostController, journey: Journey) {
    val store = LocalStore.current
    val en = store.state.locale == AppLocale.en
    val gate = ChurchCalendar.journeyGate(journey.id, store.state.calendarRite, store.state.locale)
    val enrolled = store.isEnrolled(journey.id)
    val locked = gate != null && !gate.open && !enrolled
    val done = store.journeyCursor(journey.id, journey.steps.size)
    val teaser = when {
        locked -> if (en) "Opens ${gate?.opens}" else "Opent ${gate?.opens}"
        enrolled && done >= journey.steps.size -> if (en) "This road is complete." else "Deze weg is uit."
        enrolled -> if (en) "Day ${done + 1} of ${journey.steps.size}" else "Dag ${done + 1} van ${journey.steps.size}"
        else -> journey.teaser(store.state.locale)
    }
    AgapeCard {
        Row(Modifier.fillMaxWidth().clickable { nav.navigate("journey/${journey.id}") }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(journey.title(store.state.locale), style = AppType.display(24.sp), color = if (locked) AppColor.muted() else AppColor.fg())
                Text(teaser, style = AppType.serif(16.sp), color = AppColor.muted())
                if (enrolled) {
                    Text(if (en) "$done of ${journey.steps.size}" else "$done van ${journey.steps.size}", style = AppType.serif(13.sp), color = AppColor.faint())
                    LinearProgressIndicator(
                        progress = { done.toFloat() / journey.steps.size.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = AppColor.dotGold,
                        trackColor = AppColor.hairline()
                    )
                }
            }
            Icon(
                if (locked) Icons.Outlined.Lock else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppColor.muted()
            )
        }
    }
}

@Composable
fun JourneyPathScreen(nav: NavHostController, id: String) {
    val store = LocalStore.current
    val journey = JourneyCatalog.find(id) ?: store.catalog.plans.firstOrNull { it.id == id }?.let(JourneyCatalog::fromPlan) ?: return
    val en = store.state.locale == AppLocale.en
    val gate = ChurchCalendar.journeyGate(journey.id, store.state.calendarRite, store.state.locale)
    val enrolled = store.isEnrolled(journey.id)
    val cursor = store.journeyCursor(journey.id, journey.steps.size)
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).padding(AppSpacing.horizontal).padding(top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Kicker(if (en) "DAY ${minOf(cursor + 1, journey.steps.size)} OF ${journey.steps.size}" else "DAG ${minOf(cursor + 1, journey.steps.size)} VAN ${journey.steps.size}")
        Text(journey.title(store.state.locale), style = AppType.display(32.sp), color = AppColor.fg())
        Text(journey.teaser(store.state.locale), style = AppType.serif(16.sp), color = AppColor.muted())
        when {
            enrolled -> journey.steps.forEachIndexed { index, step ->
                val state = when {
                    index < cursor -> "done"
                    index == cursor -> "now"
                    else -> "locked"
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(AppRadius.medium)).background(AppColor.surface()).clickable(enabled = state != "locked") {
                        nav.navigate("journeyStep/${journey.id}/$index")
                    }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(if (state == "now") 36.dp else 28.dp).clip(CircleShape).background(
                            if (state == "done") AppColor.dotGold else AppColor.surface2()
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = if (state == "done") Color(0xFF1C1916) else AppColor.fg(), style = AppType.serif(13.sp))
                    }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(step.title(store.state.locale), style = AppType.serif(17.sp), color = if (state == "locked") AppColor.faint() else AppColor.fg())
                    }
                    if (state == "locked") Icon(Icons.Outlined.Lock, null, tint = AppColor.faint())
                }
            }
            gate != null && !gate.open -> {
                Text(if (en) "Locked" else "Op slot", style = AppType.display(22.sp), color = AppColor.fg())
                Text(
                    if (en) "This road opens on ${gate.opens}. You can enroll then, not before."
                    else "Deze weg opent op ${gate.opens}. Dan kun je je inschrijven, niet eerder.",
                    style = AppType.serif(18.sp), color = AppColor.muted()
                )
            }
            else -> {
                Text(
                    if (en) "Day one opens when you enroll. Each finished step unlocks the next, and the day ends in prayer."
                    else "Dag één gaat open als je je inschrijft. Elke voltooide stap opent de volgende, en de dag eindigt met bidden.",
                    style = AppType.serif(18.sp), color = AppColor.fg()
                )
                Text(
                    if (en) "Enroll" else "Schrijf je in",
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(AppColor.fg()).clickable { store.enroll(journey.id) }.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = AppColor.bg(),
                    style = AppType.serif(16.sp)
                )
            }
        }
    }
}

@Composable
fun JourneyStepScreen(nav: NavHostController, id: String, index: Int) {
    val store = LocalStore.current
    val journey = JourneyCatalog.find(id) ?: store.catalog.plans.firstOrNull { it.id == id }?.let(JourneyCatalog::fromPlan) ?: return
    val step = journey.steps.getOrNull(index) ?: return
    val en = store.state.locale == AppLocale.en
    val cursor = store.journeyCursor(journey.id, journey.steps.size)
    val already = index < cursor
    var phase by remember { mutableIntStateOf(0) }
    var celebrated by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).padding(AppSpacing.horizontal).padding(top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (celebrated) {
            Text("${store.journeyStreak}", style = AppType.display(64.sp), color = AppColor.fg())
            Text(if (en) "This day is complete" else "Deze dag is voltooid", style = AppType.display(28.sp), color = AppColor.fg())
            Text(step.title(store.state.locale), style = AppType.serifItalic(18.sp), color = AppColor.muted())
            Text(
                if (en) "Come back tomorrow. The next step waits, in order."
                else "Kom morgen terug. De volgende stap wacht, op volgorde.",
                style = AppType.serif(16.sp), color = AppColor.muted()
            )
            Text(
                if (en) "Done" else "Klaar",
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(AppColor.fg()).clickable { nav.popBackStack() }.padding(horizontal = 18.dp, vertical = 10.dp),
                color = AppColor.bg()
            )
            return@Column
        }
        Kicker(if (en) "DAY ${index + 1} OF ${journey.steps.size}" else "DAG ${index + 1} VAN ${journey.steps.size}")
        Text(step.title(store.state.locale), style = AppType.display(30.sp), color = AppColor.fg())
        Text(store.displayedRef(step.bookId, step.chapter), style = AppType.serif(16.sp), color = AppColor.muted())
        if (!already) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= phase) AppColor.dotGold else AppColor.hairline()))
                }
            }
        }
        if (already || phase >= 1) {
            Kicker(if (en) "THE STORY" else "HET VERHAAL")
            Text(step.context(store.state.locale), style = AppType.serif(18.sp), color = AppColor.fg())
        }
        if (already || phase >= 1) {
            Kicker(if (en) "TO SIT WITH" else "OM BIJ STIL TE STAAN")
            Text(step.question(store.state.locale), style = AppType.serifItalic(18.sp), color = AppColor.fg())
        }
        if (!already && phase == 0) {
            Text(if (en) "First, read." else "Eerst lezen.", style = AppType.serifItalic(18.sp), color = AppColor.muted())
            Text(
                if (en) "Open the passage" else "Open het gedeelte",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppRadius.medium)).background(AppColor.accent()).clickable {
                    nav.openReader(ChapterRef(step.bookId, step.chapter))
                }.padding(vertical = 14.dp),
                color = AppColor.accentFg(),
                textAlign = TextAlign.Center
            )
            Text(
                if (en) "I have read it" else "Ik heb het gelezen",
                modifier = Modifier.clickable { phase = 1; Haptics.light() }.padding(vertical = 8.dp),
                color = AppColor.fg(),
                style = AppType.serif(16.sp)
            )
        } else if (!already && phase == 1) {
            Text(
                if (en) "Continue to prayer" else "Verder naar het gebed",
                modifier = Modifier.clickable { phase = 2; Haptics.light() }.padding(vertical = 8.dp),
                color = AppColor.fg(),
                style = AppType.serif(16.sp)
            )
        } else if (!already) {
            Kicker(if (en) "PRAYER" else "GEBED")
            Text(
                if (en) "Lord, what I read today, do not let it leave me empty. Keep it near. Amen."
                else "Heer, wat ik vandaag gelezen heb, laat het niet leeg bij mij blijven. Houd het nabij. Amen.",
                style = AppType.serif(22.sp), color = AppColor.fg()
            )
            Text(
                "Amen",
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(AppColor.fg()).clickable {
                    store.completeJourneyStep(journey.id, index, journey.steps.size, step.bookId, step.chapter)
                    celebrated = true
                }.padding(horizontal = 22.dp, vertical = 10.dp),
                color = AppColor.bg()
            )
        }
    }
}
