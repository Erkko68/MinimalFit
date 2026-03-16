package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    val relatedMeals: List<Meal> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val showSearchDialog: Boolean = false,
    val searchMealQuery: String = ""
)

class MealDetailViewModel(
    private val mealId: String,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    private val _showSearchDialog = MutableStateFlow(false)
    private val _searchMealQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MealDetailUiState> = combine(
        _showSearchDialog,
        _searchMealQuery
    ) { showDialog, query ->
        showDialog to query
    }.flatMapLatest { (showDialog, query) ->
        foodCatalog.getMeal(mealId).flatMapLatest { meal ->
            if (meal == null) return@flatMapLatest flowOf(MealDetailUiState())

            val relatedIds = meal.relatedMealIds
            val relatedFlow = if (relatedIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(relatedIds.map { foodCatalog.getMeal(it) }) { mealsArray ->
                    mealsArray.filterNotNull()
                }
            }

            combine(
                relatedFlow,
                foodCatalog.getMeals(query)
            ) { related, savedMeals ->
                MealDetailUiState(
                    meal = meal,
                    relatedMeals = related,
                    savedMeals = savedMeals,
                    showSearchDialog = showDialog,
                    searchMealQuery = query
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MealDetailUiState()
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
            val currentMeal = uiState.value.meal ?: return@launch
            if (!currentMeal.relatedMealIds.contains(mealToAdd.id)) {
                val updatedMeal = currentMeal.copy(
                    relatedMealIds = currentMeal.relatedMealIds + mealToAdd.id
                )
                foodCatalog.updateMeal(updatedMeal)
            }
            dismissSearchDialog()
        }
    }

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
