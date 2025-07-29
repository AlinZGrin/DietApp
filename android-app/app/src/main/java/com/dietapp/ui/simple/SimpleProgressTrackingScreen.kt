package com.dietapp.ui.simple

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dietapp.ui.viewmodels.WeightTrackingViewModel
import com.dietapp.utils.WeightUnit
import com.dietapp.utils.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleProgressTrackingScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeightTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // 🚨 TEMPORARY: Emergency database cleanup button
                    IconButton(onClick = { viewModel.nukeDatabaseEmergency() }) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Emergency DB Cleanup",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { viewModel.toggleUnits() }) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Toggle Units"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Weight Entry")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Weight Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(
                                label = "Current",
                                value = uiState.currentWeight?.let { viewModel.formatWeight(it) } ?: "0.0 ${if (uiState.useImperialUnits) "lbs" else "kg"}",
                                icon = Icons.Default.MonitorWeight
                            )
                            StatItem(
                                label = "Goal",
                                value = uiState.goalWeight?.let { viewModel.formatWeight(it) } ?: "0.0 ${if (uiState.useImperialUnits) "lbs" else "kg"}",
                                icon = Icons.Default.Flag
                            )
                            StatItem(
                                label = "Change",
                                value = uiState.weightChange?.let {
                                    val formatted = if (uiState.useImperialUnits) {
                                        "${String.format("%.1f", UnitConverter.kgToPounds(it))} lbs"
                                    } else {
                                        "${String.format("%.1f", it)} kg"
                                    }
                                    formatted
                                } ?: "0.0 ${if (uiState.useImperialUnits) "lbs" else "kg"}",
                                icon = Icons.Default.TrendingUp
                            )
                        }
                    }
                }
            }

            // Recent Entries
            item {
                Text(
                    text = "Recent Entries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.recentEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.MonitorWeight,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No weight entries yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Start tracking your progress by adding your first weight entry",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentEntries) { entry ->
                    WeightEntryCard(
                        weight = entry.weight,
                        date = entry.date,
                        notes = entry.notes,
                        useImperialUnits = uiState.useImperialUnits
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { weight, notes, unit ->
                viewModel.addWeightEntry(weight, notes, unit)
                showAddDialog = false
            },
            useImperialUnits = uiState.useImperialUnits
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeightEntryCard(
    weight: Double,
    date: Date,
    notes: String?,
    useImperialUnits: Boolean
) {
    val displayWeight = if (useImperialUnits) {
        "${String.format("%.1f", UnitConverter.kgToPounds(weight))} lbs"
    } else {
        "${String.format("%.1f", weight)} kg"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MonitorWeight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayWeight,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!notes.isNullOrBlank()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, WeightUnit) -> Unit,
    useImperialUnits: Boolean
) {
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(if (useImperialUnits) WeightUnit.LBS else WeightUnit.KG) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight Entry") },
        text = {
            Column {
                // Weight input with unit selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight") },
                        modifier = Modifier.weight(1f)
                    )

                    // Unit selector buttons
                    Row {
                        FilterChip(
                            onClick = { selectedUnit = WeightUnit.KG },
                            label = { Text("kg") },
                            selected = selectedUnit == WeightUnit.KG
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            onClick = { selectedUnit = WeightUnit.LBS },
                            label = { Text("lbs") },
                            selected = selectedUnit == WeightUnit.LBS
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weight.toDoubleOrNull()?.let { weightValue ->
                        onConfirm(weightValue, notes, selectedUnit)
                    }
                },
                enabled = weight.toDoubleOrNull() != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
