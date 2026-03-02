package eric.bitria.minimalfit.data.repository.meal

import eric.bitria.minimalfit.data.model.Meal
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to the user's personal saved meal library.
 */
interface MealRepository {

    /** Emits the full list of saved meals, updated reactively. */
    fun getMeals(): Flow<List<Meal>>


    /** Returns a single meal by [id], or null if not found. */
    suspend fun getMealById(id: String): Meal?

    /** Adds a new meal to the library. */
    suspend fun addMeal(meal: Meal)

    /** Replaces an existing meal that shares the same [Meal.id]. */
    suspend fun updateMeal(meal: Meal)

    /** Removes a meal by [id]. */
    suspend fun deleteMeal(id: String)
}

