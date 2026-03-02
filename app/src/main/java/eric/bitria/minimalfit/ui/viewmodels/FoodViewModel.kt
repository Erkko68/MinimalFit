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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SavedMeal(
    val name: String,
    val calories: Int,
    val description: String,
    val tags: List<String>,
    val color: Color,
    val icon: ImageVector
)

class FoodViewModel : ViewModel() {

    val savedMeals: StateFlow<List<SavedMeal>> get() = _savedMeals

    private val _savedMeals = MutableStateFlow(listOf(
        SavedMeal("Oatmeal & Berries", 350, "Rolled oats, almond milk, strawberries.", listOf("Breakfast"), Vivid8, Icons.Filled.BreakfastDining),
        SavedMeal("Greek Yogurt", 200, "Low fat yogurt with honey.", listOf("Breakfast"), Vivid7, Icons.Filled.BreakfastDining),
        SavedMeal("Grilled Chicken Salad", 450, "Mixed greens, chicken breast.", listOf("Lunch"), Vivid6, Icons.Filled.LunchDining),
        SavedMeal("Quinoa Bowl", 400, "Quinoa, roasted veggies, chickpeas.", listOf("Lunch"), Vivid5, Icons.Filled.LunchDining),
        SavedMeal("Salmon & Asparagus", 500, "Baked salmon with grilled asparagus.", listOf("Dinner"), Vivid4, Icons.Filled.DinnerDining),
        SavedMeal("Protein Shake", 150, "Whey protein with water.", listOf("Snack"), Vivid3, Icons.Filled.Restaurant),
        SavedMeal("Apple & Peanut Butter", 180, "Sliced apple with 1 tbsp peanut butter.", listOf("Snack"), Vivid2, Icons.Filled.Restaurant),
        // Ingredients
        SavedMeal("Egg", 70, "1 large boiled egg.", listOf("Breakfast", "Ingredient"), Vivid1, Icons.Filled.Egg),
        SavedMeal("Chicken Breast", 165, "100g grilled chicken breast.", listOf("Lunch", "Ingredient"), Vivid2, Icons.Filled.Restaurant),
        SavedMeal("Almonds", 160, "28g of raw almonds.", listOf("Snack", "Ingredient"), Vivid3, Icons.Filled.Restaurant)
    ))

    val availableTags: StateFlow<List<String>> = _savedMeals.map { meals ->
        meals.flatMap { it.tags }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tagFilter = MutableStateFlow<String?>(null)
    val tagFilter: StateFlow<String?> = _tagFilter

    val filteredMeals: StateFlow<List<SavedMeal>> = combine(
        _savedMeals, _tagFilter
    ) { meals, tag ->
        if (tag == null) meals
        else meals.filter { meal -> meal.tags.contains(tag) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTagFilter(tag: String?) {
        _tagFilter.value = tag
    }

    private val _mockDates = MutableStateFlow(listOf("Sun, 22 Feb", "Mon, 23 Feb", "Tue, 24 Feb", "Wed, 25 Feb", "Today, 26 Feb"))
    val mockDates: StateFlow<List<String>> = _mockDates

    fun getMealsByTag() = _savedMeals.value
        .flatMap { meal -> meal.tags.map { tag -> tag to meal } }
        .groupBy({ it.first }, { it.second })
}
