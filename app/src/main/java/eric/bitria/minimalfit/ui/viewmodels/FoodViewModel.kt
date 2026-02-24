package eric.bitria.minimalfit.ui.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.ui.theme.Vivid1
import eric.bitria.minimalfit.ui.theme.Vivid2
import eric.bitria.minimalfit.ui.theme.Vivid3
import eric.bitria.minimalfit.ui.theme.Vivid4
import eric.bitria.minimalfit.ui.theme.Vivid5
import eric.bitria.minimalfit.ui.theme.Vivid6
import eric.bitria.minimalfit.ui.theme.Vivid7
import eric.bitria.minimalfit.ui.theme.Vivid8
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SavedMeal(
    val name: String,
    val calories: Int,
    val description: String,
    val category: MealCategory,
    val color: Color,
    val icon: ImageVector
)

enum class MealCategory {
    Breakfast, Lunch, Dinner, Snack
}

class FoodViewModel : ViewModel() {

    private val _savedMeals = MutableStateFlow(listOf(
        SavedMeal("Oatmeal & Berries", 350, "Rolled oats, almond milk, strawberries.", MealCategory.Breakfast, Vivid8, Icons.Filled.BreakfastDining),
        SavedMeal("Greek Yogurt", 200, "Low fat yogurt with honey.", MealCategory.Breakfast, Vivid7, Icons.Filled.BreakfastDining),
        SavedMeal("Grilled Chicken Salad", 450, "Mixed greens, chicken breast.", MealCategory.Lunch, Vivid6, Icons.Filled.LunchDining),
        SavedMeal("Quinoa Bowl", 400, "Quinoa, roasted veggies, chickpeas.", MealCategory.Lunch, Vivid5, Icons.Filled.LunchDining),
        SavedMeal("Salmon & Asparagus", 500, "Baked salmon with grilled asparagus.", MealCategory.Dinner, Vivid4, Icons.Filled.DinnerDining),
        SavedMeal("Protein Shake", 150, "Whey protein with water.", MealCategory.Snack, Vivid3, Icons.Filled.Restaurant),
        SavedMeal("Apple & Peanut Butter", 180, "Sliced apple with 1 tbsp peanut butter.", MealCategory.Snack, Vivid2, Icons.Filled.Restaurant)
    ))
    val savedMeals: StateFlow<List<SavedMeal>> = _savedMeals

    private val _mockDates = MutableStateFlow(listOf("Sun, 22 Feb", "Mon, 23 Feb", "Tue, 24 Feb", "Wed, 25 Feb", "Today, 26 Feb"))
    val mockDates: StateFlow<List<String>> = _mockDates

    fun getMealsByCategory() = _savedMeals.value.groupBy { it.category }
}
