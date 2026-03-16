package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.food.Meal
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing the food catalog (meal templates).
 */
interface FoodCatalogRepository {

    /** Returns all available meals. Supports optional search. */
    fun getMeals(query: String = ""): Flow<List<Meal>>

    /** Returns a specific meal by ID. */
    fun getMeal(id: String): Flow<Meal?>

    suspend fun addMeal(meal: Meal)
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(id: String)
}
