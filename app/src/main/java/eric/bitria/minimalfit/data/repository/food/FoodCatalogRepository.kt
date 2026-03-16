package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.food.Meal

/**
 * Repository for accessing the food catalog (meal templates).
 * This will eventually be backed by a database or remote API.
 */
interface FoodCatalogRepository {

    /** Returns all available meals in the catalog. */
    fun getAllMeals(): List<Meal>

    /** Returns a meal by id, or null if it does not exist. */
    fun getMealById(id: String): Meal?

    /** Searches meals by name. */
    fun searchMeals(query: String): List<Meal>
}
