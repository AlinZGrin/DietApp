package com.dietapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dietapp.ui.viewmodels.GoalSetupViewModel
import com.dietapp.utils.UnitConverter
import com.dietapp.utils.WeightUnit
import com.dietapp.utils.HeightUnit
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    onNavigateBack: () -> Unit,
    onGoalCreated: () -> Unit = {},
    viewModel: GoalSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isGoalCreated) {
        if (uiState.isGoalCreated) {
            onGoalCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Your Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { viewModel.createGoal() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isValid && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Goal")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Personal Information Section
            PersonalInfoSection(
                currentWeight = uiState.currentWeight,
                onCurrentWeightChange = viewModel::updateCurrentWeight,
                targetWeight = uiState.targetWeight,
                onTargetWeightChange = viewModel::updateTargetWeight,
                height = uiState.height,
                onHeightChange = viewModel::updateHeight,
                heightFeet = uiState.heightFeet,
                onHeightFeetChange = viewModel::updateHeightFeet,
                heightInches = uiState.heightInches,
                onHeightInchesChange = viewModel::updateHeightInches,
                age = uiState.age,
                onAgeChange = viewModel::updateAge,
                gender = uiState.gender,
                onGenderChange = viewModel::updateGender,
                useImperialUnits = uiState.useImperialUnits,
                onToggleUnits = viewModel::toggleUnits
            )

            // Goal Type Selection
            GoalTypeSection(
                selectedGoalType = uiState.goalType,
                onGoalTypeChange = viewModel::updateGoalType
            )

            // Activity Level Selection
            ActivityLevelSection(
                selectedActivityLevel = uiState.activityLevel,
                onActivityLevelChange = viewModel::updateActivityLevel
            )

            // Weekly Goal Section
            if (uiState.goalType != "maintenance") {
                WeeklyGoalSection(
                    goalType = uiState.goalType,
                    weeklyGoal = uiState.weeklyWeightGoal,
                    onWeeklyGoalChange = viewModel::updateWeeklyWeightGoal,
                    useImperialUnits = uiState.useImperialUnits
                )
            }

            // Calculated Values Display
            CalculatedValuesSection(
                targetCalories = uiState.calculatedCalories,
                targetProtein = uiState.calculatedProtein,
                targetCarbs = uiState.calculatedCarbs,
                targetFat = uiState.calculatedFat,
                bmr = uiState.calculatedBMR,
                tdee = uiState.calculatedTDEE
            )

            // Error Display
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Bottom padding for the bottom bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoSection(
    currentWeight: String,
    onCurrentWeightChange: (String) -> Unit,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    heightFeet: String,
    onHeightFeetChange: (String) -> Unit,
    heightInches: String,
    onHeightInchesChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    useImperialUnits: Boolean,
    onToggleUnits: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Unit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Units:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    onClick = { if (useImperialUnits) onToggleUnits() },
                    label = { Text("Metric (kg/cm)") },
                    selected = !useImperialUnits
                )
                FilterChip(
                    onClick = { if (!useImperialUnits) onToggleUnits() },
                    label = { Text("Imperial (lbs/ft)") },
                    selected = useImperialUnits
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = currentWeight,
                    onValueChange = onCurrentWeightChange,
                    label = { Text("Current Weight (${if (useImperialUnits) "lbs" else "kg"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = onTargetWeightChange,
                    label = { Text("Target Weight (${if (useImperialUnits) "lbs" else "kg"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Height input - different for imperial vs metric
                if (useImperialUnits) {
                    OutlinedTextField(
                        value = heightFeet,
                        onValueChange = onHeightFeetChange,
                        label = { Text("Feet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.4f)
                    )
                    OutlinedTextField(
                        value = heightInches,
                        onValueChange = onHeightInchesChange,
                        label = { Text("Inches") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.4f)
                    )
                } else {
                    OutlinedTextField(
                        value = height,
                        onValueChange = onHeightChange,
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.2f)
                )
            }

            Column {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf("Male", "Female").forEach { option ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = gender == option,
                                    onClick = { onGenderChange(option) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gender == option,
                                onClick = null
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTypeSection(
    selectedGoalType: String,
    onGoalTypeChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "What's your goal?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val goalOptions = listOf(
                "weight_loss" to "Lose Weight",
                "weight_gain" to "Gain Weight",
                "muscle_gain" to "Build Muscle",
                "maintenance" to "Maintain Weight"
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goalOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedGoalType == value,
                                onClick = { onGoalTypeChange(value) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGoalType == value,
                            onClick = null
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLevelSection(
    selectedActivityLevel: String,
    onActivityLevelChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Activity Level",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val activityOptions = listOf(
                "sedentary" to "Sedentary (desk job, no exercise)",
                "light" to "Light (exercise 1-3 days/week)",
                "moderate" to "Moderate (exercise 3-5 days/week)",
                "active" to "Active (exercise 6-7 days/week)",
                "very_active" to "Very Active (2x/day or intense exercise)"
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedActivityLevel == value,
                                onClick = { onActivityLevelChange(value) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedActivityLevel == value,
                            onClick = null
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = label.split(" (")[0],
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "(${label.split(" (")[1]}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalSection(
    goalType: String,
    weeklyGoal: String,
    onWeeklyGoalChange: (String) -> Unit,
    useImperialUnits: Boolean
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val goalText = when (goalType) {
                "weight_loss" -> "Weekly Weight Loss Goal"
                "weight_gain" -> "Weekly Weight Gain Goal"
                "muscle_gain" -> "Weekly Weight Gain Goal"
                else -> "Weekly Goal"
            }

            Text(
                text = goalText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val unitLabel = if (useImperialUnits) "lbs per week" else "kg per week"

            OutlinedTextField(
                value = weeklyGoal,
                onValueChange = onWeeklyGoalChange,
                label = { Text(unitLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    val recommendedRange = when (goalType) {
                        "weight_loss" -> if (useImperialUnits) {
                            "Recommended: 1.0-2.2 lbs/week"
                        } else {
                            "Recommended: 0.5-1.0 kg/week"
                        }
                        "weight_gain" -> if (useImperialUnits) {
                            "Recommended: 0.5-1.1 lbs/week"
                        } else {
                            "Recommended: 0.25-0.5 kg/week"
                        }
                        "muscle_gain" -> if (useImperialUnits) {
                            "Recommended: 0.5-1.1 lbs/week"
                        } else {
                            "Recommended: 0.25-0.5 kg/week"
                        }
                        else -> ""
                    }
                    Text(recommendedRange)
                }
            )
        }
    }
}

@Composable
private fun CalculatedValuesSection(
    targetCalories: Double,
    targetProtein: Double,
    targetCarbs: Double,
    targetFat: Double,
    bmr: Double,
    tdee: Double
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Calculated Targets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricRow("BMR (Basal Metabolic Rate)", "${bmr.toInt()} cal/day")
                MetricRow("TDEE (Total Daily Energy)", "${tdee.toInt()} cal/day")

                Divider()

                MetricRow("Target Calories", "${targetCalories.toInt()} cal/day", isHighlighted = true)
                MetricRow("Target Protein", "${targetProtein.toInt()}g/day")
                MetricRow("Target Carbs", "${targetCarbs.toInt()}g/day")
                MetricRow("Target Fat", "${targetFat.toInt()}g/day")
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlighted) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
