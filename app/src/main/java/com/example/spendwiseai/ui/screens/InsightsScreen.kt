package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.R
import com.example.spendwiseai.presentation.insights.InsightsViewModel
import com.example.spendwiseai.ui.theme.AppBackground
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

    LaunchedEffect(latest?.createdAtMillis) {
        if (state.insights.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = viewModel::refreshInsight,
                enabled = !state.isGenerating,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(52.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Text(stringResource(R.string.ai_analyzing), color = Color.White)
                } else {
                    Text(stringResource(R.string.generate_report_btn))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = AppBackground), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(AppBackground, AppBackground.copy(alpha = 0.85f))))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.ai_coach_title), color = Color.White, style = MaterialTheme.typography.titleLarge)
                            if (latest != null) {
                                Text(
                                    text = stringResource(R.string.last_analysis, formatReportDate(latest.createdAtMillis)),
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else {
                                Text(stringResource(R.string.no_analysis_yet), color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            if (latest == null && !state.isGenerating) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🤖", fontSize = 48.sp)
                        Text(stringResource(R.string.no_report_yet), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text(stringResource(R.string.generate_report_hint), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }

            if (latest != null) {
                item {
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = latest.content,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp),
                                softWrap = true,
                                overflow = TextOverflow.Visible
                            )
                            HorizontalDivider()
                            Text(
                                text = stringResource(R.string.report_created_at, formatReportDate(latest.createdAtMillis)),
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
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}