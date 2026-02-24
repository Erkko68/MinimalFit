package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SavedMeal(val name: String, val calories: Int, val description: String, val category: MealCategory)

enum class MealCategory {
    Breakfast, Lunch, Dinner, Snack
}

class FoodViewModel : ViewModel() {

    private val _savedMeals = MutableStateFlow(listOf(
        SavedMeal("Oatmeal & Berries", 350, "Rolled oats, almond milk, strawberries.", MealCategory.Breakfast),
        SavedMeal("Greek Yogurt", 200, "Low fat yogurt with honey.", MealCategory.Breakfast),
        SavedMeal("Grilled Chicken Salad", 450, "Mixed greens, chicken breast.", MealCategory.Lunch),
        SavedMeal("Quinoa Bowl", 400, "Quinoa, roasted veggies, chickpeas.", MealCategory.Lunch),
        SavedMeal("Salmon & Asparagus", 500, "Baked salmon with grilled asparagus.", MealCategory.Dinner),
        SavedMeal("Protein Shake", 150, "Whey protein with water.", MealCategory.Snack),
        SavedMeal("Apple & Peanut Butter", 180, "Sliced apple with 1 tbsp peanut butter.", MealCategory.Snack)
    ))
    val savedMeals: StateFlow<List<SavedMeal>> = _savedMeals

    private val _mockDates = MutableStateFlow(listOf("Sun, 22 Feb", "Mon, 23 Feb", "Today, 24 Feb", "Wed, 25 Feb", "Thu, 26 Feb"))
    val mockDates: StateFlow<List<String>> = _mockDates

    fun getMealsByCategory() = _savedMeals.value.groupBy { it.category }
}
