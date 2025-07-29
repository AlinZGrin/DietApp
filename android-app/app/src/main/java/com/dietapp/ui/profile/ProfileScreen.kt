package com.dietapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dietapp.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Auto-navigate to login when signed out
    LaunchedEffect(uiState.isSigningOut) {
        if (uiState.isSigningOut && uiState.userProfile == null) {
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                if (!uiState.isEditing) {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
                IconButton(onClick = { showSignOutDialog = true }) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
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
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        } else {
            val userProfile = uiState.userProfile
            if (userProfile != null) {
                if (uiState.isEditing) {
                    EditProfileContent(
                        profile = userProfile,
                        onSave = { name, age, gender, height, targetWeight, activityLevel, dietaryGoal ->
                            viewModel.updateProfile(name, age, gender, height, targetWeight, activityLevel, dietaryGoal)
                        },
                        onCancel = { viewModel.toggleEditMode() }
                    )
                } else {
                    ViewProfileContent(
                        profile = userProfile,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ViewProfileContent(
    profile: com.dietapp.data.entities.UserProfile,
    uiState: com.dietapp.ui.viewmodels.ProfileUiState
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Information
        item {
            ProfileSection(title = "Personal Information") {
                ProfileField(label = "Name", value = profile.name)
                ProfileField(label = "Email", value = profile.email)
                ProfileField(label = "Age", value = "${profile.age} years")
                ProfileField(label = "Gender", value = profile.gender)
                ProfileField(label = "Height", value = "${profile.height} cm")
            }
        }

        // Goals
        item {
            ProfileSection(title = "Goals & Targets") {
                ProfileField(
                    label = "Current Weight",
                    value = "${uiState.currentWeight?.let { "%.1f".format(it) } ?: "N/A"} kg"
                )
                ProfileField(label = "Target Weight", value = "${profile.targetWeight} kg")
                ProfileField(label = "Activity Level", value = profile.activityLevel)
                ProfileField(label = "Dietary Goal", value = profile.dietaryGoal)
            }
        }

        // Nutrition Goals
        item {
            ProfileSection(title = "Daily Nutrition Goals") {
                ProfileField(label = "Calories", value = "${profile.dailyCalorieGoal} kcal")
                ProfileField(label = "Protein", value = "${profile.dailyProteinGoal}g")
                ProfileField(label = "Carbohydrates", value = "${profile.dailyCarbGoal}g")
                ProfileField(label = "Fat", value = "${profile.dailyFatGoal}g")
            }
        }

        // Account Info
        item {
            ProfileSection(title = "Account") {
                ProfileField(label = "Member Since", value = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(profile.createdAt))
                ProfileField(label = "Last Updated", value = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(profile.updatedAt))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(
    profile: com.dietapp.data.entities.UserProfile,
    onSave: (String, Int, String, Double, Double, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var age by remember { mutableStateOf(profile.age.toString()) }
    var gender by remember { mutableStateOf(profile.gender) }
    var height by remember { mutableStateOf(profile.height.toString()) }
    var targetWeight by remember { mutableStateOf(profile.targetWeight.toString()) }
    var activityLevel by remember { mutableStateOf(profile.activityLevel) }
    var dietaryGoal by remember { mutableStateOf(profile.dietaryGoal) }

    val genderOptions = listOf("Male", "Female", "Other")
    val activityOptions = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")
    val goalOptions = listOf("Weight Loss", "Weight Gain", "Maintain Weight", "Muscle Gain")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Age and Height in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Weight
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Target Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gender Dropdown
                    var genderExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Activity Level Dropdown
                    var activityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = activityExpanded,
                        onExpandedChange = { activityExpanded = !activityExpanded }
                    ) {
                        OutlinedTextField(
                            value = activityLevel,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Activity Level") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = activityExpanded,
                            onDismissRequest = { activityExpanded = false }
                        ) {
                            activityOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        activityLevel = option
                                        activityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dietary Goal Dropdown
                    var goalExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = goalExpanded,
                        onExpandedChange = { goalExpanded = !goalExpanded }
                    ) {
                        OutlinedTextField(
                            value = dietaryGoal,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Dietary Goal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = goalExpanded,
                            onDismissRequest = { goalExpanded = false }
                        ) {
                            goalOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        dietaryGoal = option
                                        goalExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val ageInt = age.toIntOrNull() ?: profile.age
                                val heightDouble = height.toDoubleOrNull() ?: profile.height
                                val targetWeightDouble = targetWeight.toDoubleOrNull() ?: profile.targetWeight

                                onSave(name, ageInt, gender, heightDouble, targetWeightDouble, activityLevel, dietaryGoal)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
