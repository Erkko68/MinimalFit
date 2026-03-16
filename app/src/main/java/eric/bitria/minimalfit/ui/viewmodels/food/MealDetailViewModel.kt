package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MealDetailUiState(
    val meal: Meal? = null
)

class MealDetailViewModel(
    private val mealId: String,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MealDetailUiState(meal = foodCatalog.getMealById(mealId)))
    val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()
}
