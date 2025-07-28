package com.dietapp.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dietapp.ui.viewmodels.BarcodeScannerViewModel
import com.dietapp.ui.viewmodels.BarcodeScannerUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Update permission status in ViewModel
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.updatePermissionStatus(cameraPermissionState.status.isGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isScanning) {
                        IconButton(
                            onClick = { viewModel.stopScanning() }
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop scanning")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !uiState.hasPermission -> {
                    // Permission request content
                    PermissionRequestContent(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                uiState.isScanning -> {
                    // Camera preview
                    CameraPreview(
                        onBarcodeScanned = { barcode ->
                            viewModel.onBarcodeScanned(barcode)
                        },
                        onError = { error ->
                            viewModel.updateError("Camera error: $error")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    // Scanner ready state
                    ScannerReadyContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onStartScanning = { viewModel.startScanning() },
                        onClearBarcode = { viewModel.clearScannedBarcode() },
                        onClearError = { viewModel.clearError() },
                        onLookupFood = { barcode -> viewModel.lookupFood(barcode) },
                        onClearLookupError = { viewModel.clearLookupError() }
                    )
                }
            }
        }
    }

    // Meal Selector Dialog
    if (uiState.showMealSelector) {
        MealSelectorDialog(
            uiState = uiState,
            onDismiss = { viewModel.hideMealSelector() },
            onMealTypeSelected = { mealType -> viewModel.setMealType(mealType) },
            onQuantityChanged = { quantity -> viewModel.setQuantity(quantity) },
            onConfirm = { viewModel.addSelectedFoodToLog() }
        )
    }

    // Success dialog
    if (uiState.logSuccess) {
        LaunchedEffect(uiState.logSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.hideMealSelector()
        }

        AlertDialog(
            onDismissRequest = { viewModel.hideMealSelector() },
            confirmButton = {
                TextButton(onClick = { viewModel.hideMealSelector() }) {
                    Text("OK")
                }
            },
            title = { Text("Success") },
            text = { Text("Food has been added to your log!") }
        )
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need camera access to scan barcodes for you.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun ScannerReadyContent(
    uiState: BarcodeScannerUiState,
    viewModel: BarcodeScannerViewModel,
    onStartScanning: () -> Unit,
    onClearBarcode: () -> Unit,
    onClearError: () -> Unit,
    onLookupFood: (String) -> Unit,
    onClearLookupError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.scannedBarcode == null) {
            // Initial scan state
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Ready to Scan",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Point your camera at a barcode to get started",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onStartScanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Scanning")
            }
        } else {
            // Show scanned barcode and lookup results
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Scanned Barcode:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.scannedBarcode,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    if (uiState.isLookingUp) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Looking up food information...")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClearBarcode,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear")
                            }
                            Button(
                                onClick = { onLookupFood(uiState.scannedBarcode) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Lookup Food")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show found food information
        uiState.foundFood?.let { food ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = food.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (!food.brand.isNullOrBlank()) {
                                Text(
                                    text = food.brand,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (!food.isCustom) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nutritional information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NutritionInfo("Calories", "${food.calories.toInt()}")
                        NutritionInfo("Protein", "${food.protein.toInt()}g")
                        NutritionInfo("Carbs", "${food.carbs.toInt()}g")
                        NutritionInfo("Fat", "${food.fat.toInt()}g")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Per ${food.servingSize?.toInt() ?: 100}${food.servingUnit ?: "g"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearBarcode,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Scan Another")
                        }
                        Button(
                            onClick = { viewModel.showMealSelector() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLogging
                        ) {
                            if (uiState.isLogging) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Add to Log")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show lookup error if available
        uiState.lookupError?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lookup Error:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onClearLookupError,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show error if available
        uiState.error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onClearError,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show general message when no barcode is scanned
        if (uiState.scannedBarcode == null && uiState.error == null) {
            Text(
                text = "Scan a barcode to find food information and add it to your daily log.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NutritionInfo(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSelectorDialog(
    uiState: BarcodeScannerUiState,
    onDismiss: () -> Unit,
    onMealTypeSelected: (String) -> Unit,
    onQuantityChanged: (Double) -> Unit,
    onConfirm: () -> Unit
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !uiState.isLogging
            ) {
                if (uiState.isLogging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add to Log")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Add to Food Log")
        },
        text = {
            Column {
                uiState.foundFood?.let { food ->
                    Text(
                        text = "Adding: ${food.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Meal Type:")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mealTypes.forEach { mealType ->
                        FilterChip(
                            onClick = { onMealTypeSelected(mealType) },
                            label = { Text(mealType) },
                            selected = uiState.selectedMealType == mealType,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Quantity (grams):")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.selectedQuantity.toInt().toString(),
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { quantity ->
                            if (quantity > 0) {
                                onQuantityChanged(quantity)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show calculated nutrition for selected quantity
                uiState.foundFood?.let { food ->
                    val multiplier = uiState.selectedQuantity / 100.0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Nutrition for ${uiState.selectedQuantity.toInt()}g:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Calories: ${(food.calories * multiplier).toInt()}")
                                Text("Protein: ${(food.protein * multiplier).toInt()}g")
                                Text("Carbs: ${(food.carbs * multiplier).toInt()}g")
                                Text("Fat: ${(food.fat * multiplier).toInt()}g")
                            }
                        }
                    }
                }
            }
        }
    )
}
