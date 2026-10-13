package agape.app.ui.reader

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.BibleDatabase
import agape.app.data.ChapterGuide
import agape.app.data.ChapterGuideStore
import agape.app.data.ChapterRef
import agape.app.data.ChurchFatherCommentary
import agape.app.data.MarkTint
import agape.app.data.NightMode
import agape.app.data.SavedVerse
import agape.app.data.VerseKey
import agape.app.data.VerseRecord
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.Haptics
import agape.app.ui.components.AgapeButton
import agape.app.ui.components.AgapeButtonKind
import agape.app.ui.components.AppDivider
import agape.app.ui.components.AppListRow
import agape.app.ui.components.Kicker
import agape.app.ui.components.VerseActionBar
import agape.app.ui.components.appColumn
import agape.app.ui.components.fillColor
import agape.app.ui.components.nativeGlass
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.encode
import agape.app.ui.navigation.openReader
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderView(nav: NavHostController, ref: ChapterRef) {
    val store = LocalStore.current
    val context = LocalContext.current
    val paper = !store.night
    val bg = if (paper) AppColor.paper else AppColor.bg()
    val ink = if (paper) AppColor.ink else AppColor.fg()
    val chapters = remember(store.state.tradition) { store.catalog.allChapters(store.state.tradition) }
    val start = remember(ref) { store.catalog.chapterIndex(ref, store.state.tradition) ?: 0 }
    val pager = rememberPagerState(initialPage = start) { chapters.size }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var showChrome by remember { mutableStateOf(true) }
    var showChapters by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }
    var noteKey by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val current = chapters.getOrNull(pager.currentPage) ?: ref
    var verses by remember { mutableStateOf(emptyList<VerseRecord>()) }
    LaunchedEffect(pager.currentPage, current.id) {
        verses = BibleDatabase.verses(current.bookId, current.chapter)
        selected = if (pager.currentPage == start && ref.verse != null) setOf(ref.verse) else emptySet()
        store.markVisited(current.bookId, current.chapter)
    }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(bg)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            val pageRef = chapters[page]
            BibleChapterPage(
                nav, pageRef, paper, ink, showChrome, selected,
                onToggle = {
                    selected = if (it in selected) selected - it else selected + it
                    showChrome = true
                    Haptics.light(context)
                },
                onOpenChapters = { showChapters = true },
                onOpenNotes = { showNotes = true },
                onTapBackground = { if (selected.isEmpty()) showChrome = !showChrome }
            )
        }
        if (showChrome) {
            Column(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Icon(Icons.Outlined.MoreVert, store.t("more"), tint = ink, modifier = Modifier.clickable { menu = true }.padding(12.dp))
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text(store.t("smaller")) }, { store.setFont(-0.08); menu = false })
                    DropdownMenuItem({ Text(store.t("larger")) }, { store.setFont(0.08); menu = false })
                    DropdownMenuItem({ Text(if (store.night) store.t("parchment") else store.t("night")) }, {
                        store.update { copy(nightMode = if (store.night) NightMode.day else NightMode.night) }
                        store.persist()
                        menu = false
                    })
                }
            }
        }
        if (showChrome) {
            Column(
                Modifier.align(Alignment.BottomCenter).padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
            ) {
                if (selected.isNotEmpty()) {
                    VerseActionBar(
                        paper = paper,
                        onHighlight = { tint ->
                            verses.filter { it.verse in selected }.forEach { v ->
                                val key = VerseKey.make(v.bookId, v.chapter, v.verse)
                                store.setColor(key, tint, SavedVerse(key, v.bookId, v.chapter, v.verse, store.verseText(v)))
                            }
                        },
                        onClear = {
                            verses.filter { it.verse in selected }.forEach { v ->
                                store.setColor(VerseKey.make(v.bookId, v.chapter, v.verse), null, null)
                            }
                        },
                        onBookmark = {
                            verses.filter { it.verse in selected }.forEach { v ->
                                val key = VerseKey.make(v.bookId, v.chapter, v.verse)
                                store.toggleSave(SavedVerse(key, v.bookId, v.chapter, v.verse, store.verseText(v)))
                            }
                        },
                        onNote = {
                            val v = verses.firstOrNull { it.verse in selected } ?: return@VerseActionBar
                            val key = VerseKey.make(v.bookId, v.chapter, v.verse)
                            noteText = store.state.notes[key].orEmpty()
                            noteKey = key
                        },
                        onShare = {
                            val body = verses.filter { it.verse in selected }.sortedBy { it.verse }
                                .joinToString("\n") { "${it.verse} ${store.verseText(it)}" }
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${store.bookName(current.bookId)} ${current.chapter}\n$body")
                            }
                            context.startActivity(Intent.createChooser(share, store.t("share")))
                            Haptics.success(context)
                        },
                        bookmarkTitle = store.t("saved"),
                        noteTitle = store.t("note"),
                        shareTitle = store.t("share"),
                        highlightTitle = store.t("highlight"),
                        bookmarked = verses.firstOrNull { it.verse in selected }?.let {
                            store.isSaved(VerseKey.make(it.bookId, it.chapter, it.verse))
                        } ?: false
                    )
                } else {
                    val shape = AppRadius.extraLargeShape()
                    Column(
                        Modifier.fillMaxWidth().nativeGlass(shape).padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ChevronLeft, store.t("previousPage"), tint = ink,
                                modifier = Modifier.clickable(enabled = pager.currentPage > 0) { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }.padding(10.dp))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(store.bookName(current.bookId), style = AppType.display(18.sp), color = ink, maxLines = 1)
                                Text("${current.chapter}", style = AppType.sans(13.sp, FontWeight.Medium), color = if (paper) AppColor.inkSoft else AppColor.muted())
                            }
                            Icon(Icons.Outlined.ChevronRight, store.t("nextPage"), tint = ink,
                                modifier = Modifier.clickable(enabled = pager.currentPage < chapters.lastIndex) { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }.padding(10.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val read = store.isRead(current.bookId, current.chapter)
                            Text(
                                if (read) store.t("finished") else store.t("markRead"),
                                style = AppType.sans(14.sp), color = ink,
                                modifier = Modifier.weight(1f).clickable { store.toggleRead(current.bookId, current.chapter) }.padding(12.dp)
                            )
                            Text(
                                store.t("guide"),
                                style = AppType.sans(14.sp), color = ink,
                                modifier = Modifier.weight(1f).clickable { nav.navigate("guide/${encode(current.bookId)}/${current.chapter}") }.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    if (showChapters) {
        ModalBottomSheet(onDismissRequest = { showChapters = false }, containerColor = bg) {
            Text(store.t("chapter"), style = AppType.display(22.sp), color = ink, modifier = Modifier.padding(AppSpacing.horizontal))
            val count = store.catalog.book(current.bookId)?.chapters ?: current.chapter
            Column(Modifier.padding(AppSpacing.horizontal), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..count).chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { n ->
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(AppRadius.mediumShape())
                                    .background(if (n == current.chapter) AppColor.accent() else AppColor.surface())
                                    .clickable {
                                        val idx = store.catalog.chapterIndex(ChapterRef(current.bookId, n), store.state.tradition)
                                        if (idx != null) scope.launch { pager.scrollToPage(idx) }
                                        showChapters = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$n", color = if (n == current.chapter) AppColor.accentFg() else AppColor.fg())
                            }
                        }
                    }
                }
            }
        }
    }
    if (showNotes) {
        ModalBottomSheet(onDismissRequest = { showNotes = false }, containerColor = bg) {
            Text(store.t("notes"), style = AppType.display(22.sp), modifier = Modifier.padding(AppSpacing.horizontal), color = ink)
            val keys = store.state.notes.keys.filter { it.startsWith("${current.bookId}:${current.chapter}:") }.sorted()
            Column(Modifier.padding(AppSpacing.horizontal)) {
                if (keys.isEmpty()) Text(store.t("notesEmpty"), style = AppType.serif(18.sp), color = if (paper) AppColor.inkSoft else AppColor.muted())
                keys.forEach { key ->
                    Text(key, style = AppType.sans(13.sp, FontWeight.Medium), color = AppColor.accent())
                    Text(store.state.notes[key].orEmpty(), style = AppType.serif(18.sp), color = ink)
                }
            }
        }
    }
    noteKey?.let { key ->
        ModalBottomSheet(onDismissRequest = { noteKey = null }, containerColor = bg) {
            Text(store.t("note"), style = AppType.display(22.sp), modifier = Modifier.padding(AppSpacing.horizontal), color = ink)
            TextField(
                noteText, {
                    noteText = it
                    store.setNote(key, it)
                },
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.horizontal),
                colors = TextFieldDefaults.colors(focusedContainerColor = bg, unfocusedContainerColor = bg)
            )
            AgapeButton(store.t("setupContinue"), AgapeButtonKind.primary) { noteKey = null }
        }
    }
}

@Composable
private fun BibleChapterPage(
    nav: NavHostController,
    ref: ChapterRef,
    paper: Boolean,
    ink: androidx.compose.ui.graphics.Color,
    showChrome: Boolean,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    onOpenChapters: () -> Unit,
    onOpenNotes: () -> Unit,
    onTapBackground: () -> Unit
) {
    val store = LocalStore.current
    var verses by remember(ref.id) { mutableStateOf(emptyList<VerseRecord>()) }
    LaunchedEffect(ref.id) { verses = withContext(Dispatchers.IO) { BibleDatabase.verses(ref.bookId, ref.chapter) } }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).pointerInput(selected) {
            detectTapGestures { onTapBackground() }
        }
    ) {
        Text(
            "${store.bookName(ref.bookId)} ${ref.chapter}",
            style = AppType.display(34.sp),
            color = ink,
            modifier = Modifier.padding(horizontal = AppSpacing.horizontal, vertical = AppSpacing.md)
        )
        if (showChrome) {
            Row(Modifier.padding(horizontal = AppSpacing.horizontal).padding(bottom = AppSpacing.md), horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                Text(store.t("chapter"), style = AppType.sans(13.sp, FontWeight.Medium), color = if (paper) AppColor.inkSoft else AppColor.muted(), modifier = Modifier.clickable(onClick = onOpenChapters))
                Text(store.t("guide"), style = AppType.sans(13.sp, FontWeight.Medium), color = if (paper) AppColor.inkSoft else AppColor.muted(), modifier = Modifier.clickable { nav.navigate("guide/${encode(ref.bookId)}/${ref.chapter}") })
                Text(store.t("notes"), style = AppType.sans(13.sp, FontWeight.Medium), color = if (paper) AppColor.inkSoft else AppColor.muted(), modifier = Modifier.clickable(onClick = onOpenNotes))
            }
        }
        if (showChrome) ChapterExplanationPreview(nav, ref, ink)
        verses.filter { store.verseText(it).isNotEmpty() }.forEach { verse ->
            val key = VerseKey.make(verse.bookId, verse.chapter, verse.verse)
            val mark = store.state.colors[key]
            val red = store.state.redLetter && store.catalog.isWordsOfChrist(verse.bookId, verse.chapter, verse.verse)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(mark?.fillColor() ?: if (verse.verse in selected) (if (paper) AppColor.ink.copy(0.06f) else AppColor.fg().copy(0.08f)) else androidx.compose.ui.graphics.Color.Transparent)
                    .pointerInput(verse.verse) {
                        detectTapGestures(
                            onTap = { onToggle(verse.verse) },
                            onLongPress = { onToggle(verse.verse) }
                        )
                    }
                    .padding(horizontal = AppSpacing.horizontal, vertical = AppSpacing.xs)
            ) {
                if (store.state.verseNumbers) {
                    Text("${verse.verse}", style = AppType.sans(11.sp), color = if (paper) AppColor.inkSoft else AppColor.faint(), modifier = Modifier.width(22.dp))
                }
                Text(
                    store.verseText(verse),
                    style = AppType.serif((19 * store.state.fontScale).sp),
                    color = if (red) (if (paper) AppColor.speech else AppColor.speechNight) else ink,
                    lineHeight = (25 * store.state.fontScale * store.state.lineHeight).sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ChapterExplanationPreview(nav: NavHostController, ref: ChapterRef, ink: androidx.compose.ui.graphics.Color) {
    val store = LocalStore.current
    var guide by remember(ref.id, store.state.locale) { mutableStateOf<ChapterGuide?>(null) }
    LaunchedEffect(ref.id, store.state.locale) {
        guide = withContext(Dispatchers.IO) { ChapterGuideStore.guide(ref.bookId, ref.chapter, store.state.locale) }
    }
    guide?.let { g ->
        Column(
            Modifier.padding(horizontal = AppSpacing.horizontal, vertical = AppSpacing.sm)
                .fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(ink.copy(alpha = 0.05f))
                .clickable { nav.navigate("guide/${encode(ref.bookId)}/${ref.chapter}") }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(store.t(if (g.readingAid) "readingAidTitle" else "explanationPreview"), style = AppType.sans(13.sp, FontWeight.SemiBold), color = ink)
            Text(g.summary, style = AppType.serif(16.sp), color = ink.copy(alpha = 0.8f), maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(store.t("openExplanation") + " →", style = AppType.sans(13.sp, FontWeight.Medium), color = ink)
        }
    }
}

@Composable
fun ChapterGuideView(nav: NavHostController, bookId: String, chapter: Int, showsReadLink: Boolean = false) {
    val store = LocalStore.current
    val paper = !store.night
    var guide by remember(bookId, chapter, store.state.locale) { mutableStateOf<ChapterGuide?>(null) }
    var loading by remember(bookId, chapter, store.state.locale) { mutableStateOf(true) }
    LaunchedEffect(store.state.locale, bookId, chapter) {
        guide = withContext(Dispatchers.IO) { ChapterGuideStore.guide(bookId, chapter, store.state.locale) }
        loading = false
    }
    Column(
        Modifier.fillMaxSize().background(if (paper) AppColor.paper else AppColor.bg()).verticalScroll(rememberScrollState())
            .appColumn().padding(top = AppSpacing.lg, bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.title)) {
            Kicker(store.t("chapterGuide"))
            Text("${store.bookName(bookId)} $chapter", style = AppType.display(34.sp), color = AppColor.fg())
            guide?.let { g ->
                Text(g.title, style = AppType.serif(20.sp), color = AppColor.accent())
                if (g.deuterocanonical) Text(store.t("deuterocanonical"), style = AppType.sans(13.sp, FontWeight.Medium), color = AppColor.muted())
            }
        }
        val g = guide
        if (g != null) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t(if (g.readingAid) "readingAidTitle" else "chapterSummary"))
                if (g.readingAid) Text(store.t("readingAidNote"), style = AppType.sans(14.sp), color = AppColor.muted())
                Text(g.summary, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 24.sp)
            }
            if (g.purpose.isNotBlank()) Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t("chapterPurpose"))
                Text(g.purpose, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 24.sp)
            }
            if (g.themes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Kicker(store.t("keyThemes"))
                    g.themes.forEachIndexed { i, theme ->
                        Column {
                            Text(theme, style = AppType.serif(18.sp), color = AppColor.fg(), modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
                            if (i < g.themes.lastIndex) AppDivider()
                        }
                    }
                }
            }
            if (g.sections.isNotEmpty()) {
                Kicker(store.t("guideSections"))
                g.sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(section.title, style = AppType.serif(21.sp), color = AppColor.fg())
                        Text("${store.bookName(bookId)} $chapter:${section.start}–${section.end}",
                            style = AppType.sans(14.sp, FontWeight.Medium), color = AppColor.accent(),
                            modifier = Modifier.clickable { nav.openReader(ChapterRef(bookId, chapter, section.start)) }.padding(vertical = 12.dp))
                        Text(section.body, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 25.sp)
                    }
                }
                Text(store.t("guideBasis"), style = AppType.sans(13.sp), color = AppColor.muted())
            }
            if (g.questions.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Kicker(store.t("guideQuestions"))
                g.questions.forEach { Text(it, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 25.sp) }
            }
            if (g.context.isNotEmpty()) Disclosure(store.t("guideContext"), g.context)
            if (g.interpretations.isNotEmpty()) Disclosure(store.t("historicalInterpretations"), g.interpretations)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t("churchFathers"))
                if (g.fathers.isEmpty()) {
                    Text(store.t("noFathers"), style = AppType.serif(17.sp), color = AppColor.muted())
                } else {
                    g.fathers.forEachIndexed { i, father ->
                        BoxClick({ nav.navigate("father/${encode(bookId)}/$chapter/${i}") }) {
                            AppListRow(father.father, father.workTitle, showSeparator = i < g.fathers.lastIndex)
                        }
                    }
                }
            }
            if (g.related.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Kicker(store.t("relatedPassages"))
                    g.related.forEachIndexed { i, item ->
                        BoxClick({ nav.openReader(ChapterRef(item.bookId, item.chapter, item.verse)) }) {
                            AppListRow(store.displayedRef(item.bookId, item.chapter, item.verse), showSeparator = i < g.related.lastIndex)
                        }
                    }
                }
            }
            if (showsReadLink) {
                AgapeButton(store.t("readChapter"), AgapeButtonKind.primary) {
                    nav.openReader(ChapterRef(bookId, chapter))
                }
            }
        } else {
            Text(store.t(if (loading) "guideLoading" else "guideEmpty"), style = AppType.serif(18.sp), color = AppColor.muted())
        }
    }
}

@Composable
private fun Disclosure(title: String, text: String) {
    var open by remember { mutableStateOf(false) }
    Column {
        BoxClick({ open = !open }) { Kicker(title) }
        if (open) Text(text, style = AppType.serif(17.sp), color = AppColor.fg(), modifier = Modifier.padding(top = AppSpacing.sm), lineHeight = 22.sp)
    }
}

@Composable
fun FatherCommentaryView(entry: ChurchFatherCommentary, bookId: String, chapter: Int) {
    val store = LocalStore.current
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())
            .appColumn().padding(top = AppSpacing.lg, bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.title)) {
            Kicker(store.t("churchFathers"))
            Text(entry.father, style = AppType.display(32.sp), color = AppColor.fg())
            Text(entry.fatherDescription, style = AppType.sans(15.sp), color = AppColor.muted())
        }
        meta(store.t("work"), entry.workTitle)
        if (entry.approximateDate.isNotEmpty()) meta(store.t("date"), entry.approximateDate)
        if (entry.relatedVerses.isNotEmpty()) meta(store.t("passage"), "${store.bookName(bookId)} $chapter:${entry.relatedVerses}")
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Kicker(store.t("interpretation"))
            Text(entry.commentary, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 24.sp)
            if (entry.paraphrase) Text(store.t("paraphraseNote"), style = AppType.sans(13.sp), color = AppColor.muted())
        }
        if (entry.whyItMatters.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t("whyItMatters"))
                Text(entry.whyItMatters, style = AppType.serif(18.sp), color = AppColor.fg(), lineHeight = 24.sp)
            }
        }
        if (entry.sourceReference.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Kicker(store.t("source"))
                Text(entry.sourceReference, style = AppType.sans(14.sp), color = AppColor.muted())
            }
        }
    }
}

@Composable
private fun meta(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), style = AppType.kicker().copy(letterSpacing = 1.6.sp), color = AppColor.faint())
        Text(value, style = AppType.serif(17.sp), color = AppColor.fg())
    }
}
