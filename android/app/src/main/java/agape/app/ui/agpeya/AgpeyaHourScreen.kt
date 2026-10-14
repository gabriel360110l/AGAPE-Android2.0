package agape.app.ui.agpeya

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import agape.app.data.AgpeyaHourModel
import agape.app.data.PrayerModel
import agape.app.ui.theme.LocalAgapeColors

@Composable
fun AgpeyaHourScreen(
    hours: List<AgpeyaHourModel>,
    prayers: List<PrayerModel>
) {
    val sacredColors = LocalAgapeColors.current
    var selectedHourIndex by remember { mutableIntStateOf(0) }
    var showIndividualPrayers by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sacredColors.background)
    ) {
        TabRow(
            selectedTabIndex = if (showIndividualPrayers) 1 else 0,
            containerColor = sacredColors.surface,
            contentColor = sacredColors.primaryText
        ) {
            Tab(
                selected = !showIndividualPrayers,
                onClick = { showIndividualPrayers = false },
                text = { Text("Agpeya (Getijden)", style = MaterialTheme.typography.labelLarge) }
            )
            Tab(
                selected = showIndividualPrayers,
                onClick = { showIndividualPrayers = true },
                text = { Text("Orthodoxe Gebeden", style = MaterialTheme.typography.labelLarge) }
            )
        }

        if (!showIndividualPrayers && hours.isNotEmpty()) {
            val activeHour = hours.getOrElse(selectedHourIndex) { hours.first() }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(hours) { idx, hour ->
                    FilterChip(
                        selected = idx == selectedHourIndex,
                        onClick = { selectedHourIndex = idx },
                        label = { Text(hour.titleNl, style = MaterialTheme.typography.labelLarge) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = sacredColors.goldHighlight,
                            selectedLabelColor = sacredColors.primaryText
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeHour.copticTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = sacredColors.sacredGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeHour.titleNl,
                            style = MaterialTheme.typography.headlineLarge,
                            color = sacredColors.primaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeHour.themeNl,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = sacredColors.rubricRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = sacredColors.border)
                    }
                }

                items(activeHour.sections, key = { it.id }) { section ->
                    if (section.type == "kyrie_41") {
                        CanonicalKyrieEleisonCard(
                            rubricNl = section.rubricNl
                                ?: "Vervolgens zegt de gelovige 41 maal 'Kyrie Eleison' (Heer, ontferm U) vóór het Trisagion."
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (section.titleNl.isNotBlank()) {
                                Text(
                                    text = section.titleNl,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = sacredColors.primaryText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            section.rubricNl?.let { rubric ->
                                Text(
                                    text = rubric,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = sacredColors.rubricRed
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            section.bodyNl?.let { body ->
                                Text(
                                    text = body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = sacredColors.primaryText
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(prayers, key = { it.id }) { prayer ->
                    Surface(
                        color = sacredColors.surface,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, sacredColors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = prayer.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = sacredColors.sacredGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prayer.titleNl,
                                style = MaterialTheme.typography.headlineMedium,
                                color = sacredColors.primaryText
                            )
                            if (prayer.sourceFather.isNotBlank()) {
                                Text(
                                    text = prayer.sourceFather,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = sacredColors.secondaryText
                                )
                            }
                            if (prayer.rubricNl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = prayer.rubricNl,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = sacredColors.rubricRed
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = prayer.contentNl,
                                style = MaterialTheme.typography.bodyLarge,
                                color = sacredColors.primaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CanonicalKyrieEleisonCard(
    rubricNl: String,
    targetCount: Int = 41
) {
    val sacredColors = LocalAgapeColors.current
    val view = LocalView.current
    var currentCount by remember { mutableIntStateOf(0) }

    Surface(
        color = sacredColors.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, sacredColors.sacredGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Ⲕⲥⲡⲉⲛⲁ̀ ⲛ̀ⲗⲛⲙ̀ⲟⲟⲛⲟⲝ • KYRIE ELEISON (41×)",
                style = MaterialTheme.typography.labelSmall,
                color = sacredColors.sacredGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rubricNl,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = sacredColors.rubricRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { currentCount / targetCount.toFloat() },
                color = sacredColors.rubricRed,
                trackColor = sacredColors.border,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { currentCount = 0 },
                    border = BorderStroke(1.dp, sacredColors.border)
                ) {
                    Text("Reset", color = sacredColors.secondaryText)
                }
                Button(
                    onClick = {
                        if (currentCount < targetCount) {
                            currentCount++
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sacredColors.rubricRed,
                        contentColor = sacredColors.background
                    )
                ) {
                    Text(
                        text = if (currentCount < targetCount) {
                            "Heer, ontferm U ($currentCount / $targetCount)"
                        } else {
                            "Voltooid (41 / 41) — Amen"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
