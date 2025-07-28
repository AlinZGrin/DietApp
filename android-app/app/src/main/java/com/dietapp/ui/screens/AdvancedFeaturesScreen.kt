package com.dietapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFeaturesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGoalSetup: () -> Unit = {},
    onNavigateToWaterTracking: () -> Unit = {},
    onNavigateToProgressTracking: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Features") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Advanced Diet & Fitness Features",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Unlock the full potential of your health journey with these advanced features.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Goal Setting & Nutrition Planning
            item {
                FeatureCard(
                    icon = Icons.Default.Flag,
                    title = "Smart Goal Setting",
                    description = "Set personalized nutrition and fitness goals based on your body metrics, activity level, and objectives. Automatic BMR and TDEE calculations.",
                    features = listOf(
                        "BMR & TDEE calculations",
                        "Personalized macro targets",
                        "Weight loss/gain planning",
                        "Activity level optimization"
                    ),
                    onClick = onNavigateToGoalSetup
                )
            }

            // Water Tracking
            item {
                FeatureCard(
                    icon = Icons.Default.WaterDrop,
                    title = "Hydration Monitoring",
                    description = "Track your daily water intake with intelligent recommendations based on your weight and activity level. Visual progress tracking.",
                    features = listOf(
                        "Daily hydration goals",
                        "Quick-add serving sizes",
                        "Hydration status indicators",
                        "Smart recommendations"
                    ),
                    onClick = onNavigateToWaterTracking
                )
            }

            // Progress Analytics
            item {
                FeatureCard(
                    icon = Icons.Default.TrendingUp,
                    title = "Advanced Analytics",
                    description = "Comprehensive charts and insights to track your progress over time. Weight trends, nutrition patterns, and goal achievement.",
                    features = listOf(
                        "Weight progress charts",
                        "Nutrition trend analysis",
                        "Goal achievement tracking",
                        "Weekly/monthly summaries"
                    ),
                    onClick = onNavigateToProgressTracking
                )
            }

            // Meal Planning (Placeholder for future feature)
            item {
                FeatureCard(
                    icon = Icons.Default.RestaurantMenu,
                    title = "Meal Planning (Coming Soon)",
                    description = "Plan your meals in advance with AI-powered suggestions based on your nutritional goals and food preferences.",
                    features = listOf(
                        "Weekly meal planning",
                        "Recipe suggestions",
                        "Shopping list generation",
                        "Macro-balanced meals"
                    ),
                    onClick = { /* TODO: Implement meal planning */ },
                    isComingSoon = true
                )
            }

            // Workout Integration (Placeholder for future feature)
            item {
                FeatureCard(
                    icon = Icons.Default.FitnessCenter,
                    title = "Workout Integration (Coming Soon)",
                    description = "Sync with fitness apps and devices to track calories burned, adjust nutrition goals, and monitor overall health metrics.",
                    features = listOf(
                        "Exercise logging",
                        "Calorie burn tracking",
                        "Health app integration",
                        "Activity-based adjustments"
                    ),
                    onClick = { /* TODO: Implement workout integration */ },
                    isComingSoon = true
                )
            }

            // AI Insights (Placeholder for future feature)
            item {
                FeatureCard(
                    icon = Icons.Default.Psychology,
                    title = "AI Health Insights (Coming Soon)",
                    description = "Get personalized recommendations and insights powered by machine learning to optimize your nutrition and fitness journey.",
                    features = listOf(
                        "Personalized recommendations",
                        "Habit analysis",
                        "Predictive health insights",
                        "Custom advice"
                    ),
                    onClick = { /* TODO: Implement AI insights */ },
                    isComingSoon = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    features: List<String>,
    onClick: () -> Unit,
    isComingSoon: Boolean = false
) {
    Card(
        onClick = if (!isComingSoon) onClick else { {} },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isComingSoon
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isComingSoon)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isComingSoon)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    if (isComingSoon) {
                        Text(
                            text = "Coming Soon",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (!isComingSoon) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isComingSoon)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isComingSoon)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
