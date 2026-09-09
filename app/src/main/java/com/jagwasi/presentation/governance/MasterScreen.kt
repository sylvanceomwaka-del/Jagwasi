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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.entity.Staff
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterScreen(
    viewModel: MasterViewModel,
    onNavigateToSupervisor: () -> Unit,
    onNavigateToStaffRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showRecoverySealDialog by remember { mutableStateOf(false) }
    var generatedSeal by remember { mutableStateOf("") }
    var verifySealInput by remember { mutableStateOf("") }
    var verifyResult by remember { mutableStateOf<Boolean?>(null) }
    var showConfigEditDialog by remember { mutableStateOf(false) }
    var configKey by remember { mutableStateOf("") }
    var configValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Initial load triggers via ViewModel init
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MasterUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MasterUiState.Error -> {
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
                        Button(onClick = { viewModel.loadStaff() }) {
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
                        Button(
                            onClick = onNavigateToSupervisor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go to Supervisor Dashboard")
                        }

                        // ---- Staff Management ----
                        SectionTitle("Staff Management")
                        if (state is MasterUiState.StaffList) {
                            if (state.staff.isEmpty()) {
                                Text("No staff members registered.")
                            } else {
                                state.staff.forEach { staff ->
                                    StaffItem(
                                        staff = staff,
                                        onDeactivate = { viewModel.deactivateStaff(staff.id) }
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.loadStaff() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Load Staff List")
                            }
                        }

                        Button(
                            onClick = onNavigateToStaffRegistration,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Register New Staff")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---- Audit Log ----
                        SectionTitle("Audit Log")
                        Button(
                            onClick = { viewModel.loadAuditLog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Audit Log")
                        }
                        if (state is MasterUiState.AuditLog) {
                            if (state.entries.isEmpty()) {
                                Text("No audit entries found.")
                            } else {
                                state.entries.take(10).forEach { entry ->
                                    AuditEntryItem(entry)
                                }
                                if (state.entries.size > 10) {
                                    Text("... and ${state.entries.size - 10} more")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---- Recovery Seal ----
                        SectionTitle("Recovery Seal")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    generatedSeal = viewModel.generateRecoverySeal()
                                    showRecoverySealDialog = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Generate Seal")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = verifySealInput,
                                onValueChange = { verifySealInput = it },
                                label = { Text("Enter seal to verify") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    verifyResult = viewModel.verifyRecoverySeal(verifySealInput)
                                }
                            ) {
                                Text("Verify")
                            }
                        }
                        verifyResult?.let { result ->
                            Text(
                                text = if (result) "✅ Seal verified" else "❌ Invalid seal",
                                color = if (result) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---- Configuration ----
                        SectionTitle("Configuration")
                        Button(
                            onClick = { viewModel.loadConfig() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Configuration")
                        }
                        if (state is MasterUiState.Config) {
                            if (state.config.isEmpty()) {
                                Text("No configuration set.")
                            } else {
                                state.config.forEach { (key, value) ->
                                    ConfigItem(
                                        key = key,
                                        value = value,
                                        onEdit = {
                                            configKey = key
                                            configValue = value
                                            showConfigEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecoverySealDialog) {
        AlertDialog(
            onDismissRequest = { showRecoverySealDialog = false },
            title = { Text("Recovery Seal") },
            text = {
                Column {
                    Text("Write down these 12 words in order and keep them safe:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generatedSeal,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "This seal will not be shown again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecoverySealDialog = false }) {
                    Text("I have saved it")
                }
            }
        )
    }

    if (showConfigEditDialog) {
        AlertDialog(
            onDismissRequest = { showConfigEditDialog = false },
            title = { Text("Edit Configuration") },
            text = {
                Column {
                    Text("Key: $configKey")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = configValue,
                        onValueChange = { configValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateConfig(configKey, configValue)
                        showConfigEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigEditDialog = false }) {
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
private fun StaffItem(
    staff: Staff,
    onDeactivate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = staff.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Role: ${staff.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (staff.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (staff.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (staff.isActive) {
                Button(onClick = onDeactivate) {
                    Text("Deactivate")
                }
            }
        }
    }
}

@Composable
private fun AuditEntryItem(entry: AuditEntry) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = entry.actor, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = dateFormat.format(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = entry.action,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = entry.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfigItem(
    key: String,
    value: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = key, style = MaterialTheme.typography.bodyMedium)
                Text(text = value, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onEdit) {
                Text("Edit")
            }
        }
    }
}

