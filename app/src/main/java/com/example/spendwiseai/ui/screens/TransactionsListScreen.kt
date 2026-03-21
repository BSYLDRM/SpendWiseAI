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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.data.db.dao.TransactionWithCategory
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.transactions.TransactionsListViewModel
import com.example.spendwiseai.R
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionsListScreen(
    transactionType: TransactionType,
    viewModel: TransactionsListViewModel,
    title: String,
    onNavigateBackHome: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedCurrency = LocaleManager.getCurrency(context)
    val accent = if (transactionType == TransactionType.EXPENSE) SoftCoralRed else NeonGreen

    // Local UI state for edit/delete dialogs
    var deleteCandidate by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var editDraft by remember { mutableStateOf<TransactionEditDraft?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }

    // Collecting uiState via `value` would require collectAsState; uiState is a StateFlow in VM.
    // To keep this file lightweight and avoid adding more plumbing, we read it via Compose runtime.
    val stateValue by uiState.collectAsState()
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val grouped = stateValue.transactions.groupBy { tx ->
        Instant.ofEpochMilli(tx.dateMillis).atZone(zone).toLocalDate()
    }.toSortedMap(compareByDescending { it })

    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Islem silinsin mi?") },
            text = { Text("Bu islem geri alinamaz.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deleteCandidate!!.id
                        deleteCandidate = null
                        viewModel.deleteTransaction(id)
                    }
                ) { Text(stringResource(id = R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Iptal") }
            }
        )
    }

    if (editDraft != null) {
        val draft = editDraft!!
        AlertDialog(
            onDismissRequest = {
                editError = null
                editDraft = null
            },
            title = { Text("Islemi duzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draft.amount,
                        onValueChange = { editDraft = draft.copy(amount = it); editError = null },
                        label = { Text("Tutar") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = draft.currency,
                        onValueChange = { editDraft = draft.copy(currency = it); editError = null },
                        label = { Text("Para birimi (orn. TL)") }
                    )
                    OutlinedTextField(
                        value = draft.categoryName,
                        onValueChange = { editDraft = draft.copy(categoryName = it); editError = null },
                        label = { Text("Kategori") }
                    )
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { editDraft = draft.copy(description = it); editError = null },
                        label = { Text("Aciklama") },
                        minLines = 2,
                        maxLines = 4
                    )
                    editError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = draft.amount.replace(',', '.').toDoubleOrNull()
                        if (amount == null || draft.currency.isBlank() || draft.categoryName.isBlank()) {
                            editError = "Lutfen gecerli tutar/para birimi/kategori girin."
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
                        editError = null
                        editDraft = null
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { editError = null; editDraft = null }) { Text("Iptal") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        }
        grouped.forEach { (date, txs) ->
            if (date == today) {
                item {
                    Text("Bugun", style = MaterialTheme.typography.titleMedium)
                }
                items(txs, key = { it.id }) { tx ->
                    TransactionDetailCard(
                        tx = tx,
                        currency = selectedCurrency,
                        accent = accent,
                        onEdit = {
                            editError = null
                            editDraft = TransactionEditDraft(
                                id = tx.id,
                                amount = tx.amount.toString(),
                                currency = tx.currency,
                                categoryName = tx.categoryName,
                                description = tx.description,
                                dateMillis = tx.dateMillis
                            )
                        },
                        onDelete = { deleteCandidate = tx }
                    )
                }
            } else {
                item {
                    GroupedDayCard(
                        date = date,
                        transactions = txs,
                        currency = selectedCurrency,
                        accent = accent,
                        isYesterday = date == today.minusDays(1),
                        onEdit = { tx ->
                            editError = null
                            editDraft = TransactionEditDraft(
                                id = tx.id,
                                amount = tx.amount.toString(),
                                currency = tx.currency,
                                categoryName = tx.categoryName,
                                description = tx.description,
                                dateMillis = tx.dateMillis
                            )
                        },
                        onDelete = { tx -> deleteCandidate = tx }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TransactionDetailCard(
    tx: TransactionWithCategory,
    currency: String,
    accent: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberDismissState(confirmStateChange = { dismissValue ->
        if (dismissValue == androidx.compose.material.DismissValue.DismissedToStart ||
            dismissValue == androidx.compose.material.DismissValue.DismissedToEnd
        ) {
            onDelete()
            false
        } else {
            true
        }
    })
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(accent.copy(alpha = 0.25f))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissContent = {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = tx.categoryName, style = MaterialTheme.typography.titleMedium)
                        Text(text = tx.description, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = formatDate(tx.dateMillis), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatAmount(tx.amount, currency),
                            color = accent,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Duzenle")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun GroupedDayCard(
    date: LocalDate,
    transactions: List<TransactionWithCategory>,
    currency: String,
    accent: Color,
    isYesterday: Boolean,
    onEdit: (TransactionWithCategory) -> Unit,
    onDelete: (TransactionWithCategory) -> Unit
) {
    var expanded by remember(date) { mutableStateOf(false) }
    val total = transactions.sumOf { it.amount }
    val dateLabel = if (isYesterday) "Dun" else date.format(
        DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr-TR"))
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$dateLabel - ${transactions.size} islem - ${formatAmount(total, currency)}",
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
            if (expanded) {
                transactions.forEach { tx ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.categoryName, style = MaterialTheme.typography.bodyMedium)
                            Text(tx.description, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(formatAmount(tx.amount, currency), color = accent)
                        IconButton(onClick = { onEdit(tx) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Duzenle")
                        }
                        IconButton(onClick = { onDelete(tx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double, currency: String): String {
    return "$currency ${String.format(Locale.US, "%.2f", amount)}"
}

private fun formatDate(dateMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(dateMillis))
}

