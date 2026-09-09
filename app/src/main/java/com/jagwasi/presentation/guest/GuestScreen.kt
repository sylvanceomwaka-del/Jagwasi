package com.jagwasi.presentation.guest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagwasi.data.local.entity.MealCard

@Composable
fun GuestScreen(
    viewModel: GuestViewModel,
    onOrderPlaced: (orderId: Int) -> Unit,
    onConsentRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()

    var customerName by remember { mutableStateOf("") }

    // Auto-load menu on first composition
    LaunchedEffect(Unit) {
        if (uiState is GuestUiState.Idle) {
            viewModel.loadMenu()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GuestUiState.OrderPlaced -> {
                onOrderPlaced(state.orderId)
            }
            is GuestUiState.ConsentRequired -> {
                onConsentRequired()
            }
            else -> { /* no‑op */ }
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
            when (val state = uiState) {
                is GuestUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is GuestUiState.MenuLoaded -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            MenuItemRow(
                                item = item,
                                quantity = selectedItems.find { it.first == item.id }?.second ?: 0,
                                onQuantityChange = { newQuantity ->
                                    viewModel.selectItem(item.id, newQuantity)
                                },
                                onRemove = { viewModel.removeItem(item.id) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.placeOrder(customerName, selectedItems)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customerName.isNotBlank() && selectedItems.isNotEmpty()
                    ) {
                        Text("Place Order (${selectedItems.size} items)")
                    }

                    if (selectedItems.isNotEmpty()) {
                        Text(
                            text = "Selected: ${selectedItems.joinToString { "${it.first}×${it.second}" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is GuestUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadMenu() }) {
                            Text("Retry")
                        }
                    }
                }

                is GuestUiState.OrderPlaced -> {
                    Text("Order placed successfully! (ID: ${state.orderId})")
                }

                is GuestUiState.ConsentRequired -> {
                    Text("Consent required for portrait capture.")
                }

                is GuestUiState.Idle -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: MealCard,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.items,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (quantity > 0) onQuantityChange(quantity - 1)
                    },
                    enabled = quantity > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = quantity.toString(),
                    modifier = Modifier.widthIn(min = 24.dp)
                )

                IconButton(
                    onClick = { onQuantityChange(quantity + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }

                if (quantity > 0) {
                    TextButton(onClick = onRemove) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}

