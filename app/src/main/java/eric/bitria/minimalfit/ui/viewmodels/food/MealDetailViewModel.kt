package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealDetailUiState(
    val meal: Meal? = null
)

class MealDetailViewModel(
    private val mealId: String,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    val uiState: StateFlow<MealDetailUiState> = foodCatalog.getMeal(mealId)
        .map { meal -> MealDetailUiState(meal = meal) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MealDetailUiState()
        )

    fun updateMeal(meal: Meal) {
        viewModelScope.launch {
            foodCatalog.updateMeal(meal)
        }
    }

    fun deleteMeal() {
        viewModelScope.launch {
            foodCatalog.deleteMeal(mealId)
        }
    }
}
