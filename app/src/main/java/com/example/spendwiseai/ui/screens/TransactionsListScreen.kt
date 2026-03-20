package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.data.db.dao.TransactionWithCategory
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.transactions.TransactionsListViewModel
import com.example.spendwiseai.R
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import java.text.SimpleDateFormat
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
    val accent = if (transactionType == TransactionType.EXPENSE) SoftCoralRed else NeonGreen

    // Local UI state for edit/delete dialogs
    var deleteCandidate by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var editDraft by remember { mutableStateOf<TransactionEditDraft?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }

    // Collecting uiState via `value` would require collectAsState; uiState is a StateFlow in VM.
    // To keep this file lightweight and avoid adding more plumbing, we read it via Compose runtime.
    val stateValue by uiState.collectAsState()

    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete transaction?") },
            text = { Text("This action cannot be undone.") },
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
                TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") }
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
            title = { Text("Edit transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draft.amount,
                        onValueChange = { editDraft = draft.copy(amount = it); editError = null },
                        label = { Text("Amount") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = draft.currency,
                        onValueChange = { editDraft = draft.copy(currency = it); editError = null },
                        label = { Text("Currency (e.g., TL)") }
                    )
                    OutlinedTextField(
                        value = draft.categoryName,
                        onValueChange = { editDraft = draft.copy(categoryName = it); editError = null },
                        label = { Text("Category") }
                    )
                    OutlinedTextField(
                        value = draft.description,
                        onValueChange = { editDraft = draft.copy(description = it); editError = null },
                        label = { Text("Description") },
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
                            editError = "Please provide valid amount/currency/category."
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
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editError = null; editDraft = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        }
        items(stateValue.transactions.size) { index ->
            val tx = stateValue.transactions[index]

            val dismissState = rememberDismissState(confirmStateChange = { dismissValue ->
                if (dismissValue == androidx.compose.material.DismissValue.DismissedToStart ||
                    dismissValue == androidx.compose.material.DismissValue.DismissedToEnd
                ) {
                    deleteCandidate = tx
                    false
                } else {
                    true
                }
            })

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(
                    androidx.compose.material.DismissDirection.EndToStart
                ),
                background = {
                    val bgColor = accent.copy(alpha = 0.25f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor)
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete),
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
                                Text(
                                    text = tx.categoryName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatDate(tx.dateMillis),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatAmount(tx.amount, tx.currency),
                                    color = accent,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = {
                                    editError = null
                                    editDraft = TransactionEditDraft(
                                        id = tx.id,
                                        amount = tx.amount.toString(),
                                        currency = tx.currency,
                                        categoryName = tx.categoryName,
                                        description = tx.description,
                                        dateMillis = tx.dateMillis
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            )
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

