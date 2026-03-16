package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.DietRepository
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

data class DietDetailUiState(
    val diet: Diet? = null,
    val relatedMeals: List<Meal> = emptyList()
)

class DietDetailViewModel(
    private val dietId: String,
    private val dietRepository: DietRepository,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DietDetailUiState> = dietRepository.getDiet(dietId)
        .flatMapLatest { diet ->
            if (diet == null) return@flatMapLatest flowOf(DietDetailUiState())
            
            val mealIds = diet.relatedMealIds
            val mealsFlow = if (mealIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(mealIds.map { foodCatalog.getMeal(it) }) { mealsArray ->
                    mealsArray.filterNotNull()
                }
            }

            mealsFlow.map { meals ->
                DietDetailUiState(diet = diet, relatedMeals = meals)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DietDetailUiState()
        )

    fun updateDiet(diet: Diet) {
        viewModelScope.launch {
            dietRepository.updateDiet(diet)
        }
    }

    fun deleteDiet() {
        viewModelScope.launch {
            dietRepository.deleteDiet(dietId)
        }
    }
}
