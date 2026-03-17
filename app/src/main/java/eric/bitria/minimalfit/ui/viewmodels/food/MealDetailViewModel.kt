package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.IngredientReference
import eric.bitria.minimalfit.data.entity.food.Meal
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

data class MealIngredientUiState(
    val ingredient: Ingredient,
    val amount: Float
)

data class MealDetailUiState(
    val meal: Meal? = null,
    val ingredients: List<MealIngredientUiState> = emptyList(),
    val savedIngredients: List<Ingredient> = emptyList(),
    val showSearchDialog: Boolean = false,
    val searchIngredientQuery: String = ""
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

            val ingredientRefs = meal.ingredients
            val ingredientsFlow = if (ingredientRefs.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ingredientRefs.map { ref ->
                    foodCatalog.getIngredient(ref.ingredientId).flatMapLatest { ingredient ->
                        if (ingredient == null) flowOf(null)
                        else flowOf(MealIngredientUiState(ingredient, ref.amount))
                    }
                }) { ingredientsArray ->
                    ingredientsArray.filterNotNull()
                }
            }

            combine(
                ingredientsFlow,
                foodCatalog.getIngredients(query)
            ) { ingredients, savedIngredients ->
                MealDetailUiState(
                    meal = meal,
                    ingredients = ingredients,
                    savedIngredients = savedIngredients,
                    showSearchDialog = showDialog,
                    searchIngredientQuery = query
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
            val currentMeal = uiState.value.meal ?: return@launch
            val updatedIngredients = currentMeal.ingredients + IngredientReference(ingredient.id, amount)
            val updatedMeal = currentMeal.copy(ingredients = updatedIngredients)
            foodCatalog.updateMeal(updatedMeal)
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
