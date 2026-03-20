package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.presentation.insights.InsightsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBackToHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Smart Insights", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            TextButton(onClick = onBackToHome) { Text("Back to home") }
        }

        if (state.isLoading && state.insights.isEmpty()) {
            item {
                Text(
                    "Generating insights…",
                    style = MaterialTheme.typography.bodyMedium
                )
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
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return "Week of ${sdf.format(Date(weekStartMillis))}"
}

