package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.Meal

/**
 * Repository for accessing the food catalog (meal templates).
 * This will eventually be backed by a database or remote API.
 */
interface FoodCatalogRepository {

    /** Returns all available meals in the catalog. */
    fun getAllMeals(): List<Meal>

    /** Searches meals by name. */
    fun searchMeals(query: String): List<Meal>
}

