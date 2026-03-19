package com.example.spendwiseai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.ui.navigation.SpendWiseNavHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Income
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
            NavigationBar {
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
                    label = "Expenses"
                )
                BottomNavItem(
                    selected = currentRoute == Routes.Incomes,
                    onClick = { navController.navigate(Routes.Incomes) },
                    icon = Icons.Default.Income,
                    label = "Incomes"
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
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(iconVector = icon, contentDescription = label) },
        label = { androidx.compose.material3.Text(label) }
    )
}

private object Routes {
    const val Home = "home"
    const val Expenses = "expenses"
    const val Incomes = "incomes"
    const val Insights = "insights"
}

