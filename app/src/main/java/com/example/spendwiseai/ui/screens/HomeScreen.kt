package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import com.example.spendwiseai.presentation.dashboard.DashboardViewModel
import com.example.spendwiseai.ui.components.DoughnutChart
import com.example.spendwiseai.core.LocaleManager
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.spendwiseai.R

@Composable
fun HomeScreen(
    onScanReceiptClicked: () -> Unit,
    onAddExpenseClicked: () -> Unit,
    dashboardViewModel: DashboardViewModel
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isTurkish = LocaleManager.getLanguageTag(context) == "tr"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart Spend & Save",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = isTurkish,
                onCheckedChange = { checked ->
                    val newTag = if (checked) "tr" else "en"
                    LocaleManager.setLanguageTag(context, newTag)
                    (context as? Activity)?.recreate()
                }
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (dashboardState.isLoading) {
                    RowLoading()
                } else {
                    val balanceColor =
                        if (dashboardState.totalBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                    StatRow(
                        label = stringResource(id = R.string.total_balance),
                        value = dashboardState.totalBalance,
                        color = balanceColor
                    )
                    StatRow(
                        label = stringResource(id = R.string.daily_spending),
                        value = dashboardState.dailySpending,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        if (!dashboardState.isLoading) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category Distribution", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    DoughnutChart(categoryTotals = dashboardState.categoryTotals)
                }
            }
        }

        // Visual expense entry
        OutlinedButton(
            onClick = onScanReceiptClicked,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = stringResource(id = R.string.scan_receipt)
            )
            Spacer(Modifier.width(10.dp))
            Text(stringResource(id = R.string.scan_receipt))
        }

        Button(onClick = onAddExpenseClicked) { Text(stringResource(id = R.string.add)) }
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
private fun StatRow(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
    }
}

