package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.R
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.presentation.budget.BudgetViewModel
import com.example.spendwiseai.ui.components.categoryLocalizedName
import com.example.spendwiseai.ui.components.expenseCategoryColor
import com.example.spendwiseai.ui.theme.AppBackground
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed

@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currency = LocaleManager.getCurrency(context)

    var budgetInput by remember(state.monthKey) {
        mutableStateOf(if (state.budgetAmount > 0) state.budgetAmount.toInt().toString() else "")
    }

    val progress = (state.spentPercent / 100.0).toFloat().coerceIn(0f, 1f)
    val progressColor = when {
        state.spentPercent <= 60  -> NeonGreen
        state.spentPercent <= 85  -> Color(0xFFFFD54F)
        state.spentPercent <= 100 -> SoftCoralRed
        else                      -> Color.Red
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Başlık
        item {
            Text(
                text = stringResource(R.string.budget_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Ay seçici
        item {
            Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::goPrevMonth) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.prev_month))
                    }
                    Text(state.monthLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = viewModel::goNextMonth) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
                    }
                }
            }
        }

        // Bütçe girişi
        item {
            Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.set_monthly_budget), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.budget_amount_hint)) },
                            placeholder = { Text("5000") },
                            suffix = { Text(currency) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                val amount = budgetInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                                viewModel.saveBudget(amount)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text(stringResource(R.string.save), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (state.budgetAmount > 0.0) {
            item {
                Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(AppBackground, Color(0xFF0D1117))), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        stringResource(R.string.percent_used, state.spentPercent.toInt()),
                                        color = progressColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        when {
                                            state.spentPercent > 100 -> stringResource(R.string.budget_over)
                                            state.spentPercent > 85  -> stringResource(R.string.budget_warn)
                                            else                     -> stringResource(R.string.budget_ok)
                                        },
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                                Text("${state.spentPercent.toInt()}%", color = progressColor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))) {
                                Box(modifier = Modifier.fillMaxWidth(progress).height(14.dp).background(
                                    Brush.horizontalGradient(listOf(progressColor, progressColor.copy(alpha = 0.7f))), RoundedCornerShape(50)
                                ))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMiniCard(
                                    label = stringResource(R.string.spent_label),
                                    value = "${"%.0f".format(state.spentAmount)} $currency",
                                    color = SoftCoralRed,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMiniCard(
                                    label = stringResource(R.string.budget_label),
                                    value = "${"%.0f".format(state.budgetAmount)} $currency",
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMiniCard(
                                    label = stringResource(R.string.remaining_label),
                                    value = "${"%.0f".format(state.remainingAmount)} $currency",
                                    color = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.category_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(state.categoryTotals) { item ->
                val categoryColor = expenseCategoryColor(item.categoryName)
                val categoryPercent = if (state.spentAmount > 0) (item.totalAmount / state.spentAmount * 100).toInt() else 0
                val barRatio = if (state.spentAmount > 0) (item.totalAmount / state.spentAmount).toFloat().coerceIn(0f, 1f) else 0f

                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(categoryColor))
                                Spacer(Modifier.width(8.dp))
                                Text(categoryLocalizedName(item.categoryName), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${"%.0f".format(item.totalAmount)} $currency", color = categoryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("$categoryPercent%", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(50))) {
                            Box(modifier = Modifier.fillMaxWidth(barRatio).height(6.dp).background(categoryColor, RoundedCornerShape(50)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}