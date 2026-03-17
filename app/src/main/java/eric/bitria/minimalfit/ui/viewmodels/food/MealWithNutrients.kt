package eric.bitria.minimalfit.ui.viewmodels.food

import eric.bitria.minimalfit.data.entity.food.Meal

data class MealWithNutrients(
    val meal: Meal,
    val totalCalories: Int,
    val totalAmount: Float
)
