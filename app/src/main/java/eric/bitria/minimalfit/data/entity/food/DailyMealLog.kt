package eric.bitria.minimalfit.data.entity.food

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
data class DailyMealLog(
    val date: LocalDate,
    val meals: List<MealLog> = emptyList(),
    val calorieGoal: Int = 2500
) {
    val totalCalories: Int get() = meals.sumOf { it.meal.calories }
}