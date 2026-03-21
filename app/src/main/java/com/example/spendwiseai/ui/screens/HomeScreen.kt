package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.spendwiseai.presentation.dashboard.DashboardViewModel
import com.example.spendwiseai.ui.components.DoughnutChart
import com.example.spendwiseai.R
import com.example.spendwiseai.core.LocaleManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed

@Composable
fun HomeScreen(
    onAddExpenseClicked: () -> Unit,
    dashboardViewModel: DashboardViewModel
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedCurrency = LocaleManager.getCurrency(context)
    var selectedTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (dashboardState.isLoading) {
                        RowLoading()
                    } else {
                        val balanceColor = if (dashboardState.totalBalance >= 0) NeonGreen else SoftCoralRed

                        StatRow(
                            label = stringResource(id = R.string.total_balance),
                            value = dashboardState.totalBalance,
                            color = balanceColor,
                            currency = selectedCurrency
                        )
                        StatRow(
                            label = stringResource(id = R.string.daily_spending),
                            value = dashboardState.dailySpending,
                            color = SoftCoralRed,
                            currency = selectedCurrency
                        )
                    }
                }
            }
        }

        if (!dashboardState.isLoading) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Finans Grafiklerim", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Giderler") })
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gelirler") })
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Net Durum") })
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        when (selectedTab) {
                            0 -> {
                                if (dashboardState.expenseCategoryTotals.isEmpty()) {
                                    Text("Henuz veri yok")
                                } else {
                                    DoughnutChart(categoryTotals = dashboardState.expenseCategoryTotals)
                                }
                            }
                            1 -> {
                                if (dashboardState.incomeCategoryTotals.isEmpty()) {
                                    Text("Henuz veri yok")
                                } else {
                                    DoughnutChart(categoryTotals = dashboardState.incomeCategoryTotals)
                                }
                            }
                            else -> {
                                if (dashboardState.totalIncome == 0.0 && dashboardState.totalExpense == 0.0) {
                                    Text("Henuz veri yok")
                                } else {
                                    NetBarChart(
                                        income = dashboardState.totalIncome,
                                        expense = dashboardState.totalExpense,
                                        currency = selectedCurrency
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAddExpenseClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Harcama Ekle"
                )
                Spacer(Modifier.width(10.dp))
                Text("Harcama Ekle")
            }
        }
    }
}

@Composable
private fun RowLoading() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StatRow(label: String, value: Double, color: androidx.compose.ui.graphics.Color, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$currency ${String.format("%.2f", value)}",
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
    }
}

@Composable
private fun NetBarChart(income: Double, expense: Double, currency: String) {
    val maxValue = maxOf(income, expense).coerceAtLeast(1.0)
    val incomeRatio = (income / maxValue).toFloat()
    val expenseRatio = (expense / maxValue).toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Gelir vs Gider", style = MaterialTheme.typography.titleSmall)
        Text("Gelir: $currency ${String.format("%.2f", income)}", color = NeonGreen)
        Box(
            modifier = Modifier
                .fillMaxWidth(incomeRatio)
                .height(20.dp)
                .padding(end = 8.dp)
                .background(NeonGreen, RoundedCornerShape(8.dp))
        )
        Text("Gider: $currency ${String.format("%.2f", expense)}", color = SoftCoralRed)
        Box(
            modifier = Modifier
                .fillMaxWidth(expenseRatio)
                .height(20.dp)
                .padding(end = 8.dp)
                .background(SoftCoralRed, RoundedCornerShape(8.dp))
        )
    }
}

