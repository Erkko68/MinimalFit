package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.model.food.Meal

/**
 * In-memory implementation of the food catalog repository.
 * Wraps the FoodDatabase datasource.
 */
class InMemoryFoodCatalogRepository(
    private val foodDatabase: FoodDatabase
) : FoodCatalogRepository {

    override fun getAllMeals(): List<Meal> = foodDatabase.meals

    override fun getMealById(id: String): Meal? = foodDatabase.meals.find { it.id == id }

    override fun searchMeals(query: String): List<Meal> = foodDatabase.search(query)
}
