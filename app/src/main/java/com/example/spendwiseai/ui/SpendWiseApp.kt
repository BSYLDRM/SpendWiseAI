package com.example.spendwiseai.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spendwiseai.R
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.ui.navigation.SpendWiseNavHost
import com.example.spendwiseai.ui.theme.AppBackground
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpendWiseApp() {
    val context = LocalContext.current
    val appContainer = remember(context) { AppContainer(context) }
    val navController = rememberNavController()
    var showSettings by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(LocaleManager.getLanguageTag(context)) }
    var selectedCurrency by remember { mutableStateOf(LocaleManager.getCurrency(context)) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "login") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(selected = currentRoute == Routes.Home,     onClick = { navController.navigateToBottomTab(Routes.Home) },     icon = Icons.Default.Home,                        label = stringResource(R.string.nav_home))
                    BottomNavItem(selected = currentRoute == Routes.Expenses,  onClick = { navController.navigateToBottomTab(Routes.Expenses) },  icon = Icons.AutoMirrored.Filled.ReceiptLong,     label = stringResource(R.string.nav_expense))
                    BottomNavItem(selected = currentRoute == Routes.Incomes,   onClick = { navController.navigateToBottomTab(Routes.Incomes) },   icon = Icons.Default.AccountBalanceWallet,        label = stringResource(R.string.nav_income))
                    BottomNavItem(selected = currentRoute == Routes.Insights,  onClick = { navController.navigateToBottomTab(Routes.Insights) },  icon = Icons.Default.Analytics,                  label = stringResource(R.string.nav_ai_coach))
                    BottomNavItem(selected = currentRoute == Routes.Budget,    onClick = { navController.navigateToBottomTab(Routes.Budget) },    icon = Icons.Default.Flag,                       label = stringResource(R.string.nav_budget))
                    BottomNavItem(selected = false,                            onClick = { showSettings = true },                               icon = Icons.Default.Settings,                   label = stringResource(R.string.nav_settings))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SpendWiseNavHost(navController = navController, appContainer = appContainer)
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            containerColor = Color.Transparent,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            SettingsSheet(
                selectedLanguage = selectedLanguage,
                selectedCurrency = selectedCurrency,
                navController = navController,
                appContainer = appContainer,
                onLanguageSelected = { lang ->
                    selectedLanguage = lang
                    LocaleManager.setLanguageTag(context, lang)
                    (context as? Activity)?.recreate()
                },
                onCurrencySelected = { currency ->
                    selectedCurrency = currency
                    LocaleManager.setCurrency(context, currency)
                },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun SettingsSheet(
    selectedLanguage: String,
    selectedCurrency: String,
    navController: androidx.navigation.NavHostController,
    appContainer: AppContainer,
    onLanguageSelected: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val languages = listOf(
        "tr" to "Türkçe 🇹🇷",
        "en" to "English 🇬🇧"
    )
    val currencies = listOf("TL", "USD", "EUR", "GBP", "EUR")
        .distinct()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1218), AppBackground)
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(top = 12.dp, bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Handle çizgisi
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            // Başlık
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Dil Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.language_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(languages) { (code, label) ->
                        SelectableChip(
                            label = label,
                            selected = selectedLanguage == code,
                            accentColor = NeonGreen,
                            onClick = { onLanguageSelected(code) }
                        )
                    }
                }
            }

            // Para Birimi Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.currency_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(currencies) { currency ->
                        val symbol = when (currency) {
                            "TL"  -> "₺  TL"
                            "USD" -> "$  USD"
                            "EUR" -> "€  EUR"
                            "GBP" -> "£  GBP"
                            else  -> currency
                        }
                        SelectableChip(
                            label = symbol,
                            selected = selectedCurrency == currency,
                            accentColor = NeonGreen,
                            onClick = { onCurrencySelected(currency) }
                        )
                    }
                }
            }

            // Sign Out Button
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftCoralRed.copy(alpha = 0.1f))
                    .border(1.dp, SoftCoralRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable {
                        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                        GlobalScope.launch(Dispatchers.IO) {
                            appContainer.clearAllLocalData()
                            appContainer.authRepository.signOut()
                            withContext(Dispatchers.Main) {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                                onDismiss()
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Çıkış Yap",
                    color = SoftCoralRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            // Uygulama bilgisi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "v1.0.0  •  Powered by Gemini AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                Text("✨", fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (selected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if (selected) accentColor else Color.White.copy(alpha = 0.12f)
    val textColor = if (selected) accentColor else Color.White.copy(alpha = 0.7f)
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = fontWeight,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private object Routes {
    const val Home     = "home"
    const val Expenses = "expenses"
    const val Incomes  = "incomes"
    const val Insights = "insights"
    const val Budget   = "budget"
}

private fun androidx.navigation.NavHostController.navigateToBottomTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}