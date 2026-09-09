package com.jagwasi.presentation.payment

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (receiptText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Local state for payment method and amount
    var selectedMethod by remember { mutableStateOf("CASH") }
    var amountText by remember { mutableStateOf("") }

    // Handle one‑time events
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is PaymentUiEvent.PaymentSuccess -> {
                    Toast.makeText(context, "Payment successful!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is PaymentUiEvent.ShowReceipt -> {
                    onNavigateToReceipt(event.receiptText)
                }
                is PaymentUiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                PaymentUiEvent.UnpaidExitDetected -> {
                    Toast.makeText(context, "WARNING: Unpaid exit detected!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order ID and amount
            Text(
                text = "Order ID: ${uiState.orderId ?: "N/A"}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Total Amount: $${uiState.payment?.amount?.toString() ?: "0.00"}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Payment method selection
            Text("Payment Method", style = MaterialTheme.typography.titleMedium)
            listOf("CASH", "M-PESA", "CARD").forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        colors = RadioButtonDefaults.colors()
                    )
                    Text(text = method, modifier = Modifier.padding(start = 8.dp))
                }
            }

            // Amount input
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount Paid") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Action buttons
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        Toast.makeText(context, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.processPayment(amount, selectedMethod)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Process Payment")
            }

            Button(
                onClick = { viewModel.generateReceipt() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Receipt")
            }

            Button(
                onClick = { viewModel.detectUnpaidExit() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Detect Unpaid Exit")
            }

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            // Error state with retry
            uiState.errorMessage?.let { error ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

