package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.R
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.presentation.dashboard.DashboardViewModel
import com.example.spendwiseai.ui.components.DoughnutChart
import com.example.spendwiseai.ui.theme.AppBackground
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Başlık
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.greeting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bakiye kartı
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AppBackground, Color(0xFF0D1117))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    if (dashboardState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonGreen)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(
                                    stringResource(R.string.total_balance),
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$selectedCurrency ${String.format("%.2f", dashboardState.totalBalance)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dashboardState.totalBalance >= 0) NeonGreen else SoftCoralRed
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Gelir kutusu
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(NeonGreen.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.TrendingUp, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(Modifier.width(6.dp))
                                            Text(stringResource(R.string.income), color = Color.Gray, fontSize = 12.sp)
                                        }
                                        Text(
                                            "$selectedCurrency ${String.format("%.2f", dashboardState.totalIncome)}",
                                            color = NeonGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Gider kutusu
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SoftCoralRed.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(SoftCoralRed.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.TrendingDown, null, tint = SoftCoralRed, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(Modifier.width(6.dp))
                                            Text(stringResource(R.string.expense), color = Color.Gray, fontSize = 12.sp)
                                        }
                                        Text(
                                            "$selectedCurrency ${String.format("%.2f", dashboardState.totalExpense)}",
                                            color = SoftCoralRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            // Bugünkü harcama
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.todays_spending), color = Color.Gray, fontSize = 13.sp)
                                Text(
                                    "$selectedCurrency ${String.format("%.2f", dashboardState.dailySpending)}",
                                    color = SoftCoralRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grafik kartı
        if (!dashboardState.isLoading) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.financial_chart),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))

                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text(stringResource(R.string.expenses_tab), fontSize = 12.sp) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text(stringResource(R.string.incomes_tab), fontSize = 12.sp) }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text(stringResource(R.string.net_tab), fontSize = 12.sp) }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        when (selectedTab) {
                            0 -> {
                                if (dashboardState.expenseCategoryTotals.isEmpty()) {
                                    EmptyChart(stringResource(R.string.no_expense_data))
                                } else {
                                    DoughnutChart(categoryTotals = dashboardState.expenseCategoryTotals, isIncome = false)
                                }
                            }
                            1 -> {
                                if (dashboardState.incomeCategoryTotals.isEmpty()) {
                                    EmptyChart(stringResource(R.string.no_income_data))
                                } else {
                                    DoughnutChart(categoryTotals = dashboardState.incomeCategoryTotals, isIncome = true)
                                }
                            }
                            2 -> {
                                if (dashboardState.totalIncome == 0.0 && dashboardState.totalExpense == 0.0) {
                                    EmptyChart(stringResource(R.string.no_data_yet))
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

        // İşlem Ekle butonu
        item {
            Button(
                onClick = onAddExpenseClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.add_transaction),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyChart(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray)
    }
}

@Composable
private fun NetBarChart(income: Double, expense: Double, currency: String) {
    val maxValue = maxOf(income, expense).coerceAtLeast(1.0)
    val incomeRatio = (income / maxValue).toFloat().coerceIn(0.05f, 1f)
    val expenseRatio = (expense / maxValue).toFloat().coerceIn(0.05f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.income_vs_expense), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        // Gelir bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.income), color = NeonGreen, fontSize = 13.sp)
                }
                Text("$currency ${String.format("%.2f", income)}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(50))) {
                Box(modifier = Modifier.fillMaxWidth(incomeRatio).height(14.dp).background(
                    Brush.horizontalGradient(listOf(NeonGreen, NeonGreen.copy(alpha = 0.7f))), RoundedCornerShape(50)
                ))
            }
        }

        // Gider bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingDown, null, tint = SoftCoralRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.expense), color = SoftCoralRed, fontSize = 13.sp)
                }
                Text("$currency ${String.format("%.2f", expense)}", color = SoftCoralRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(SoftCoralRed.copy(alpha = 0.15f), RoundedCornerShape(50))) {
                Box(modifier = Modifier.fillMaxWidth(expenseRatio).height(14.dp).background(
                    Brush.horizontalGradient(listOf(SoftCoralRed, SoftCoralRed.copy(alpha = 0.7f))), RoundedCornerShape(50)
                ))
            }
        }

        // Net durum
        val net = income - expense
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (net >= 0) NeonGreen.copy(alpha = 0.1f) else SoftCoralRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.net_status), color = Color.Gray, fontSize = 13.sp)
                Text(
                    "${if (net >= 0) "+" else ""}$currency ${String.format("%.2f", net)}",
                    color = if (net >= 0) NeonGreen else SoftCoralRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}