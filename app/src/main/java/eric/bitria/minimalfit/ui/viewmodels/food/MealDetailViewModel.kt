package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
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
                foodCatalog.getIngredients(query)
            ) { ingredientStates, savedIngredients ->
                val totalWeight = ingredientStates.sumOf { it.amount.toDouble() }.toFloat()
                val totalCalories = ingredientStates.sumOf { state ->
                    calculateIngredientCalories(state.ingredient, state.amount).toDouble()
                }.toInt()

                MealDetailUiState(
                    meal = meal,
                    ingredients = ingredientStates,
                    savedIngredients = savedIngredients,
                    showSearchDialog = showDialog,
                    searchIngredientQuery = query,
                    totalCalories = totalCalories,
                    totalAmount = totalWeight
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

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            foodCatalog.removeIngredientFromMeal(mealId, ingredient.id)
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

    private fun calculateIngredientCalories(ingredient: Ingredient, amount: Float): Float {
        return when (ingredient.measurementUnit) {
            MeasurementUnit.PIECE -> ingredient.baseCalories * amount
            MeasurementUnit.GRAMS,
            MeasurementUnit.MILLILITERS -> (ingredient.baseCalories / 100f) * amount
        }
    }
}
