package agape.app.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import agape.app.data.BibleVerseUiModel
import agape.app.data.ChapterExplanation
import agape.app.ui.theme.LocalAgapeColors

@Composable
fun BibleReaderScreen(
    bookTitle: String,
    chapterNumber: Int,
    chapterExplanation: ChapterExplanation,
    verses: List<BibleVerseUiModel>,
    onNavigateToFather: (String) -> Unit = {}
) {
    val sacredColors = LocalAgapeColors.current
    var showCommentary by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sacredColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$bookTitle $chapterNumber",
                style = MaterialTheme.typography.headlineMedium,
                color = sacredColors.primaryText
            )
            TextButton(onClick = { showCommentary = !showCommentary }) {
                Text(
                    if (showCommentary) "Verberg uitleg" else "Toon uitleg †",
                    color = sacredColors.sacredGold
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = chapterExplanation.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = sacredColors.rubricRed
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = chapterExplanation.overview,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = sacredColors.secondaryText
                )
            }

            items(verses) { verse ->
                Column {
                    Text(
                        text = "${verse.verseNumber}. ${verse.text}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = sacredColors.primaryText
                    )
                    if (showCommentary && verse.commentary != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = verse.commentary.summaryNl,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = sacredColors.secondaryText
                        )
                        verse.commentary.patristicQuotes.forEach { quote ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "— ${quote.fatherName}${if (quote.workTitle.isNotBlank()) " (${quote.workTitle})" else ""}: ${quote.quoteText}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = sacredColors.rubricRed,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
