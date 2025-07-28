package com.dietapp.utils

object UnitConverter {
    // Weight conversions
    fun poundsToKg(pounds: Double): Double = pounds * 0.453592
    fun kgToPounds(kg: Double): Double = kg / 0.453592

    // Height conversions
    fun feetInchesToCm(feet: Int, inches: Int): Double = ((feet * 12) + inches) * 2.54
    fun cmToFeetInches(cm: Double): Pair<Int, Int> {
        val totalInches = cm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).toInt()
        return Pair(feet, inches)
    }

    // BMI calculation
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    // Format weight with unit
    fun formatWeight(weightKg: Double, useImperial: Boolean): String {
        return if (useImperial) {
            "${String.format("%.1f", kgToPounds(weightKg))} lbs"
        } else {
            "${String.format("%.1f", weightKg)} kg"
        }
    }

    // Format height with unit
    fun formatHeight(heightCm: Double, useImperial: Boolean): String {
        return if (useImperial) {
            val (feet, inches) = cmToFeetInches(heightCm)
            "${feet}' ${inches}\""
        } else {
            "${String.format("%.1f", heightCm)} cm"
        }
    }
}

enum class WeightUnit {
    KG, LBS
}

enum class HeightUnit {
    CM, FEET_INCHES
}

data class WeightWithUnit(
    val value: Double,
    val unit: WeightUnit
) {
    fun toKg(): Double = when (unit) {
        WeightUnit.KG -> value
        WeightUnit.LBS -> UnitConverter.poundsToKg(value)
    }

    fun toPounds(): Double = when (unit) {
        WeightUnit.KG -> UnitConverter.kgToPounds(value)
        WeightUnit.LBS -> value
    }
}

data class HeightWithUnit(
    val feet: Int? = null,
    val inches: Int? = null,
    val cm: Double? = null,
    val unit: HeightUnit
) {
    fun toCm(): Double = when (unit) {
        HeightUnit.CM -> cm ?: 0.0
        HeightUnit.FEET_INCHES -> UnitConverter.feetInchesToCm(feet ?: 0, inches ?: 0)
    }

    fun toFeetInches(): Pair<Int, Int> = when (unit) {
        HeightUnit.CM -> UnitConverter.cmToFeetInches(cm ?: 0.0)
        HeightUnit.FEET_INCHES -> Pair(feet ?: 0, inches ?: 0)
    }
}
