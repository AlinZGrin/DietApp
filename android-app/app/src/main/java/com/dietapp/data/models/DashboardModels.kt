package com.dietapp.data.models

data class DailySummary(
    val userId: String,
    val date: java.util.Date,
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalCaloriesBurned: Double = 0.0,
    val netCalories: Double = totalCalories - totalCaloriesBurned,
    val goalCalories: Int = 2000,
    val goalProtein: Double = 150.0,
    val goalCarbs: Double = 250.0,
    val goalFat: Double = 65.0
) {
    val calorieProgress: Float
        get() = if (goalCalories > 0) (totalCalories / goalCalories).toFloat().coerceAtMost(1f) else 0f

    val proteinProgress: Float
        get() = if (goalProtein > 0) (totalProtein / goalProtein).toFloat().coerceAtMost(1f) else 0f

    val carbProgress: Float
        get() = if (goalCarbs > 0) (totalCarbs / goalCarbs).toFloat().coerceAtMost(1f) else 0f

    val fatProgress: Float
        get() = if (goalFat > 0) (totalFat / goalFat).toFloat().coerceAtMost(1f) else 0f
}

data class WeightTrend(
    val currentWeight: Double,
    val previousWeight: Double?,
    val weightChange: Double = currentWeight - (previousWeight ?: currentWeight),
    val trend: TrendDirection = when {
        weightChange > 0.1 -> TrendDirection.UP
        weightChange < -0.1 -> TrendDirection.DOWN
        else -> TrendDirection.STABLE
    }
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

data class MotivationalTip(
    val id: String,
    val title: String,
    val message: String,
    val category: TipCategory
)

enum class TipCategory {
    NUTRITION, EXERCISE, MOTIVATION, HABIT, GENERAL
}
