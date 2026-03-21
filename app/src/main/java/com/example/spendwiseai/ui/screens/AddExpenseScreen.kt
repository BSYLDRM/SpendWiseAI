package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed

@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val selectedCurrency = LocaleManager.getCurrency(context)
    val listState = rememberLazyListState()
    val accent = if (state.selectedType == TransactionType.INCOME) NeonGreen else SoftCoralRed

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(
                    text = "Islem Ekle",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { viewModel.onTypeSelected(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gider", color = if (state.selectedType == TransactionType.EXPENSE) SoftCoralRed else MaterialTheme.colorScheme.onSurface)
                }
                OutlinedButton(
                    onClick = { viewModel.onTypeSelected(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gelir", color = if (state.selectedType == TransactionType.INCOME) NeonGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Bugun Migros'a 250 TL harcadim / Today I spent 250 TL at Migros")
                },
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting && state.inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(18.dp).height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Isleniyor...")
                } else {
                    Text(if (state.selectedType == TransactionType.INCOME) "Geliri Islet" else "Gideri Islet")
                }
            }
        }

        state.errorMessage?.let { error ->
            item { Text(text = error, color = MaterialTheme.colorScheme.error) }
        }

        state.preview?.let { parsed ->
            item { PreviewCard(parsed = parsed, accent = accent, displayCurrency = selectedCurrency) }
        }

        if (state.preview != null && state.lastTransactionId != null) {
            item {
                TextButton(
                    onClick = onSaved,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kaydet ve Devam Et")
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(
    parsed: ParsedTransaction,
    accent: androidx.compose.ui.graphics.Color,
    displayCurrency: String
) {
    val tint = accent
    val typeLabel = if (parsed.type == TransactionType.INCOME) "Gelir" else "Gider"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cozumleme Onizlemesi", style = MaterialTheme.typography.titleMedium, color = tint)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Category, contentDescription = null, tint = tint)
                Spacer(Modifier.width(8.dp))
                Text(parsed.category)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = tint)
                Spacer(Modifier.width(8.dp))
                Text("${parsed.amount} $displayCurrency")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (parsed.type == TransactionType.INCOME) Icons.Default.NorthEast else Icons.Default.SouthEast,
                    contentDescription = null,
                    tint = tint
                )
                Spacer(Modifier.width(8.dp))
                Text(typeLabel)
            }
        }
    }
}

