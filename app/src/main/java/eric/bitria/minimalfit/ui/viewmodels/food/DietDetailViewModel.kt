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

data class DietMealUiState(
    val meal: Meal,
    val calories: Int,
    val amount: Float
)

data class DietDetailUiState(
    val diet: Diet? = null,
    val relatedMeals: List<DietMealUiState> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val showSearchDialog: Boolean = false,
    val searchMealQuery: String = "",
    val totalCalories: Int = 0
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
            
            combine(
                dietRepository.getMealsForDiet(dietId).flatMapLatest { meals ->
                    if (meals.isEmpty()) flowOf(emptyList<DietMealUiState>())
                    else combine(meals.map { meal ->
                        combine(
                            dietRepository.getMealAmountInDiet(dietId, meal.id),
                            foodCatalog.getMealCalories(meal.id)
                        ) { amount, baseCalories ->
                            DietMealUiState(meal, (baseCalories * amount).toInt(), amount)
                        }
                    }) { it.toList() }
                },
                dietRepository.getDietCalories(dietId),
                foodCatalog.getMeals(query)
            ) { mealStates, totalCalories, savedMeals ->
                DietDetailUiState(
                    diet = diet,
                    relatedMeals = mealStates,
                    savedMeals = savedMeals,
                    showSearchDialog = showDialog,
                    searchMealQuery = query,
                    totalCalories = totalCalories
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

    fun addMeal(mealId: String, amount: Float = 1f) {
        viewModelScope.launch {
            dietRepository.addMealToDiet(dietId, mealId, amount)
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
