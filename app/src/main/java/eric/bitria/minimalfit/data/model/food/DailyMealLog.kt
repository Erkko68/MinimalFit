package eric.bitria.minimalfit.data.model.food

import java.time.LocalDate

data class DailyMealLog(
    val date: LocalDate,
    val meals: List<MealLog> = emptyList(),
    val calorieGoal: Int = 2500
) {
    val totalCalories: Int get() = meals.sumOf { it.meal.calories }
}

