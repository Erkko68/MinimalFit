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

    /** Returns a specific meal by ID. */
    fun getMeal(id: String): Flow<Meal?>

    suspend fun addMeal(meal: Meal)
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(id: String)

    // --- MEAL RELATIONS ---
    /** Returns the list of ingredients belonging to a meal. */
    fun getIngredientsForMeal(mealId: String): Flow<List<Ingredient>>

    /** Returns the total calories of a meal. */
    fun getMealCalories(mealId: String): Flow<Int>

    /** Returns the total weight/amount of a meal (sum of its ingredients). */
    fun getMealWeight(mealId: String): Flow<Float>
    
    /** Returns the weight/amount of a specific ingredient within a meal. */
    fun getIngredientAmountInMeal(mealId: String, ingredientId: String): Flow<Float>

    suspend fun addIngredientToMeal(mealId: String, ingredientId: String, amount: Float)
    suspend fun removeIngredientsFromMeal(mealId: String)
    suspend fun removeIngredientFromMeal(mealId: String, ingredientId: String)

    // --- INGREDIENTS ---
    /** Returns all available ingredients. Supports optional search. */
    fun getIngredients(query: String = ""): Flow<List<Ingredient>>

    /** Returns a specific ingredient by ID. */
    fun getIngredient(id: String): Flow<Ingredient?>

    suspend fun addIngredient(ingredient: Ingredient)
}
