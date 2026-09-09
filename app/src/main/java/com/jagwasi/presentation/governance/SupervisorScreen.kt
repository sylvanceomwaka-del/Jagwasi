package com.jagwasi.presentation.governance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SupervisorScreen(
    viewModel: SupervisorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog state for reject reason
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectActionId by remember { mutableStateOf("") }
    var rejectReason by remember { mutableStateOf("") }

    Scaffold(modifier = modifier) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SupervisorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is SupervisorUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Back button
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← Back")
                        }

                        // ---- Fraud Alerts ----
                        SectionTitle("Fraud Alerts")
                        Button(
                            onClick = { viewModel.loadFraudAlerts() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Fraud Alerts")
                        }
                        if (state is SupervisorUiState.FraudAlerts) {
                            if (state.alerts.isEmpty()) {
                                Text("No alerts.")
                            } else {
                                state.alerts.forEach { alert ->
                                    AlertItem(alert)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---- Pending Approvals ----
                        SectionTitle("Pending Approvals")
                        Button(
                            onClick = { viewModel.loadPendingApprovals() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Pending Approvals")
                        }
                        if (state is SupervisorUiState.PendingApprovals) {
                            if (state.approvals.isEmpty()) {
                                Text("No pending approvals.")
                            } else {
                                state.approvals.forEach { (description, id) ->
                                    ApprovalItem(
                                        description = description,
                                        actionId = id.toString(),
                                        onApprove = { viewModel.approveAction(it) },
                                        onReject = { actionId ->
                                            rejectActionId = actionId
                                            rejectReason = ""
                                            showRejectDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---- End-of-Shift Report ----
                        SectionTitle("End-of-Shift Report")
                        Button(
                            onClick = { viewModel.generateEndOfShiftReport() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Report")
                        }
                        if (state is SupervisorUiState.EndOfShiftReport) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.report,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.exportReport("PDF") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("PDF")
                                }
                                Button(
                                    onClick = { viewModel.exportReport("HTML") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("HTML")
                                }
                                Button(
                                    onClick = { viewModel.exportReport("CSV") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CSV")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Action") },
            text = {
                Column {
                    Text("Enter a reason for rejection:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            viewModel.rejectAction(rejectActionId, rejectReason)
                            showRejectDialog = false
                        }
                    }
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun AlertItem(alert: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "⚠️ $alert",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ApprovalItem(
    description: String,
    actionId: String,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onApprove(actionId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve")
                }
                Button(
                    onClick = { onReject(actionId) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            }
        }
    }
}

