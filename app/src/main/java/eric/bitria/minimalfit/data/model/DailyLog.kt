package eric.bitria.minimalfit.data.model

import java.time.LocalDate

data class DailyLog(
    val date: LocalDate,
    val meals: List<Meal> = emptyList(),
    val calorieGoal: Int = 2500
) {
    val totalCalories: Int get() = meals.sumOf { it.calories }
}

