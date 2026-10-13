package agape.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import agape.app.data.AppLocale
import agape.app.data.BibleDatabase
import agape.app.data.ChapterGuideStore
import agape.app.data.ChapterRef
import agape.app.data.GuideSearchHit
import agape.app.data.ScriptureLookup
import agape.app.data.SearchHit
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.AppTypography
import agape.app.theme.LocalReduceMotion
import agape.app.ui.components.AppScreenColumn
import agape.app.ui.components.AppSearchField
import agape.app.ui.components.ReadingRow
import agape.app.ui.components.ScreenHeader
import agape.app.ui.components.SearchResultRow
import agape.app.ui.components.SearchSuggestionRow
import agape.app.ui.components.SectionHeader
import agape.app.ui.home.BoxClick
import agape.app.ui.navigation.openReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchView(nav: NavHostController) {
    val store = LocalStore.current
    val reduce = LocalReduceMotion.current
    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf(emptyList<SearchHit>()) }
    var guideHits by remember { mutableStateOf(emptyList<GuideSearchHit>()) }
    var lookup by remember { mutableStateOf<ChapterRef?>(null) }
    var searching by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    val active = query.trim()
    val showing = active.length >= 2
    LaunchedEffect(Unit) { reveal = true }
    LaunchedEffect(query, store.state.locale) {
        lookup = ScriptureLookup.parse(query)
        val q = query.trim()
        if (q.length < 2) {
            hits = emptyList(); guideHits = emptyList(); searching = false
            return@LaunchedEffect
        }
        searching = true
        val english = store.state.locale == AppLocale.en
        val found = withContext(Dispatchers.IO) { BibleDatabase.search(q, english) }
        val guides = if (q.length >= 3) withContext(Dispatchers.IO) { ChapterGuideStore.search(q, locale = store.state.locale) } else emptyList()
        hits = found
        guideHits = guides
        searching = false
    }
    Column(
        Modifier.fillMaxSize().background(AppColor.bg()).verticalScroll(rememberScrollState())
    ) {
        AppScreenColumn(spacing = AppSpacing.lg) {
            ScreenHeader(store.t("search"), store.t("search"))
            AppSearchField(store.t("searchPlaceholder"), query) { query = it }
            lookup?.let { ref ->
                BoxClick({ nav.openReader(ref) }) {
                    ReadingRow(
                        store.t("goTo"),
                        "${store.bookName(ref.bookId)} ${ref.chapter}${ref.verse?.let { ":$it" } ?: ""}",
                        showSeparator = false
                    )
                }
            }
            if (showing) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                    if (searching) {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            CircularProgressIndicator(color = AppColor.accent())
                            Text(store.t("searching"), style = AppTypography.secondary(), color = AppColor.muted())
                        }
                    } else {
                        SectionHeader(store.t("searchResults"))
                        Text("${hits.size} ${store.t("searchHits")}", style = AppTypography.caption(), color = AppColor.muted())
                        if (hits.isEmpty()) {
                            Text(store.t("noSearchResults"), style = AppTypography.secondary(), color = AppColor.muted(), modifier = Modifier.padding(top = AppSpacing.xs))
                        } else {
                            hits.forEachIndexed { i, hit ->
                                BoxClick({
                                    store.pushQuery(query)
                                    nav.openReader(ChapterRef(hit.bookId, hit.chapter, hit.verse))
                                }) {
                                    SearchResultRow("${store.bookName(hit.bookId)} ${hit.chapter}:${hit.verse}", hit.text, query, i < hits.lastIndex)
                                }
                            }
                        }
                        if (guideHits.isNotEmpty()) {
                            SectionHeader(store.t("chapterGuides"))
                            Text(store.t("guideSearchLead"), style = AppTypography.caption(), color = AppColor.muted())
                            guideHits.forEachIndexed { i, hit ->
                                BoxClick({ nav.navigate("guide/${hit.bookId}/${hit.chapter}") }) {
                                    SearchResultRow("${store.bookName(hit.bookId)} ${hit.chapter} · ${hit.title}", hit.snippet, showSeparator = i < guideHits.lastIndex)
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                    Text(store.t("searchHint"), style = AppType.serifItalic(16.sp), color = AppColor.faint())
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                        SectionHeader(store.t("try"))
                        val sug = store.catalog.searchSuggestions(store.state.locale)
                        sug.forEachIndexed { i, item ->
                            BoxClick({ query = item }) {
                                SearchSuggestionRow(item, i < sug.lastIndex, reveal || reduce, i * 55)
                            }
                        }
                    }
                    if (store.state.recentQueries.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionHeader)) {
                            SectionHeader(store.t("recentSearches"))
                            store.state.recentQueries.forEachIndexed { i, item ->
                                BoxClick({ query = item }) {
                                    SearchSuggestionRow(item, i < store.state.recentQueries.lastIndex, reveal || reduce, 120 + i * 45)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
