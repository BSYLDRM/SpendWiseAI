package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.presentation.insights.InsightsViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBackToHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Akilli Ipuclari", style = MaterialTheme.typography.headlineMedium)
                IconButton(
                    onClick = viewModel::refreshInsights,
                    enabled = !state.isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ipuclarini yenile")
                }
            }
        }
        item {
            TextButton(onClick = onBackToHome) { Text("Ana sayfaya don") }
        }

        if (state.isLoading && state.insights.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Ilk ipucun uretiliyor...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(state.insights.size) { index ->
            val insight = state.insights[index]
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = formatWeek(insight.weekStartMillis),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(insight.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun formatWeek(weekStartMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val thisWeekStart = Instant.now()
        .atZone(zone)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toLocalDate()
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()
    if (weekStartMillis == thisWeekStart) return "Bu Hafta"

    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return "Hafta: ${sdf.format(Date(weekStartMillis))}"
}

