package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendwiseai.R
import com.example.spendwiseai.data.db.dao.TransactionWithCategory
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.transactions.TransactionsListViewModel
import com.example.spendwiseai.ui.components.categoryLocalizedName
import com.example.spendwiseai.ui.components.expenseCategoryColor
import com.example.spendwiseai.ui.components.incomeCategoryColor
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TransactionEditDraft(
    val id: Long,
    val amount: String,
    val currency: String,
    val categoryName: String,
    val description: String,
    val dateMillis: Long
)

private fun groupTransactionsByDay(
    transactions: List<TransactionWithCategory>,
    todayLabel: String,
    yesterdayLabel: String
): Map<String, List<TransactionWithCategory>> {
    val cal = Calendar.getInstance()
    val todayStart = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return transactions.groupBy { tx ->
        when {
            tx.dateMillis >= todayStart     -> todayLabel
            tx.dateMillis >= yesterdayStart -> yesterdayLabel
            else                            -> sdf.format(Date(tx.dateMillis))
        }
    }
}

private fun categoryColor(categoryName: String, isIncome: Boolean): Color =
    if (isIncome) incomeCategoryColor(categoryName) else expenseCategoryColor(categoryName)

private fun categoryEmoji(category: String): String = when (category) {
    "Groceries"         -> "🛒"
    "Food & Drink"      -> "🍕"
    "Transportation"    -> "🚗"
    "Entertainment"     -> "🎬"
    "Shopping"          -> "🛍️"
    "Bills & Utilities" -> "📱"
    "Health"            -> "💊"
    "Education"         -> "📚"
    "Technology"        -> "💻"
    "Rent"              -> "🏠"
    "Salary"            -> "💼"
    "Freelance"         -> "🖥️"
    "Refund"            -> "↩️"
    "Meal Allowance"    -> "🍱"
    "Investment"        -> "📈"
    "Gift"              -> "🎁"
    "Other Income"      -> "💵"
    else                -> "📦"
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionsListScreen(
    transactionType: TransactionType,
    viewModel: TransactionsListViewModel,
    title: String,
    onNavigateBackHome: () -> Unit = {}
) {
    val stateValue by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val isIncome = transactionType == TransactionType.INCOME
    val accent = if (isIncome) NeonGreen else SoftCoralRed
    var deleteCandidate by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var editDraft by remember { mutableStateOf<TransactionEditDraft?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }
    var expandedDays by remember { mutableStateOf(setOf<String>()) }

    val todayLabel      = stringResource(R.string.today)
    val yesterdayLabel  = stringResource(R.string.yesterday)
    // onClick lambda içinde stringResource çağrılamaz — önceden alıyoruz
    val invalidInputMsg = stringResource(R.string.invalid_input)

    val grouped = groupTransactionsByDay(stateValue.transactions, todayLabel, yesterdayLabel)
    val orderedKeys = grouped.keys.sortedWith(compareBy {
        when (it) { todayLabel -> 0; yesterdayLabel -> 1; else -> 2 }
    })
    val totalAmount = stateValue.transactions.sumOf { it.amount }

    // Delete dialog
    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text  = { Text(stringResource(R.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(deleteCandidate!!.id)
                    deleteCandidate = null
                }) { Text(stringResource(R.string.delete), color = SoftCoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Edit dialog
    if (editDraft != null) {
        val draft = editDraft!!
        AlertDialog(
            onDismissRequest = { editDraft = null; editError = null },
            title = { Text(stringResource(R.string.edit_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.amount,
                        onValueChange = { editDraft = draft.copy(amount = it); editError = null },
                        label = { Text(stringResource(R.string.amount_label)) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = draft.currency,
                        onValueChange = { editDraft = draft.copy(currency = it) },
                        label = { Text(stringResource(R.string.currency_label)) }
                    )
                    OutlinedTextField(
                        value = draft.categoryName,
                        onValueChange = { editDraft = draft.copy(categoryName = it) },
                        label = { Text(stringResource(R.string.category_label)) }
                    )
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { editDraft = draft.copy(description = it) },
                        label = { Text(stringResource(R.string.description_label)) },
                        minLines = 2, maxLines = 4
                    )
                    editError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = draft.amount.replace(',', '.').toDoubleOrNull()
                    if (amount == null || draft.currency.isBlank() || draft.categoryName.isBlank()) {
                        // stringResource burada çağrılamaz — önceden alınan değeri kullan
                        editError = invalidInputMsg
                        return@TextButton
                    }
                    viewModel.updateTransaction(
                        id = draft.id,
                        amount = amount,
                        currency = draft.currency.trim(),
                        categoryName = draft.categoryName.trim(),
                        description = draft.description,
                        dateMillis = draft.dateMillis
                    )
                    editDraft = null
                    editError = null
                }) { Text(stringResource(R.string.save), color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { editDraft = null; editError = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        if (stateValue.transactions.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isIncome) stringResource(R.string.total_income_label)
                                else stringResource(R.string.total_expense_label),
                                color = Color.Gray, fontSize = 13.sp
                            )
                            Text(
                                "${"%.2f".format(totalAmount)} ${stateValue.transactions.firstOrNull()?.currency ?: "TL"}",
                                color = accent, fontWeight = FontWeight.Bold, fontSize = 24.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.transaction_count, stateValue.transactions.size), color = Color.Gray, fontSize = 13.sp)
                            Text(stringResource(R.string.day_count, grouped.keys.size), color = accent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        if (stateValue.transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(if (isIncome) "💰" else "💸", fontSize = 48.sp)
                    Text(
                        if (isIncome) stringResource(R.string.no_income_yet)
                        else stringResource(R.string.no_expense_yet),
                        style = MaterialTheme.typography.titleMedium, color = Color.Gray
                    )
                    Text(stringResource(R.string.empty_hint), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        orderedKeys.forEach { dayKey ->
            val txList = grouped[dayKey] ?: return@forEach
            val isToday   = dayKey == todayLabel
            val isExpanded = expandedDays.contains(dayKey)

            if (isToday) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                        Spacer(Modifier.width(8.dp))
                        Text(dayKey, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.transaction_count, txList.size), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                txList.forEach { tx ->
                    item(key = tx.id) {
                        val dismissState = rememberDismissState(confirmStateChange = { value ->
                            if (value == androidx.compose.material.DismissValue.DismissedToStart) {
                                deleteCandidate = tx; false
                            } else true
                        })
                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(androidx.compose.material.DismissDirection.EndToStart),
                            background = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SoftCoralRed.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Delete, null, tint = SoftCoralRed, modifier = Modifier.size(24.dp))
                                        Text(stringResource(R.string.swipe_to_delete), color = SoftCoralRed, fontSize = 11.sp)
                                    }
                                }
                            },
                            dismissContent = {
                                TransactionCard(
                                    tx = tx, accent = accent, isIncome = isIncome,
                                    onEditClick = {
                                        editDraft = TransactionEditDraft(
                                            id = tx.id, amount = tx.amount.toString(),
                                            currency = tx.currency, categoryName = tx.categoryName,
                                            description = tx.description, dateMillis = tx.dateMillis
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }

            if (!isToday) {
                item(key = dayKey) {
                    val total    = txList.sumOf { it.amount }
                    val dotColor = if (dayKey == yesterdayLabel) accent else Color.Gray

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            expandedDays = if (isExpanded) expandedDays - dayKey else expandedDays + dayKey
                        },
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            dayKey,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (dayKey == yesterdayLabel) accent else Color.Unspecified
                                        )
                                        Text(stringResource(R.string.transaction_count, txList.size), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${"%.2f".format(total)} ${txList.firstOrNull()?.currency ?: ""}",
                                        color = accent, fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null, tint = Color.Gray, modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (isExpanded) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                                Spacer(Modifier.height(8.dp))
                                txList.forEach { tx ->
                                    val catColor = categoryColor(tx.categoryName, isIncome)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier.size(36.dp).background(catColor.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(categoryEmoji(tx.categoryName), fontSize = 16.sp)
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(categoryLocalizedName(tx.categoryName), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                Text(tx.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                                            }
                                        }
                                        Text(
                                            "${"%.2f".format(tx.amount)} ${tx.currency}",
                                            color = catColor, fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TransactionCard(
    tx: TransactionWithCategory,
    accent: Color,
    isIncome: Boolean,
    onEditClick: () -> Unit
) {
    val catColor = categoryColor(tx.categoryName, isIncome)

    Card(elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).background(catColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Text(categoryEmoji(tx.categoryName), fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(categoryLocalizedName(tx.categoryName), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(tx.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(tx.dateMillis)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.2f".format(tx.amount)} ${tx.currency}", color = catColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}