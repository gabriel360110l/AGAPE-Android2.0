package agape.app.ui.tradition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import agape.app.data.TraditionDocument
import agape.app.ui.theme.LocalAgapeColors

@Composable
fun TraditionDocsScreen(
    councilsDoc: TraditionDocument,
    fathersDoc: TraditionDocument,
    initialSearchQuery: String = ""
) {
    val sacredColors = LocalAgapeColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery) }

    val activeDoc = if (selectedTab == 0) fathersDoc else councilsDoc
    val filteredSections = remember(activeDoc, searchQuery) {
        if (searchQuery.isBlank()) {
            activeDoc.sections
        } else {
            activeDoc.sections.filter {
                it.heading.contains(searchQuery, ignoreCase = true) ||
                    it.body.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sacredColors.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = sacredColors.surface,
            contentColor = sacredColors.primaryText
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Kerkvaders", style = MaterialTheme.typography.labelLarge) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("7 Oecumenische Concilies", style = MaterialTheme.typography.labelLarge) }
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Zoek op Kerkvader, concilie, jaartal of canon...") },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = sacredColors.sacredGold)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredSections) { section ->
                Surface(
                    color = sacredColors.surface,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, sacredColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = section.heading,
                            style = if (section.level <= 2) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            color = sacredColors.rubricRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = section.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = sacredColors.primaryText
                        )
                    }
                }
            }
        }
    }
}
