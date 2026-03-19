package com.example.spendwiseai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.presentation.SpendWiseViewModelFactory
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel
import com.example.spendwiseai.presentation.transactions.TransactionsListViewModelFactory
import com.example.spendwiseai.ui.screens.AddExpenseScreen
import com.example.spendwiseai.ui.screens.HomeScreen
import com.example.spendwiseai.ui.screens.InsightsScreen
import com.example.spendwiseai.ui.screens.TransactionsListScreen

@Composable
fun SpendWiseNavHost(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val addExpenseViewModelFactory = remember(appContainer) {
        SpendWiseViewModelFactory(appContainer.getAddExpenseUseCase())
    }

    val transactionRepository = appContainer.getTransactionRepository()

    NavHost(
        navController = navController,
        startDestination = SpendWiseRoutes.Home.route
    ) {
        composable(SpendWiseRoutes.Home.route) {
            HomeScreen(
                onScanReceiptClicked = { navController.navigate(SpendWiseRoutes.Scan.route) },
                onAddExpenseClicked = { navController.navigate(SpendWiseRoutes.Add.route) }
            )
        }
        composable(SpendWiseRoutes.Add.route) {
            val vm: AddExpenseViewModel = viewModel(factory = addExpenseViewModelFactory)
            AddExpenseScreen(
                viewModel = vm,
                onSaved = { navController.navigate(SpendWiseRoutes.Home.route) }
            )
        }
        composable(SpendWiseRoutes.Expenses.route) {
            val factory = remember {
                TransactionsListViewModelFactory(
                    transactionType = com.example.spendwiseai.domain.model.TransactionType.EXPENSE,
                    transactionRepository = transactionRepository
                )
            }
            val vm: com.example.spendwiseai.presentation.transactions.TransactionsListViewModel = viewModel(factory = factory)
            TransactionsListScreen(
                transactionType = com.example.spendwiseai.domain.model.TransactionType.EXPENSE,
                viewModel = vm,
                title = "Expenses"
            )
        }
        composable(SpendWiseRoutes.Incomes.route) {
            val factory = remember {
                TransactionsListViewModelFactory(
                    transactionType = com.example.spendwiseai.domain.model.TransactionType.INCOME,
                    transactionRepository = transactionRepository
                )
            }
            val vm: com.example.spendwiseai.presentation.transactions.TransactionsListViewModel = viewModel(factory = factory)
            TransactionsListScreen(
                transactionType = com.example.spendwiseai.domain.model.TransactionType.INCOME,
                viewModel = vm,
                title = "Incomes"
            )
        }
        composable(SpendWiseRoutes.Insights.route) {
            InsightsScreen(
                onBackToHome = { navController.navigate(SpendWiseRoutes.Home.route) }
            )
        }

        composable(SpendWiseRoutes.Scan.route) {
            // TODO (Phase 2): implement the real camera + Gemini Vision receipt parsing flow.
            // For now, show the same Add-by-text screen to keep the navigation graph valid.
            val vm: AddExpenseViewModel = viewModel(factory = addExpenseViewModelFactory)
            AddExpenseScreen(
                viewModel = vm,
                onSaved = {
                    navController.navigate(
                        if (vm.uiState.value.preview?.type == com.example.spendwiseai.domain.model.TransactionType.INCOME) {
                            SpendWiseRoutes.Incomes.route
                        } else {
                            SpendWiseRoutes.Expenses.route
                        }
                    )
                }
            )
        }
    }
}

private object SpendWiseRoutes {
    val Home = Route("home")
    val Add = Route("add")
    val Scan = Route("scan")
    val Expenses = Route("expenses")
    val Incomes = Route("incomes")
    val Insights = Route("insights")

    data class Route(val route: String)
}

