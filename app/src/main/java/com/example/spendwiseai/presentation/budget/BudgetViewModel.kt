package com.example.spendwiseai.presentation.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.core.BudgetManager
import com.example.spendwiseai.data.db.dao.CategoryTotal
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth
import java.time.ZoneId

data class BudgetUiState(
    val monthLabel: String = "",
    val monthKey: String = "",
    val budgetAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val spentPercent: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList()
)

class BudgetViewModel(
    private val context: Context,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val monthOffset = MutableStateFlow(0)

    // Bütçe miktarını ayrı bir Flow olarak tutuyoruz
    // Böylece kaydet basınca bu Flow tetiklenecek ve UI güncellenecek
    private val budgetTrigger = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = monthOffset
        .flatMapLatest { offset ->
            val yearMonth = YearMonth.now().plusMonths(offset.toLong())
            val monthKey = "${yearMonth.year}_${yearMonth.monthValue.toString().padStart(2, '0')}"
            val startMillis = yearMonth.atDay(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = yearMonth.plusMonths(1).atDay(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                transactionRepository.observeAmountBetween(
                    TransactionType.EXPENSE, startMillis, endMillis
                ),
                transactionRepository.observeCategoryTotalsBetween(
                    TransactionType.EXPENSE, startMillis, endMillis
                ),
                budgetTrigger // bütçe kaydedilince bu değişir ve combine yeniden çalışır
            ) { spent, categories, _ ->
                val budget = BudgetManager.getBudget(context, monthKey)
                val percent = if (budget > 0.0) (spent / budget) * 100.0 else 0.0
                BudgetUiState(
                    monthLabel = monthLabelTr(yearMonth),
                    monthKey = monthKey,
                    budgetAmount = budget,
                    spentAmount = spent,
                    remainingAmount = (budget - spent).coerceAtLeast(0.0),
                    spentPercent = percent,
                    categoryTotals = categories
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BudgetUiState()
        )

    fun goPrevMonth() = monthOffset.update { it - 1 }
    fun goNextMonth() = monthOffset.update { it + 1 }

    fun saveBudget(amount: Double) {
        val current = uiState.value
        BudgetManager.saveBudget(context, current.monthKey, amount)
        // budgetTrigger'ı değiştirince combine yeniden çalışır ve UI güncellenir
        budgetTrigger.value = System.currentTimeMillis()
    }

    private fun monthLabelTr(yearMonth: YearMonth): String {
        val monthNames = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        return "${monthNames[yearMonth.monthValue - 1]} ${yearMonth.year}"
    }
}