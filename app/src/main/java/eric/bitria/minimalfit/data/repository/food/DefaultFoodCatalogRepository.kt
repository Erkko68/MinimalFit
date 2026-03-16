package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.FoodDatabase
import eric.bitria.minimalfit.data.entity.food.Meal
import kotlinx.coroutines.flow.Flow

/**
 * In-memory implementation of the food catalog repository.
 */
class DefaultFoodCatalogRepository(
    private val foodDatabase: FoodDatabase
) : FoodCatalogRepository {

    override fun getMeals(query: String): Flow<List<Meal>> =
        foodDatabase.getMeals(query)

    override fun getMeal(id: String): Flow<Meal?> =
        foodDatabase.getMeal(id)

    override suspend fun addMeal(meal: Meal) =
        foodDatabase.addMeal(meal)

    override suspend fun updateMeal(meal: Meal) =
        foodDatabase.updateMeal(meal)

    override suspend fun deleteMeal(id: String) =
        foodDatabase.deleteMeal(id)
}
