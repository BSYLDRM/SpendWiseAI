package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.presentation.insights.InsightsViewModel
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

    // Yeni rapor gelince en üste scroll
    LaunchedEffect(latest?.createdAtMillis) {
        if (state.insights.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = viewModel::refreshInsight,
                enabled = !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Text("  AI analiz ediyor...", color = Color.White)
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
            // Üst başlık kartı
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
                            Text(
                                "AI Finans Koçun 🤖",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (latest != null) {
                                Text(
                                    text = "Son analiz: ${formatReportDate(latest.createdAtMillis)}",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else {
                                Text(
                                    text = "Henüz analiz yapılmadı",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Rapor yoksa boş durum
            if (latest == null && !state.isGenerating) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🤖", fontSize = 48.sp)
                        Text(
                            text = "Henüz rapor yok",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Rapor üretmek için aşağıdaki butona bas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Rapor üretiliyorsa sadece buton spinner gösterir, burada gösterme
            // Rapor varsa göster
            if (latest != null) {
                item {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = latest.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 26.sp
                                ),
                                softWrap = true,
                                overflow = TextOverflow.Visible
                            )
                            HorizontalDivider()
                            Text(
                                text = "Oluşturulma: ${formatReportDate(latest.createdAtMillis)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatReportDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR"))
    return sdf.format(Date(millis))
}