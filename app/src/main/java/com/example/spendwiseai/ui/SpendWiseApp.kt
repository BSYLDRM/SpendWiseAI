package com.example.spendwiseai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.spendwiseai.R
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.ui.navigation.SpendWiseNavHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong

@Composable
fun SpendWiseApp() {
    val context = LocalContext.current
    val appContainer = remember(context) { AppContainer(context) }
    val navController = rememberNavController()

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
                    onClick = { navController.navigate(Routes.Home) },
                    icon = Icons.Default.Home,
                    label = "Home"
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Expenses,
                    onClick = { navController.navigate(Routes.Expenses) },
                    icon = Icons.Default.ReceiptLong,
                    label = stringResource(id = R.string.expense)
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Incomes,
                    onClick = { navController.navigate(Routes.Incomes) },
                    icon = Icons.Default.AccountBalanceWallet,
                    label = stringResource(id = R.string.income)
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Insights,
                    onClick = { navController.navigate(Routes.Insights) },
                    icon = Icons.Default.Analytics,
                    label = "Insights"
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
}

