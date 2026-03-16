package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealDetailUiState(
    val meal: Meal? = null,
    val relatedMeals: List<Meal> = emptyList()
)

class MealDetailViewModel(
    private val mealId: String,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MealDetailUiState> = foodCatalog.getMeal(mealId)
        .flatMapLatest { meal ->
            if (meal == null) return@flatMapLatest flowOf(MealDetailUiState())
            
            val relatedIds = meal.relatedMealIds
            val relatedFlow = if (relatedIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(relatedIds.map { foodCatalog.getMeal(it) }) { mealsArray ->
                    mealsArray.filterNotNull()
                }
            }

            relatedFlow.map { related ->
                MealDetailUiState(meal = meal, relatedMeals = related)
            }
        }
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
