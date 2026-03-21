package com.example.spendwiseai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.res.stringResource
import com.example.spendwiseai.core.AppContainer
import com.example.spendwiseai.presentation.SpendWiseViewModelFactory
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel
import com.example.spendwiseai.presentation.budget.BudgetViewModelFactory
import com.example.spendwiseai.presentation.transactions.TransactionsListViewModelFactory
import com.example.spendwiseai.presentation.insights.InsightsViewModelFactory
import com.example.spendwiseai.ui.screens.AddExpenseScreen
import com.example.spendwiseai.ui.screens.BudgetScreen
import com.example.spendwiseai.ui.screens.HomeScreen
import com.example.spendwiseai.ui.screens.InsightsScreen
import com.example.spendwiseai.ui.screens.TransactionsListScreen
import com.example.spendwiseai.presentation.dashboard.DashboardViewModelFactory

@Composable
fun SpendWiseNavHost(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val transactionRepository = appContainer.provideTransactionRepository()
    val addExpenseViewModelFactory = remember(appContainer) {
        SpendWiseViewModelFactory(
            addExpenseUseCase = appContainer.getAddExpenseUseCase(),
            transactionRepository = transactionRepository
        )
    }

    NavHost(
        navController = navController,
        startDestination = SpendWiseRoutes.Home.route
    ) {
        composable(SpendWiseRoutes.Home.route) {
            val dashboardFactory = remember(appContainer) {
                DashboardViewModelFactory(transactionRepository)
            }
            val dashboardVm: com.example.spendwiseai.presentation.dashboard.DashboardViewModel = viewModel(factory = dashboardFactory)
            HomeScreen(
                onAddExpenseClicked = { navController.navigate(SpendWiseRoutes.Add.route) },
                dashboardViewModel = dashboardVm
            )
        }
        composable(SpendWiseRoutes.Add.route) {
            val vm: AddExpenseViewModel = viewModel(factory = addExpenseViewModelFactory)
            AddExpenseScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(SpendWiseRoutes.Home.route) {
                        popUpTo(SpendWiseRoutes.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
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
                title = stringResource(id = com.example.spendwiseai.R.string.expense)
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
                title = stringResource(id = com.example.spendwiseai.R.string.income)
            )
        }
        composable(SpendWiseRoutes.Insights.route) {
            val insightsFactory = remember(appContainer) {
                InsightsViewModelFactory(
                    insightsRepository = appContainer.provideInsightsRepository(),
                    transactionRepository = transactionRepository,
                    generator = appContainer.provideInsightsGenerator()
                )
            }
            val vm: com.example.spendwiseai.presentation.insights.InsightsViewModel =
                viewModel(factory = insightsFactory)
            InsightsScreen(
                viewModel = vm,
                onBackToHome = { navController.navigate(SpendWiseRoutes.Home.route) }
            )
        }
        composable(SpendWiseRoutes.Budget.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val factory = remember(appContainer) {
                BudgetViewModelFactory(
                    context = context.applicationContext,
                    transactionRepository = transactionRepository
                )
            }
            val vm: com.example.spendwiseai.presentation.budget.BudgetViewModel = viewModel(factory = factory)
            BudgetScreen(viewModel = vm)
        }

    }
}

private object SpendWiseRoutes {
    val Home = Route("home")
    val Add = Route("add")
    val Expenses = Route("expenses")
    val Incomes = Route("incomes")
    val Insights = Route("insights")
    val Budget = Route("budget")

    data class Route(val route: String)
}

