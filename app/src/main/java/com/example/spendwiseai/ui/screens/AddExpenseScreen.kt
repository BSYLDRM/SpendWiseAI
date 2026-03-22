package com.example.spendwiseai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.R
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel
import com.example.spendwiseai.ui.components.categoryLocalizedName
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedCurrency = LocaleManager.getCurrency(context)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val preview = state.preview
    val accent = if (selectedType == TransactionType.INCOME) NeonGreen else SoftCoralRed
    val screenTitle = if (selectedType == TransactionType.INCOME)
        stringResource(R.string.add_income_title)
    else
        stringResource(R.string.add_expense_title)

    val placeholderText = if (selectedType == TransactionType.INCOME)
        stringResource(R.string.placeholder_income)
    else
        stringResource(R.string.placeholder_expense)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Başlık + geri
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(text = screenTitle, style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Gider / Gelir toggle
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (selectedType == TransactionType.EXPENSE) {
                    Button(
                        onClick = { viewModel.onTypeChanged(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftCoralRed)
                    ) { Text("💸 ${stringResource(R.string.expense)}") }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.onTypeChanged(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("💸 ${stringResource(R.string.expense)}") }
                }

                if (selectedType == TransactionType.INCOME) {
                    Button(
                        onClick = { viewModel.onTypeChanged(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) { Text("💰 ${stringResource(R.string.income)}") }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.onTypeChanged(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("💰 ${stringResource(R.string.income)}") }
                }
            }
        }

        // Metin girişi
        item {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholderText) },
                minLines = 5,
                maxLines = 8
            )
        }

        // Analiz Et butonu
        item {
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting && state.inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.processing))
                } else {
                    Text(stringResource(R.string.analyze_with_ai))
                }
            }
        }

        // Hata
        state.errorMessage?.let { error ->
            item { Text(text = error, color = MaterialTheme.colorScheme.error) }
        }

        // Preview kartı
        preview?.let { parsed ->
            item {
                AnimatedVisibility(visible = true, enter = fadeIn(tween(280)), exit = fadeOut(tween(200))) {
                    PreviewCard(parsed = parsed, accent = accent, displayCurrency = selectedCurrency)
                }
            }
        }

        // Kaydet butonu
        if (preview != null && state.lastTransactionId == null) {
            item {
                Button(
                    onClick = { viewModel.confirmSave() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) { Text("✅ ${stringResource(R.string.save)}") }
            }
        }

        // Başarı
        if (state.lastTransactionId != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen)
                    Text(stringResource(R.string.successfully_saved), color = NeonGreen)
                }
            }
            item {
                Button(
                    onClick = { coroutineScope.launch { delay(300); onSaved() } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.go_home)) }
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
    val isIncome = parsed.type == TransactionType.INCOME
    val typeLabel = if (isIncome) "💰 ${stringResource(R.string.income)}" else "💸 ${stringResource(R.string.expense)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.ai_result_title), style = MaterialTheme.typography.titleMedium, color = accent)
            Text(typeLabel, color = accent, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(categoryEmoji(parsed.category))
                Spacer(Modifier.width(8.dp))
                Text(categoryLocalizedName(parsed.category))
            }
            Text(
                "${parsed.amount} $displayCurrency",
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Text(
                stringResource(R.string.confirm_question),
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
}

private fun categoryEmoji(category: String): String = when (category) {
    "Groceries"         -> "🛒"
    "Food & Drink"      -> "🍕"
    "Transportation"    -> "🚗"
    "Technology"        -> "💻"
    "Entertainment"     -> "🎬"
    "Shopping"          -> "🛍️"
    "Health"            -> "💊"
    "Bills & Utilities" -> "📱"
    "Education"         -> "📚"
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