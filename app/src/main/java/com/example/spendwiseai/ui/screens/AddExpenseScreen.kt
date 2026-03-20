package com.example.spendwiseai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel

@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    onSaved: () -> Unit
) {
    val state = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add a transaction (natural language)",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = state.inputText,
            onValueChange = viewModel::onInputChanged,
            modifier = Modifier.padding(top = 8.dp),
            label = { Text("Example: Today I spent 150 TL on a coffee and a sandwich") },
            minLines = 3,
            maxLines = 5
        )

        Button(
            onClick = viewModel::submit,
            enabled = !state.isSubmitting,
        ) {
            Text("Save transaction")
        }

        if (state.isSubmitting) {
            CircularProgressIndicator()
        }

        state.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        state.preview?.let { parsed ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Parsed preview", style = MaterialTheme.typography.titleMedium)
                    Text("Amount: ${parsed.amount}")
                    Text("Currency: ${parsed.currency}")
                    Text("Category: ${parsed.category}")
                    Text("Type: ${parsed.type}")
                }
            }
        }

        // When the transaction is saved, notify the caller (e.g., navigate back home).
        state.lastTransactionId?.let {
            TextButton(
                onClick = onSaved
            ) {
                Text("Continue")
            }
        }
    }
}

