package agape.app.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.BookMeta
import agape.app.data.ChapterRef
import agape.app.data.Testament
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.Haptics
import agape.app.ui.components.AppListRow
import agape.app.ui.components.AppScreenColumn
import agape.app.ui.components.AppSearchField
import agape.app.ui.components.BibleBookCard
import agape.app.ui.components.GlassIconButton
import agape.app.ui.components.GlassScrollMenu
import agape.app.ui.components.HeroBand
import agape.app.ui.components.Kicker
import agape.app.ui.components.MeaningCard
import agape.app.ui.components.SectionHeader
import agape.app.ui.components.appColumn
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.encode
import agape.app.ui.navigation.openReader

@Composable
fun BibleView(nav: NavHostController) {
    val store = LocalStore.current
    val context = LocalContext.current
    var part by remember { mutableStateOf("ot") }
    var query by remember { mutableStateOf("") }
    val needle = query.trim().lowercase()
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())
    ) {
        HeroBand(if (part == "kerk") "church" else "galilee", minHeight = 280.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        Kicker(if (part == "kerk") store.t("church") else store.t("scripture"), AppColor.onPhoto.copy(0.72f))
                        Text(if (part == "kerk") store.t("church") else store.t("bible"), style = AppType.display(36.sp), color = AppColor.onPhoto)
                    }
                    GlassIconButton(Icons.Outlined.Search, store.t("searchAria"), onPhoto = true) {
                        nav.navigate("search")
                    }
                }
                if (part == "kerk") {
                    Text(store.t("churchLead"), style = AppType.sans(15.sp), color = AppColor.onPhoto.copy(0.78f))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.button)) {
                        HeroAction(store.t("fromBeginning"), true) {
                            nav.openReader(store.catalog.firstChapter(store.state.tradition))
                        }
                        HeroAction(store.t("randomChapter"), false) {
                            nav.openReader(store.catalog.randomChapter(store.state.tradition))
                        }
                    }
                }
            }
        }
        AppScreenColumn(spacing = AppSpacing.lg) {
            GlassScrollMenu(
                listOf("ot" to store.t("ot"), "nt" to store.t("nt"), "kerk" to store.t("church")),
                part
            ) { part = it }
            if (part != "kerk") {
                BoxClick({ nav.navigate("harmony") }) { AppListRow(store.t("harmony"), showSeparator = false) }
            }
            AppSearchField(if (part == "kerk") store.t("church") else store.t("filterBooks"), query) { query = it }
            if (part == "kerk") {
                val rows = store.catalog.churchChildren("root").filter { node ->
                    if (needle.isEmpty()) true
                    else "${node.title.nl} ${node.title.en} ${node.teaser.nl} ${node.teaser.en}".lowercase().contains(needle)
                }
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.card)) {
                    rows.forEach { node ->
                        val meaning = store.catalog.churchMeaning(node.id)
                        BoxClick({ nav.navigate("church/${encode(node.id)}") }) {
                            MeaningCard(
                                kicker = store.t("churchFolder"),
                                term = node.title.text(store.state.locale),
                                original = meaning?.original,
                                translit = meaning?.translit,
                                meaning = node.teaser.text(store.state.locale),
                                compact = true
                            )
                        }
                    }
                }
            } else {
                val testament = if (part == "nt") Testament.nt else Testament.ot
                val visible = store.catalog.books(store.state.tradition)
                val groups = store.catalog.groups.filter { it.testament == testament }
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.section)) {
                    groups.forEach { group ->
                        val books = visible.filter { book ->
                            if (book.group != group.id) return@filter false
                            if (needle.isEmpty()) true
                            else book.name.lowercase().contains(needle)
                                || store.catalog.bookName(book, store.state.locale).lowercase().contains(needle)
                                || book.shortName.lowercase().contains(needle)
                                || book.teaser.lowercase().contains(needle)
                                || (book.teaserEn ?: "").lowercase().contains(needle)
                        }
                        if (books.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                                SectionHeader(store.catalog.groupTitle(group.id, store.state.locale))
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.card)) {
                                    books.forEach { book ->
                                        BoxClick({
                                            Haptics.light(context)
                                            nav.navigate("book/${encode(book.id)}")
                                        }) {
                                            BibleBookCard(book, store.catalog.bookName(book, store.state.locale), store.readCount(book.id))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroAction(title: String, filled: Boolean, onClick: () -> Unit) {
    val shape = AppRadius.mediumShape()
    Text(
        title,
        style = AppType.sans(14.sp),
        color = if (filled) AppColor.dusk else AppColor.onPhoto,
        modifier = Modifier
            .clip(shape)
            .background(if (filled) AppColor.onPhoto else Color.Transparent)
            .then(if (!filled) Modifier.border(1.dp, AppColor.onPhoto.copy(0.28f), shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = 12.dp)
    )
}

@Composable
fun BookChaptersView(nav: NavHostController, book: BookMeta) {
    val store = LocalStore.current
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState()).appColumn().padding(bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Kicker(book.greek)
        Text(store.catalog.bookName(book, store.state.locale), style = AppType.display(36.sp), color = AppColor.fg())
        Text(store.catalog.bookIntro(book, store.state.locale), style = AppType.serif(18.sp), color = AppColor.muted())
        LinearProgressIndicator(
            progress = { store.bookProgress(book.chapters, book.id).toFloat() },
            modifier = Modifier.fillMaxWidth().height(3.dp).padding(bottom = AppSpacing.xs),
            color = AppColor.accent(),
            trackColor = AppColor.line()
        )
        val cols = 5
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..book.chapters).chunked(cols).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { n ->
                        val read = store.isRead(book.id, n)
                        val shape = AppRadius.md
                        Box(
                            Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(AppRadius.mediumShape())
                                .background(if (read) AppColor.accent() else AppColor.surface())
                                .then(if (!read) Modifier.border(1.dp, AppColor.hairline(), AppRadius.mediumShape()) else Modifier)
                                .clickable {
                                    Haptics.light(context)
                                    nav.openReader(ChapterRef(book.id, n))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$n", style = AppType.sans(16.sp), color = if (read) AppColor.accentFg() else AppColor.fg())
                        }
                    }
                    repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
