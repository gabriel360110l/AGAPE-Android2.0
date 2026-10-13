package agape.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import agape.app.data.ChapterRef
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.Haptics
import agape.app.ui.bible.BibleView
import agape.app.ui.bible.BookChaptersView
import agape.app.ui.calendar.CalendarDayView
import agape.app.ui.calendar.CalendarHomeView
import agape.app.ui.calendar.ReadingsView
import agape.app.ui.components.nativeGlass
import agape.app.ui.home.HomeView
import agape.app.ui.library.ChurchNodeView
import agape.app.ui.library.FathersView
import agape.app.ui.library.GlossaryView
import agape.app.ui.library.HarmonyView
import agape.app.ui.library.PlanDetailView
import agape.app.ui.library.PlansView
import agape.app.ui.library.QuotesView
import agape.app.ui.library.TopicDetailView
import agape.app.ui.library.TopicsView
import agape.app.ui.more.MoreView
import agape.app.ui.more.SavedView
import agape.app.ui.more.SettingsView
import agape.app.ui.onboarding.IntroView
import agape.app.ui.onboarding.SetupView
import agape.app.ui.prayer.AgpeyaHourView
import agape.app.ui.prayer.AgpeyaIndexView
import agape.app.ui.prayer.GuidedPrayerGuideView
import agape.app.ui.prayer.GuidedPrayerNameView
import agape.app.ui.prayer.GuidedPrayerWhoView
import agape.app.ui.prayer.PrayerHomeView
import agape.app.ui.prayer.PrayerPointDetailView
import agape.app.ui.reader.ChapterGuideView
import agape.app.ui.reader.FatherCommentaryView
import agape.app.ui.reader.ReaderView
import agape.app.ui.search.SearchView
import agape.app.ui.splash.BootFilm
import agape.app.ui.onboarding.WelcomeView
import java.net.URLDecoder
import java.net.URLEncoder

enum class AppTab { home, bible, calendar, prayer, search }

fun encode(s: String) = URLEncoder.encode(s, "UTF-8")
fun decode(s: String) = URLDecoder.decode(s, "UTF-8")

fun NavHostController.openReader(ref: ChapterRef) {
    val v = ref.verse?.let { "?verse=$it" } ?: ""
    navigate("reader/${encode(ref.bookId)}/${ref.chapter}$v")
}

@Composable
fun RootView() {
    val store = LocalStore.current
    var showSplash by rememberSaveable { mutableStateOf(true) }
    Box(Modifier.fillMaxSize().background(AppColor.bg())) {
        AnimatedContent(
            targetState = when {
                showSplash -> "splash"
                !store.seenWelcome -> "welcome"
                !store.state.seenSetup -> "setup"
                else -> "main"
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "root"
        ) { phase ->
            when (phase) {
                "splash" -> BootFilm { showSplash = false }
                "welcome" -> WelcomeView()
                "setup" -> SetupView()
                else -> MainTabs()
            }
        }
    }
}

@Composable
fun MainTabs() {
    val store = LocalStore.current
    var tab by rememberSaveable { mutableStateOf(AppTab.home) }
    var immersive by remember { mutableStateOf(false) }
    val homeNav = rememberNavController()
    val bibleNav = rememberNavController()
    val calNav = rememberNavController()
    val prayerNav = rememberNavController()
    val searchNav = rememberNavController()
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = AppColor.bg(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!immersive) GlassTabBar(tab, onSelect = { tab = it })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                AppTab.home -> TabNav(homeNav, "home") { HomeView(it) { immersive = it } }
                AppTab.bible -> TabNav(bibleNav, "bible") { BibleView(it) }
                AppTab.calendar -> TabNav(calNav, "calendar") { CalendarHomeView(it) }
                AppTab.prayer -> TabNav(prayerNav, "prayer") { PrayerHomeView(it) }
                AppTab.search -> TabNav(searchNav, "search") { SearchView(it) }
            }
        }
    }
}

@Composable
private fun TabNav(nav: NavHostController, start: String, startContent: @Composable (NavHostController) -> Unit) {
    val entry by nav.currentBackStackEntryAsState()
    val store = LocalStore.current
    Column(Modifier.fillMaxSize()) {
        if (entry != null && entry?.destination?.route != start) {
            TextButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = AppColor.accent())
                Text(store.t("back"), modifier = Modifier.padding(start = 8.dp), color = AppColor.accent())
            }
        }
        NavHost(nav, startDestination = start, modifier = Modifier.weight(1f)) {
            composable(start) { startContent(nav) }
            sharedRoutes(nav)
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.sharedRoutes(nav: NavHostController) {
    composable(
        "reader/{bookId}/{chapter}?verse={verse}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType },
            navArgument("chapter") { type = NavType.IntType },
            navArgument("verse") { type = NavType.IntType; defaultValue = -1 }
        )
    ) { entry ->
        val bookId = decode(entry.arguments?.getString("bookId") ?: "genesis")
        val chapter = entry.arguments?.getInt("chapter") ?: 1
        val verse = entry.arguments?.getInt("verse")?.takeIf { it > 0 }
        ReaderView(nav, ChapterRef(bookId, chapter, verse))
    }
    composable("book/{bookId}") { entry ->
        val id = decode(entry.arguments?.getString("bookId") ?: "")
        agape.app.data.ContentCatalog.book(id)?.let { BookChaptersView(nav, it) }
    }
    composable("guide/{bookId}/{chapter}") { entry ->
        ChapterGuideView(
            nav,
            decode(entry.arguments?.getString("bookId") ?: ""),
            entry.arguments?.getString("chapter")?.toIntOrNull() ?: 1
        )
    }
    composable(
        "father/{bookId}/{chapter}/{index}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType },
            navArgument("chapter") { type = NavType.IntType },
            navArgument("index") { type = NavType.IntType }
        )
    ) { entry ->
        val bookId = decode(entry.arguments?.getString("bookId") ?: "")
        val chapter = entry.arguments?.getInt("chapter") ?: 1
        val index = entry.arguments?.getInt("index") ?: 0
        val locale = agape.app.AgapeApplication.instance.store.state.locale
        val guide = agape.app.data.ChapterGuideStore.guide(bookId, chapter, locale)
        val father = guide?.fathers?.getOrNull(index)
        if (father != null) FatherCommentaryView(father, bookId, chapter)
    }
    composable("settings") { SettingsView(nav) }
    composable("profile") { agape.app.ui.onboarding.ProfileView() }
    composable("saved") { SavedView(nav) }
    composable("more") { MoreView(nav) }
    composable("calendarHome") { CalendarHomeView(nav) }
    composable("readings") { ReadingsView(nav) }
    composable("plans") { agape.app.ui.journey.JourneyCatalogScreen(nav) }
    composable("journey/{id}") { agape.app.ui.journey.JourneyPathScreen(nav, entryArg(it, "id")) }
    composable(
        "journeyStep/{id}/{index}",
        arguments = listOf(navArgument("index") { type = NavType.IntType })
    ) { entry ->
        agape.app.ui.journey.JourneyStepScreen(nav, entryArg(entry, "id"), entry.arguments?.getInt("index") ?: 0)
    }
    composable("prayHelp") { agape.app.ui.prayer.PrayerTutorialScreen() }
    composable("plan/{planId}") { entry ->
        val id = entry.arguments?.getString("planId") ?: return@composable
        ContentCatalogSafe.plan(id)?.let { PlanDetailView(nav, it) }
    }
    composable("topics") { TopicsView(nav) }
    composable("topic/{id}") { entry ->
        ContentCatalogSafe.topic(entry.arguments?.getString("id").orEmpty())?.let { TopicDetailView(nav, it) }
    }
    composable("harmony") { HarmonyView(nav) }
    composable("glossary") { GlossaryView() }
    composable("fathers") { FathersView(nav) }
    composable("quotes") { QuotesView(nav) }
    composable("church/{id}") { ChurchNodeView(nav, decode(entryArg(it, "id"))) }
    composable("calendarDay/{ymd}") { CalendarDayView(nav, entryArg(it, "ymd")) }
    composable("prayerPoint/{id}") { PrayerPointDetailView(nav, entryArg(it, "id")) }
    composable("prayerHome") { PrayerHomeView(nav) }
    composable("agpeya") { AgpeyaIndexView(nav) }
    composable("agpeyaHour/{id}") { AgpeyaHourView(entryArg(it, "id")) }
    composable("guidedWho") { GuidedPrayerWhoView(nav) }
    composable("guidedName/{id}") { GuidedPrayerNameView(nav, entryArg(it, "id")) }
    composable("guidedGuide/{id}?name={name}") { entry ->
        GuidedPrayerGuideView(nav, entryArg(entry, "id"), decode(entry.arguments?.getString("name") ?: ""))
    }
    composable("search") { SearchView(nav) }
}

private fun entryArg(entry: androidx.navigation.NavBackStackEntry, key: String) =
    decode(entry.arguments?.getString(key) ?: "")

private object ContentCatalogSafe {
    fun plan(id: String) = agape.app.data.ContentCatalog.plans.firstOrNull { it.id == id }
    fun topic(id: String) = agape.app.data.ContentCatalog.topics.firstOrNull { it.id == id }
}

@Composable
fun GlassTabBar(selection: AppTab, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val context = LocalContext.current
    val items = listOf(
        Triple(AppTab.home, store.t("home"), Icons.Outlined.Home to Icons.Filled.Home),
        Triple(AppTab.bible, store.t("bible"), Icons.Outlined.MenuBook to Icons.Filled.MenuBook),
        Triple(AppTab.calendar, store.t("calendar"), Icons.Outlined.CalendarMonth to Icons.Filled.CalendarMonth),
        Triple(AppTab.prayer, store.t("prayer"), Icons.Outlined.FavoriteBorder to Icons.Filled.Favorite),
        Triple(AppTab.search, store.t("search"), Icons.Outlined.Search to Icons.Filled.Search)
    )
    val bar = RoundedCornerShape(32.dp)
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.horizontal)
            .padding(top = AppSpacing.xs, bottom = AppSpacing.xs)
            .nativeGlass(bar)
            .padding(6.dp)
    ) {
        items.forEach { (tab, title, icons) ->
            val active = selection == tab
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .semantics { selected = active }
                    .background(if (active) AppColor.accent().copy(0.13f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable(role = Role.Tab) {
                        Haptics.select(context)
                        onSelect(tab)
                    }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(if (active) icons.second else icons.first, title, tint = if (active) AppColor.accent() else AppColor.muted(), modifier = androidx.compose.ui.Modifier.padding(0.dp))
                Text(title, style = AppType.sans(11.sp, if (active) FontWeight.SemiBold else FontWeight.Medium), color = if (active) AppColor.accent() else AppColor.muted(), maxLines = 1)
            }
        }
    }
}
