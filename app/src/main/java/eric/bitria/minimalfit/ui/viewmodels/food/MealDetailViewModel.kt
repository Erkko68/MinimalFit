package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Ingredient
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

data class MealIngredientUiState(
    val ingredient: Ingredient,
    val amount: Float
)

data class MealDetailUiState(
    val meal: Meal? = null,
    val ingredients: List<MealIngredientUiState> = emptyList(),
    val savedIngredients: List<Ingredient> = emptyList(),
    val showSearchDialog: Boolean = false,
    val searchIngredientQuery: String = "",
    val totalCalories: Int = 0,
    val totalAmount: Float = 0f
)

class MealDetailViewModel(
    private val mealId: String,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    private val _showSearchDialog = MutableStateFlow(false)
    private val _searchIngredientQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MealDetailUiState> = combine(
        _showSearchDialog,
        _searchIngredientQuery
    ) { showDialog, query ->
        showDialog to query
    }.flatMapLatest { (showDialog, query) ->
        foodCatalog.getMeal(mealId).flatMapLatest { meal ->
            if (meal == null) return@flatMapLatest flowOf(MealDetailUiState())

            combine(
                foodCatalog.getIngredientsForMeal(mealId).flatMapLatest { ingredients ->
                    if (ingredients.isEmpty()) flowOf(emptyList<MealIngredientUiState>())
                    else combine(ingredients.map { ing ->
                        foodCatalog.getIngredientAmountInMeal(mealId, ing.id).map { amt ->
                            MealIngredientUiState(ing, amt)
                        }
                    }) { it.toList() }
                },
                foodCatalog.getMealCalories(mealId),
                foodCatalog.getMealWeight(mealId),
                foodCatalog.getIngredients(query)
            ) { ingredientStates, calories, weight, savedIngredients ->
                MealDetailUiState(
                    meal = meal,
                    ingredients = ingredientStates,
                    savedIngredients = savedIngredients,
                    showSearchDialog = showDialog,
                    searchIngredientQuery = query,
                    totalCalories = calories,
                    totalAmount = weight
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

    fun onSearchIngredientQueryChange(query: String) {
        _searchIngredientQuery.value = query
    }

    fun addIngredient(ingredient: Ingredient, amount: Float) {
        viewModelScope.launch {
            foodCatalog.addIngredientToMeal(mealId, ingredient.id, amount)
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
