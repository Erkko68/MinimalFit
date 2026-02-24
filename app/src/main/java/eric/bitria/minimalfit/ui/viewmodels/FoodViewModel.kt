package eric.bitria.minimalfit.ui.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SavedMeal(
    val name: String,
    val calories: Int,
    val description: String,
    val category: MealCategory,
    val color: Color,
    val icon: ImageVector,
    val type: FoodItemType = FoodItemType.Meal
)

enum class MealCategory {
    All, Breakfast, Lunch, Dinner, Snack
}

enum class FoodItemType {
    All, Meal, Ingredient
}

class FoodViewModel : ViewModel() {

    private val _savedMeals = MutableStateFlow(listOf(
        SavedMeal("Oatmeal & Berries", 350, "Rolled oats, almond milk, strawberries.", MealCategory.Breakfast, Vivid8, Icons.Filled.BreakfastDining),
        SavedMeal("Greek Yogurt", 200, "Low fat yogurt with honey.", MealCategory.Breakfast, Vivid7, Icons.Filled.BreakfastDining),
        SavedMeal("Grilled Chicken Salad", 450, "Mixed greens, chicken breast.", MealCategory.Lunch, Vivid6, Icons.Filled.LunchDining),
        SavedMeal("Quinoa Bowl", 400, "Quinoa, roasted veggies, chickpeas.", MealCategory.Lunch, Vivid5, Icons.Filled.LunchDining),
        SavedMeal("Salmon & Asparagus", 500, "Baked salmon with grilled asparagus.", MealCategory.Dinner, Vivid4, Icons.Filled.DinnerDining),
        SavedMeal("Protein Shake", 150, "Whey protein with water.", MealCategory.Snack, Vivid3, Icons.Filled.Restaurant),
        SavedMeal("Apple & Peanut Butter", 180, "Sliced apple with 1 tbsp peanut butter.", MealCategory.Snack, Vivid2, Icons.Filled.Restaurant),
        // Ingredients
        SavedMeal("Egg", 70, "1 large boiled egg.", MealCategory.Breakfast, Vivid1, Icons.Filled.Egg, FoodItemType.Ingredient),
        SavedMeal("Chicken Breast", 165, "100g grilled chicken breast.", MealCategory.Lunch, Vivid2, Icons.Filled.Restaurant, FoodItemType.Ingredient),
        SavedMeal("Almonds", 160, "28g of raw almonds.", MealCategory.Snack, Vivid3, Icons.Filled.Restaurant, FoodItemType.Ingredient)
    ))

    private val _categoryFilter = MutableStateFlow(MealCategory.All)
    val categoryFilter: StateFlow<MealCategory> = _categoryFilter

    private val _typeFilter = MutableStateFlow(FoodItemType.All)
    val typeFilter: StateFlow<FoodItemType> = _typeFilter

    val filteredMeals: StateFlow<List<SavedMeal>> = combine(
        _savedMeals, _categoryFilter, _typeFilter
    ) { meals, category, type ->
        meals.filter { meal ->
            (category == MealCategory.All || meal.category == category) &&
            (type == FoodItemType.All || meal.type == type)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategoryFilter(category: MealCategory) {
        _categoryFilter.value = category
    }

    fun setTypeFilter(type: FoodItemType) {
        _typeFilter.value = type
    }

    private val _mockDates = MutableStateFlow(listOf("Sun, 22 Feb", "Mon, 23 Feb", "Tue, 24 Feb", "Wed, 25 Feb", "Today, 26 Feb"))
    val mockDates: StateFlow<List<String>> = _mockDates

    fun getMealsByCategory() = _savedMeals.value.groupBy { it.category }
}
