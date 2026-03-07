package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Meal(
    val id: Int,
    val name: String,
    val calories: Int,
    val imageUrl: String? = null,
    val description: String? = null
)

data class DailyCalorieData(
    val dayName: String,
    val dayNumber: Int,
    val currentCalories: Int,
    val goalCalories: Int
)

data class FoodUiState(
    val weeklyProgress: List<DailyCalorieData> = emptyList(),
    val savedMeals: List<Meal> = emptyList()
)

class FoodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    init {
        // Hardcoded data for weekly progress
        val weeklyData = listOf(
            DailyCalorieData("MON", 10, 1850, 2500),
            DailyCalorieData("TUE", 11, 2100, 2500),
            DailyCalorieData("WED", 12, 2600, 2500),
            DailyCalorieData("THU", 13, 1400, 2500),
            DailyCalorieData("FRI", 14, 2200, 2500),
            DailyCalorieData("SAT", 15, 1900, 2500),
            DailyCalorieData("SUN", 16, 500, 2500)
        )

        _uiState.value = _uiState.value.copy(
            weeklyProgress = weeklyData,
            savedMeals = listOf(
                Meal(
                    1, "Oatmeal with Berries", 350,
                    imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop",
                    description = "Healthy breakfast"
                ),
                Meal(
                    2, "Grilled Chicken Salad", 450,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop",
                    description = "Lunch"
                ),
                Meal(
                    3, "Protein Shake", 200,
                    imageUrl = null,
                    description = "Post-workout"
                ),
                Meal(
                    4, "Salmon and Asparagus", 600,
                    imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop",
                    description = "Dinner"
                ),
                Meal(
                    5, "Greek Yogurt", 150,
                    imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500&auto=format&fit=crop",
                    description = "Snack"
                ),
                Meal(
                    6, "Avocado Toast", 300,
                    imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop",
                    description = "Breakfast"
                ),
                Meal(
                    7, "Beef Stir Fry", 550,
                    imageUrl = "https://images.unsplash.com/photo-1512058560366-cd2427ff56f3?q=80&w=500&auto=format&fit=crop",
                    description = "Lunch"
                )
            )
        )
    }
}
