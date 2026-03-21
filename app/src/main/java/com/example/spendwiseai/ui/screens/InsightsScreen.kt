package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.presentation.insights.InsightsViewModel
import androidx.compose.ui.platform.LocalFocusManager
import com.example.spendwiseai.ui.theme.AppBackground
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
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
    val focusManager = LocalFocusManager.current
    val latest = state.insights.firstOrNull()
    val balance = extractBalanceFromReport(latest?.content.orEmpty())

    LaunchedEffect(Unit) {
        listState.scrollToItem(0)
    }
    LaunchedEffect(state.insights.firstOrNull()?.createdAtMillis) {
        if (state.insights.isNotEmpty()) listState.animateScrollToItem(0)
    }

    androidx.compose.material3.Scaffold(
        bottomBar = {
            Button(
                onClick = viewModel::refreshInsight,
                enabled = !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    Text(" AI analiz ediyor...")
                } else {
                    Text("🔄 Yeni Rapor Üret")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(AppBackground, AppBackground.copy(alpha = 0.85f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AI Finans Koçun 🤖", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "${"%.2f".format(balance)} TL",
                                color = if (balance >= 0) NeonGreen else SoftCoralRed,
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Text(
                                text = "Son analiz: ${latest?.createdAtMillis?.let { formatReportDate(it) } ?: "-"}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            item {
                val report = latest?.content.orEmpty()
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.isGenerating) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                                Text(" AI analiz ediyor...")
                            }
                        }
                        val sections = buildSections(report)
                        sections.forEachIndexed { index, section ->
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = section.body,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp)
                            )
                            if (index != sections.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                        Text(
                            text = "Olusturulma tarihi: ${
                                latest?.createdAtMillis?.let { formatReportDate(it) } ?: "-"
                            }",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

private fun formatReportDate(createdAtMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("tr-TR"))
    return sdf.format(Date(createdAtMillis))
}

private data class ReportSection(val title: String, val body: String)

private fun buildSections(report: String): List<ReportSection> {
    val titles = listOf(
        "💰 GENEL DURUM",
        "📊 HARCAMA ANALİZİ",
        "⚠️ DİKKAT EDİLMESİ GEREKENLER",
        "🎯 BU AY İÇİN HEDEFLER",
        "💡 TASARRUF ÖNERİLERİ"
    )
    return titles.map { title ->
        val body = extractSectionBody(report, title, titles)
        ReportSection(title = title, body = body.ifBlank { "Henüz rapor üretilmedi." })
    }
}

private fun extractSectionBody(report: String, title: String, titles: List<String>): String {
    val start = report.indexOf(title)
    if (start == -1) return ""
    val contentStart = start + title.length
    val nextStarts = titles
        .filter { it != title }
        .map { report.indexOf(it, contentStart) }
        .filter { it != -1 }
    val end = nextStarts.minOrNull() ?: report.length
    return report.substring(contentStart, end).trim()
}

private fun extractBalanceFromReport(report: String): Double {
    val regex = Regex("""-?\d+[.,]?\d*""")
    return regex.find(report)?.value?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
}

