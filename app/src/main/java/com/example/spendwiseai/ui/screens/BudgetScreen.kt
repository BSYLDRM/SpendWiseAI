package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.presentation.budget.BudgetViewModel
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed

@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.uiState.collectAsState()
    var budgetInput by remember(state.monthKey) { mutableStateOf(if (state.budgetAmount > 0) state.budgetAmount.toString() else "") }
    val progress = (state.spentPercent / 100.0).toFloat().coerceIn(0f, 1f)
    val progressColor = when {
        state.spentPercent <= 60 -> NeonGreen
        state.spentPercent <= 85 -> Color(0xFFFFD54F)
        state.spentPercent <= 100 -> SoftCoralRed
        else -> Color.Red
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::goPrevMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Onceki ay")
                }
                Text(state.monthLabel, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = viewModel::goNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
                }
            }
        }
        item {
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bu ay icin butcen") },
                placeholder = { Text("2000") },
                suffix = { Text("TL") }
            )
        }
        item {
            Button(
                onClick = {
                    val amount = budgetInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                    viewModel.saveBudget(amount)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }
        }
        if (state.budgetAmount > 0.0) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = progressColor
                        )
                        Text("%${state.spentPercent.toInt()}")
                        Text("Harcanan: ${"%.2f".format(state.spentAmount)} TL / Butce: ${"%.2f".format(state.budgetAmount)} TL")
                        Text("Kalan: ${"%.2f".format(state.remainingAmount)} TL")
                        if (state.spentPercent > 100) {
                            Text("⚠️ Butceni astin!", color = Color.Red)
                        }
                    }
                }
            }
            items(state.categoryTotals.size) { idx ->
                val item = state.categoryTotals[idx]
                val emoji = when (item.categoryName) {
                    "Groceries" -> "🛒"
                    "Food & Drink" -> "🍕"
                    "Transportation" -> "🚗"
                    "Shopping" -> "🛍️"
                    "Health" -> "💊"
                    "Bills & Utilities" -> "📱"
                    "Education" -> "📚"
                    "Entertainment" -> "🎬"
                    else -> "📦"
                }
                Text("$emoji ${item.categoryName}: ${"%.2f".format(item.totalAmount)} TL")
            }
        }
    }
}
