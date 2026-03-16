package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DietDetailUiState(
    val diet: Diet? = null,
    val relatedMeals: List<Meal> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val showSearchDialog: Boolean = false,
    val searchMealQuery: String = ""
)

class DietDetailViewModel(
    private val dietId: String,
    private val dietRepository: DietRepository,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    private val _showSearchDialog = MutableStateFlow(false)
    private val _searchMealQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DietDetailUiState> = combine(
        _showSearchDialog,
        _searchMealQuery
    ) { showDialog, query ->
        showDialog to query
    }.flatMapLatest { (showDialog, query) ->
        dietRepository.getDiet(dietId).flatMapLatest { diet ->
            if (diet == null) return@flatMapLatest flowOf(DietDetailUiState())
            
            val mealIds = diet.relatedMealIds
            val mealsFlow = if (mealIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(mealIds.map { foodCatalog.getMeal(it) }) { mealsArray ->
                    mealsArray.filterNotNull()
                }
            }

            combine(
                mealsFlow,
                foodCatalog.getMeals(query)
            ) { meals, savedMeals ->
                DietDetailUiState(
                    diet = diet,
                    relatedMeals = meals,
                    savedMeals = savedMeals,
                    showSearchDialog = showDialog,
                    searchMealQuery = query
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DietDetailUiState()
    )

    fun openSearchDialog() {
        _showSearchDialog.value = true
    }

    fun dismissSearchDialog() {
        _showSearchDialog.value = false
    }

    fun onSearchMealQueryChange(query: String) {
        _searchMealQuery.value = query
    }

    fun addMeal(mealToAdd: Meal) {
        viewModelScope.launch {
            val currentDiet = uiState.value.diet ?: return@launch
            if (!currentDiet.relatedMealIds.contains(mealToAdd.id)) {
                val updatedDiet = currentDiet.copy(
                    relatedMealIds = currentDiet.relatedMealIds + mealToAdd.id
                )
                dietRepository.updateDiet(updatedDiet)
            }
            dismissSearchDialog()
        }
    }

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
