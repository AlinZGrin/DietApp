package com.dietapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dietapp.data.entities.FoodLog
import com.dietapp.ui.viewmodels.FoodLoggingViewModel
import com.dietapp.ui.viewmodels.FoodLoggingUiState
import com.dietapp.ui.viewmodels.FoodLogWithFood
import androidx.compose.foundation.text.KeyboardOptions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLoggingScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFoodSearch: (String) -> Unit = {},
    onNavigateToBarCodeScanner: () -> Unit = {},
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null,
    viewModel: FoodLoggingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle food selection result
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.let { handle ->
            val foodId = handle.get<String>("food_id")
            val selectedMealType = handle.get<String>("selected_meal_type")

            println("DEBUG FoodLoggingScreen: Received food_id: $foodId, meal_type: $selectedMealType")

            if (foodId != null && selectedMealType != null) {
                // Reconstruct Food object from individual values
                val selectedFood = com.dietapp.data.entities.Food(
                    id = foodId,
                    name = handle.get<String>("food_name") ?: "",
                    brand = handle.get<String>("food_brand"),
                    calories = handle.get<Double>("food_calories") ?: 0.0,
                    protein = handle.get<Double>("food_protein") ?: 0.0,
                    carbs = handle.get<Double>("food_carbs") ?: 0.0,
                    fat = handle.get<Double>("food_fat") ?: 0.0,
                    fiber = handle.get<Double>("food_fiber"),
                    sugar = handle.get<Double>("food_sugar"),
                    sodium = handle.get<Double>("food_sodium"),
                    barcode = handle.get<String>("food_barcode"),
                    servingSize = handle.get<Double>("food_serving_size"),
                    servingUnit = handle.get<String>("food_serving_unit"),
                    createdAt = Date(),
                    isCustom = handle.get<Boolean>("food_is_custom") ?: false
                )

                println("DEBUG FoodLoggingScreen: Reconstructed food: ${selectedFood.name}, calories: ${selectedFood.calories}")
                println("DEBUG FoodLoggingScreen: Calling addFoodToLog...")

                viewModel.addFoodToLog(selectedFood, selectedMealType)

                println("DEBUG FoodLoggingScreen: addFoodToLog call completed")

                // Clear all the result values to prevent re-adding
                handle.remove<String>("food_id")
                handle.remove<String>("food_name")
                handle.remove<String>("food_brand")
                handle.remove<Double>("food_calories")
                handle.remove<Double>("food_protein")
                handle.remove<Double>("food_carbs")
                handle.remove<Double>("food_fat")
                handle.remove<Double>("food_fiber")
                handle.remove<Double>("food_sugar")
                handle.remove<Double>("food_sodium")
                handle.remove<String>("food_barcode")
                handle.remove<Double>("food_serving_size")
                handle.remove<String>("food_serving_unit")
                handle.remove<Boolean>("food_is_custom")
                handle.remove<String>("selected_meal_type")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToBarCodeScanner() }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date selector
            DateSelector(
                selectedDate = uiState.selectedDate,
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() },
                onToday = { viewModel.goToToday() },
                isToday = viewModel.isToday()
            )

            // Daily nutrition summary
            DailyNutritionSummary(
                totalCalories = uiState.totalCalories,
                totalProtein = uiState.totalProtein,
                totalCarbs = uiState.totalCarbs,
                totalFat = uiState.totalFat
            )

            // Food logs by meal
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

                    items(mealTypes) { mealType ->
                        MealSection(
                            mealType = mealType,
                            foodLogs = uiState.groupedLogs[mealType] ?: emptyList(),
                            onAddFood = { viewModel.showAddFoodDialog(mealType) },
                            onEditFood = { foodLogWithFood -> viewModel.showEditDialog(foodLogWithFood) },
                            onDeleteFood = { foodLogWithFood -> viewModel.deleteFoodLog(foodLogWithFood) }
                        )
                    }
                }
            }
        }
    }

    // Add Food Dialog (placeholder - would navigate to food search)
    if (uiState.showAddFoodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddFoodDialog() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hideAddFoodDialog()
                    onNavigateToFoodSearch(uiState.selectedMealType)
                }) {
                    Text("Search Foods")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.hideAddFoodDialog()
                    onNavigateToBarCodeScanner()
                }) {
                    Text("Scan Barcode")
                }
            },
            title = { Text("Add Food to ${uiState.selectedMealType}") },
            text = { Text("How would you like to add food?") }
        )
    }

    // Edit Food Dialog
    if (uiState.showEditDialog && uiState.editingFoodLog != null) {
        EditFoodDialog(
            foodLogWithFood = uiState.editingFoodLog!!,
            onDismiss = { viewModel.hideEditDialog() },
            onUpdate = { quantity, mealType ->
                viewModel.updateFoodLog(uiState.editingFoodLog!!, quantity, mealType)
            }
        )
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: Date,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    isToday: Boolean
) {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateFormat.format(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!isToday) {
                    TextButton(onClick = onToday) {
                        Text("Go to Today")
                    }
                }
            }

            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
            }
        }
    }
}

@Composable
private fun DailyNutritionSummary(
    totalCalories: Double,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Totals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem("Calories", "${totalCalories.toInt()}", "kcal")
                NutritionItem("Protein", "${totalProtein.toInt()}", "g")
                NutritionItem("Carbs", "${totalCarbs.toInt()}", "g")
                NutritionItem("Fat", "${totalFat.toInt()}", "g")
            }
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSection(
    mealType: String,
    foodLogs: List<FoodLogWithFood>,
    onAddFood: () -> Unit,
    onEditFood: (FoodLogWithFood) -> Unit,
    onDeleteFood: (FoodLogWithFood) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealType,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val mealCalories = foodLogs.sumOf { it.foodLog.calories }
                Text(
                    text = "${mealCalories.toInt()} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (foodLogs.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onAddFood,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Food")
                    }
                }
            } else {
                Column {
                    foodLogs.forEach { foodLogWithFood ->
                        FoodLogItem(
                            foodLogWithFood = foodLogWithFood,
                            onEdit = { onEditFood(foodLogWithFood) },
                            onDelete = { onDeleteFood(foodLogWithFood) }
                        )
                        if (foodLogWithFood != foodLogs.last()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onAddFood,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add More Food")
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodLogItem(
    foodLogWithFood: FoodLogWithFood,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val foodLog = foodLogWithFood.foodLog
    val food = foodLogWithFood.food

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = food?.name ?: "Unknown Food",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            food?.brand?.let { brand ->
                Text(
                    text = brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "${foodLog.quantity.toInt()}${foodLog.unit} • ${foodLog.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFoodDialog(
    foodLogWithFood: FoodLogWithFood,
    onDismiss: () -> Unit,
    onUpdate: (quantity: Double, mealType: String) -> Unit
) {
    val foodLog = foodLogWithFood.foodLog
    val food = foodLogWithFood.food
    var quantity by remember { mutableStateOf(foodLog.quantity.toString()) }
    var selectedMealType by remember { mutableStateOf(foodLog.mealType) }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    quantity.toDoubleOrNull()?.let { qty ->
                        if (qty > 0) {
                            onUpdate(qty, selectedMealType)
                        }
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Food Entry") },
        text = {
            Column {
                food?.let {
                    Text(
                        text = "Editing: ${it.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    it.brand?.let { brand ->
                        Text(
                            text = brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Quantity (grams):")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Meal Type:")
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    mealTypes.forEach { mealType ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMealType == mealType,
                                onClick = { selectedMealType = mealType }
                            )
                            Text(mealType)
                        }
                    }
                }
            }
        }
    )
}
