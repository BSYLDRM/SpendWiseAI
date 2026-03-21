package com.example.spendwiseai.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.spendwiseai.R
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.ui.navigation.SpendWiseNavHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import com.example.spendwiseai.core.LocaleManager

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpendWiseApp() {
    val context = LocalContext.current
    val appContainer = remember(context) { AppContainer(context) }
    val navController = rememberNavController()
    var showSettings by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(LocaleManager.getLanguageTag(context)) }
    var selectedCurrency by remember { mutableStateOf(LocaleManager.getCurrency(context)) }

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    selected = currentRoute == Routes.Home,
                    onClick = { navController.navigateToBottomTab(Routes.Home) },
                    icon = Icons.Default.Home,
                    label = "Ana Sayfa"
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Expenses,
                    onClick = { navController.navigateToBottomTab(Routes.Expenses) },
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = stringResource(id = R.string.expense)
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Incomes,
                    onClick = { navController.navigateToBottomTab(Routes.Incomes) },
                    icon = Icons.Default.AccountBalanceWallet,
                    label = stringResource(id = R.string.income)
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Insights,
                    onClick = { navController.navigateToBottomTab(Routes.Insights) },
                    icon = Icons.Default.Analytics,
                    label = "Ipuclari"
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Budget,
                    onClick = { navController.navigateToBottomTab(Routes.Budget) },
                    icon = Icons.Default.Flag,
                    label = "Butce"
                )
                BottomNavItem(
                    selected = false,
                    onClick = { showSettings = true },
                    icon = Icons.Default.Settings,
                    label = "Ayarlar"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SpendWiseNavHost(
                navController = navController,
                appContainer = appContainer
            )
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dil", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                LanguageOptionRow(
                    label = "TR 🇹🇷",
                    selected = selectedLanguage == "tr",
                    onSelect = {
                        selectedLanguage = "tr"
                        LocaleManager.setLanguageTag(context, "tr")
                        (context as? Activity)?.recreate()
                    }
                )
                HorizontalDivider()
                LanguageOptionRow(
                    label = "EN 🇬🇧",
                    selected = selectedLanguage == "en",
                    onSelect = {
                        selectedLanguage = "en"
                        LocaleManager.setLanguageTag(context, "en")
                        (context as? Activity)?.recreate()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Para Birimi", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                CurrencyOptionRow(
                    label = "TL",
                    selected = selectedCurrency == "TL",
                    onSelect = {
                        selectedCurrency = "TL"
                        LocaleManager.setCurrency(context, "TL")
                    }
                )
                HorizontalDivider()
                CurrencyOptionRow(
                    label = "USD",
                    selected = selectedCurrency == "USD",
                    onSelect = {
                        selectedCurrency = "USD"
                        LocaleManager.setCurrency(context, "USD")
                    }
                )
                HorizontalDivider()
                CurrencyOptionRow(
                    label = "EUR",
                    selected = selectedCurrency == "EUR",
                    onSelect = {
                        selectedCurrency = "EUR"
                        LocaleManager.setCurrency(context, "EUR")
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private object Routes {
    const val Home = "home"
    const val Expenses = "expenses"
    const val Incomes = "incomes"
    const val Insights = "insights"
    const val Budget = "budget"
}

private fun androidx.navigation.NavHostController.navigateToBottomTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Black)
        RadioButton(selected = selected, onClick = onSelect)
    }
}

@Composable
private fun CurrencyOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Black)
        RadioButton(selected = selected, onClick = onSelect)
    }
}

