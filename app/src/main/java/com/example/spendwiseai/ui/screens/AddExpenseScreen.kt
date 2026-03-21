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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel
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
    val state = viewModel.uiState.collectAsState().value
    val selectedType = viewModel.selectedType.collectAsState().value
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectedCurrency = LocaleManager.getCurrency(context)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showSuccess by remember { mutableStateOf(false) }
    val preview = state.preview
    val accent = if (selectedType == TransactionType.INCOME) NeonGreen else SoftCoralRed

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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(
                    text = "Harcama Ekle",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedType == TransactionType.EXPENSE) {
                    Button(
                        onClick = { viewModel.onTypeChanged(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SoftCoralRed)
                    ) { Text("💸 Gider") }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.onTypeChanged(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("💸 Gider") }
                }
                if (selectedType == TransactionType.INCOME) {
                    Button(
                        onClick = { viewModel.onTypeChanged(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) { Text("💰 Gelir") }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.onTypeChanged(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("💰 Gelir") }
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Ne harcadin veya ne kazandin? Ornek:\n" +
                            "• 250 TL market alisverisi\n" +
                            "• Kahve ve tavuk aldim 85 TL\n" +
                            "• Maas 15000 TL geldi\n" +
                            "• Bilgisayar icin kulaklik 450 TL"
                    )
                },
                minLines = 7,
                maxLines = 10
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
                    Text("AI ile Analiz Et")
                }
            }
        }

        state.errorMessage?.let { error ->
            item { Text(text = error, color = MaterialTheme.colorScheme.error) }
        }

        preview?.let { parsed ->
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(280)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    PreviewCard(
                        parsed = parsed,
                        accent = accent,
                        displayCurrency = selectedCurrency
                    )
                }
            }
        }

        if (preview != null && state.lastTransactionId != null) {
            item {
                Button(
                    onClick = {
                        showSuccess = true
                        coroutineScope.launch {
                            delay(900)
                            onSaved()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Kaydet") }
            }
            item {
                AnimatedVisibility(visible = showSuccess) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen)
                        Text("Basariyla kaydedildi")
                    }
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
    val isIncome = parsed.type == TransactionType.INCOME
    val typeLabel = if (isIncome) "\uD83D\uDCB0 Gelir" else "\uD83D\uDCB8 Gider"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cozumleme Onizlemesi", style = MaterialTheme.typography.titleMedium, color = tint)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isIncome) "\uD83D\uDCB0" else "\uD83D\uDCB8", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(8.dp))
                Text(typeLabel, color = tint, style = MaterialTheme.typography.titleMedium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(categoryEmoji(parsed.category))
                Spacer(Modifier.width(8.dp))
                Text(parsed.category)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${parsed.amount} $displayCurrency",
                    style = MaterialTheme.typography.headlineMedium,
                    color = tint
                )
            }
        }
    }
}

private fun categoryEmoji(category: String): String {
    return when (category) {
        "Groceries" -> "\uD83D\uDED2"
        "Food & Drink" -> "\uD83C\uDF55"
        "Transportation" -> "\uD83D\uDE97"
        "Technology" -> "\uD83D\uDCBB"
        "Entertainment" -> "\uD83C\uDFAC"
        "Shopping" -> "\uD83D\uDEDD"
        "Health" -> "\uD83D\uDC8A"
        "Bills & Utilities" -> "\uD83D\uDCF1"
        "Education" -> "\uD83D\uDCDA"
        "Salary" -> "\uD83D\uDCBC"
        else -> "\uD83D\uDCE6"
    }
}

