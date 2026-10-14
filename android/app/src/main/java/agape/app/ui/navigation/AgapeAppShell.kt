package agape.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import agape.app.data.*
import agape.app.ui.agpeya.AgpeyaHourScreen
import agape.app.ui.bible.BibleReaderScreen
import agape.app.ui.theme.AgapeSacredTheme
import agape.app.ui.theme.LocalAgapeColors
import agape.app.ui.tradition.TraditionDocsScreen
import agape.app.ui.video.IntroPreferences
import agape.app.ui.video.IntroVideoScreen

@Composable
fun AgapeAppShell() {
    val context = LocalContext.current
    val repository = remember { AgapeRepository(context) }

    var showIntro by remember { mutableStateOf(IntroPreferences.shouldShowIntro(context)) }
    var currentTab by remember { mutableIntStateOf(0) }
    var fatherSearchFilter by remember { mutableStateOf("") }

    var verses by remember { mutableStateOf<List<BibleVerseUiModel>>(emptyList()) }
    var agpeyaHours by remember { mutableStateOf<List<AgpeyaHourModel>>(emptyList()) }
    var prayers by remember { mutableStateOf<List<PrayerModel>>(emptyList()) }
    var fathersDoc by remember {
        mutableStateOf(TraditionDocument("fathers", "Belangrijkste Kerkvaders", emptyList()))
    }
    var councilsDoc by remember {
        mutableStateOf(TraditionDocument("councils", "Zeven Oecumenische Concilies", emptyList()))
    }

    LaunchedEffect(Unit) {
        verses = repository.getChapterWithCommentary("JHN", 43, 1)
        agpeyaHours = repository.loadAgpeyaHours()
        prayers = repository.loadPrayers()
        fathersDoc = repository.loadTraditionMarkdown(
            "BELANGRIJKSTE-KERKVADERS.md",
            "Belangrijkste Kerkvaders"
        )
        councilsDoc = repository.loadTraditionMarkdown(
            "zeven-oecumenische-concilies-canones.md",
            "Zeven Oecumenische Concilies & Canones"
        )
    }

    AgapeSacredTheme {
        if (showIntro) {
            IntroVideoScreen(onIntroFinished = { showIntro = false })
        } else {
            val sacredColors = LocalAgapeColors.current
            Scaffold(
                containerColor = sacredColors.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = sacredColors.surface,
                        contentColor = sacredColors.primaryText
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Text("☩", color = sacredColors.sacredGold) },
                            label = { Text("Schrift", style = MaterialTheme.typography.labelLarge) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Text("✛", color = sacredColors.rubricRed) },
                            label = { Text("Agpeya & Gebed", style = MaterialTheme.typography.labelLarge) }
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Text("ΑΩ", color = sacredColors.sacredGold) },
                            label = { Text("Vaders & Concilies", style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        0 -> BibleReaderScreen(
                            bookTitle = "Johannes",
                            chapterNumber = 1,
                            chapterExplanation = ChapterExplanation(
                                bookId = "JHN",
                                chapter = 1,
                                title = "De Eeuwige Logos en Zijn Menswording",
                                overview = "De proloog van het Johannes-evangelie (1:1–18) vormt het theologische fundament van het Orthodoxe geloof: het eeuwige Woord (de Logos) dat één in wezen is met de Vader, wordt vlees om de mensheid te verlossen en te verlichten."
                            ),
                            verses = verses,
                            onNavigateToFather = { fatherName ->
                                fatherSearchFilter = fatherName
                                currentTab = 2
                            }
                        )
                        1 -> AgpeyaHourScreen(
                            hours = agpeyaHours,
                            prayers = prayers
                        )
                        2 -> TraditionDocsScreen(
                            councilsDoc = councilsDoc,
                            fathersDoc = fathersDoc,
                            initialSearchQuery = fatherSearchFilter
                        )
                    }
                }
            }
        }
    }
}
