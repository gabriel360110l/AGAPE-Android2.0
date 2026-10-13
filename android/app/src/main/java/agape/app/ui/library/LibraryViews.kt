package agape.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.ChapterRef
import agape.app.data.ReadingPlan
import agape.app.data.Topic
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.ui.components.AgapeCard
import agape.app.ui.components.EmptyStateView
import agape.app.ui.components.Kicker
import agape.app.ui.components.MeaningCard
import agape.app.ui.components.ScreenHeader
import agape.app.ui.components.appColumn
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.encode
import agape.app.ui.navigation.openReader

@Composable
fun PlansView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker(store.t("readingPlan"))
        Text(store.t("readingPlan"), style = AppType.display(34.sp), color = AppColor.fg())
        store.catalog.plans.forEach { plan ->
            BoxClick({ nav.navigate("plan/${plan.id}") }) {
                AgapeCard {
                    Text(store.catalog.planTitle(plan, store.state.locale), style = AppType.display(24.sp), color = AppColor.fg())
                    Text(store.catalog.planTeaser(plan, store.state.locale), style = AppType.sans(15.sp), color = AppColor.muted(), modifier = Modifier.padding(top = 4.dp))
                    val done = store.state.planDone[plan.id]?.size ?: 0
                    LinearProgressIndicator(
                        progress = { done.toFloat() / maxOf(plan.days.size, 1) },
                        modifier = Modifier.padding(top = 8.dp),
                        color = AppColor.accent(),
                        trackColor = AppColor.line()
                    )
                }
            }
        }
    }
}

@Composable
fun PlanDetailView(nav: NavHostController, plan: ReadingPlan) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(store.catalog.planTitle(plan, store.state.locale), style = AppType.display(28.sp), color = AppColor.fg())
        plan.days.forEachIndexed { index, day ->
            val done = store.state.planDone[plan.id]?.contains(index) == true
            AgapeCard {
                Text(if (done) "✓" else "○", color = AppColor.accent(), modifier = Modifier.clickable { store.togglePlan(plan.id, index) })
                BoxClick({ nav.openReader(ChapterRef(day.bookId, day.chapter)) }) {
                    Text(store.catalog.planDayLabel(day, store.state.locale), style = AppType.serif(18.sp), color = AppColor.fg())
                }
            }
        }
    }
}

@Composable
fun TopicsView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker(store.t("topics"))
        Text(store.t("topics"), style = AppType.display(34.sp), color = AppColor.fg())
        store.catalog.topics.forEach { topic ->
            BoxClick({ nav.navigate("topic/${topic.id}") }) {
                AgapeCard {
                    Text(store.catalog.topicTitle(topic, store.state.locale), style = AppType.display(26.sp), color = AppColor.fg())
                    Text(store.catalog.topicTeaser(topic, store.state.locale), style = AppType.sans(15.sp), color = AppColor.muted(), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun TopicDetailView(nav: NavHostController, topic: Topic) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(store.catalog.topicTitle(topic, store.state.locale), style = AppType.display(32.sp), color = AppColor.fg())
        Text(store.catalog.topicTeaser(topic, store.state.locale), style = AppType.serif(18.sp), color = AppColor.muted())
        topic.verses.forEach { verse ->
            BoxClick({ nav.openReader(ChapterRef(verse.bookId, verse.chapter, verse.verse)) }) {
                AgapeCard {
                    Text(store.displayedRef(verse.bookId, verse.chapter, verse.verse), style = AppType.display(20.sp), color = AppColor.accent())
                    Text(store.displayedVerse(verse.bookId, verse.chapter, verse.verse, verse.text), style = AppType.serif(17.sp), color = AppColor.fg(), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun HarmonyView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(store.t("harmony"))
        Text(store.t("harmony"), style = AppType.display(34.sp), color = AppColor.fg())
        store.catalog.harmony.forEach { event ->
            AgapeCard {
                MeaningCard(
                    kicker = store.catalog.harmonyKicker(event, store.state.locale),
                    term = store.catalog.harmonyTitle(event, store.state.locale),
                    meaning = store.catalog.harmonyTeaser(event, store.state.locale),
                    compact = true,
                    chrome = false
                )
                event.refs.forEach { ref ->
                    Text(
                        "${store.bookName(ref.bookId)} ${ref.chapter}${ref.verses?.let { ":$it" } ?: ""}",
                        style = AppType.serif(17.sp),
                        color = AppColor.accent(),
                        modifier = Modifier.clickable { nav.openReader(ChapterRef(ref.bookId, ref.chapter)) }.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GlossaryView() {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(AppSpacing.card)) {
        ScreenHeader(store.t("scripture"), store.t("sectionWords"))
        store.catalog.glossary.forEach { entry ->
            MeaningCard(
                term = store.catalog.glossaryTerm(entry, store.state.locale),
                original = entry.greek,
                translit = entry.translit,
                meaning = store.catalog.glossaryBody(entry, store.state.locale)
            )
        }
    }
}

@Composable
fun FathersView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker(store.t("fathers"))
        Text(store.t("fathers"), style = AppType.display(34.sp), color = AppColor.fg())
        store.catalog.fathers.forEach { quote ->
            AgapeCard {
                Text(store.catalog.fatherName(quote, store.state.locale), style = AppType.display(24.sp), color = AppColor.fg())
                Text(store.catalog.fatherSource(quote, store.state.locale), style = AppType.sans(12.sp), color = AppColor.muted())
                Text(store.catalog.fatherText(quote, store.state.locale), style = AppType.serifItalic(18.sp), color = AppColor.accent(), modifier = Modifier.padding(top = 6.dp))
                quote.bookId?.let { id ->
                    store.catalog.book(id)?.let {
                        Text(store.t("fullText"), color = AppColor.fg(), modifier = Modifier.clickable { nav.navigate("book/${encode(id)}") }.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChurchNodeView(nav: NavHostController, nodeId: String) {
    val store = LocalStore.current
    val node = store.catalog.church[nodeId]
    val children = store.catalog.churchChildren(nodeId)
    val meaning = store.catalog.churchMeaning(nodeId)
    val title = node?.title?.text(store.state.locale) ?: store.t("church")
    val teaser = node?.teaser?.text(store.state.locale) ?: store.t("churchLead")
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(AppSpacing.card)) {
        MeaningCard(kicker = store.t("churchFolder"), term = title, original = meaning?.original, translit = meaning?.translit, meaning = meaning?.body?.text(store.state.locale) ?: teaser)
        if (children.isEmpty() && meaning == null) {
            EmptyStateView(store.t("churchEmpty"), store.t("churchEmptyBody"), Icons.Outlined.Folder)
        } else {
            children.forEach { child ->
                val childMeaning = store.catalog.churchMeaning(child.id)
                BoxClick({ nav.navigate("church/${encode(child.id)}") }) {
                    MeaningCard(term = child.title.text(store.state.locale), original = childMeaning?.original, translit = childMeaning?.translit, meaning = child.teaser.text(store.state.locale), compact = true)
                }
            }
        }
    }
}

@Composable
fun QuotesView(nav: NavHostController) {
    val store = LocalStore.current
    Column(Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(vertical = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(store.t("today"), style = AppType.display(34.sp), color = AppColor.fg())
        store.catalog.quotes.forEach { quote ->
            BoxClick({ nav.openReader(ChapterRef(quote.bookId, quote.chapter, quote.verse)) }) {
                AgapeCard {
                    Text("“${store.displayedVerse(quote.bookId, quote.chapter, quote.verse, quote.text)}”", style = AppType.serif(18.sp), color = AppColor.fg())
                    Text(store.displayedRef(quote.bookId, quote.chapter, quote.verse), style = AppType.display(20.sp), color = AppColor.accent(), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}
