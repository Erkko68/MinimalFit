package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing the food catalog (ingredients and meal templates).
 */
interface FoodCatalogRepository {

    // --- MEALS ---
    /** Returns all available meals. Supports optional search. */
    fun getMeals(query: String = ""): Flow<List<Meal>>

    /** Returns a specific meal by ID, resolving its total calories from ingredients. */
    fun getMeal(id: String): Flow<Meal?>

    suspend fun addMeal(meal: Meal)
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(id: String)

    // --- INGREDIENTS ---
    /** Returns all available ingredients. Supports optional search. */
    fun getIngredients(query: String = ""): Flow<List<Ingredient>>

    /** Returns a specific ingredient by ID. */
    fun getIngredient(id: String): Flow<Ingredient?>

    suspend fun addIngredient(ingredient: Ingredient)
}
