package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.entity.food.Meal
import kotlinx.coroutines.flow.Flow

/**
 * Room implementation of the food catalog repository.
 */
class DefaultFoodCatalogRepository(
    private val mealDao: MealDao
) : FoodCatalogRepository {

    override fun getMeals(query: String): Flow<List<Meal>> =
        mealDao.searchMeals(query)

    override fun getMeal(id: String): Flow<Meal?> =
        mealDao.getMeal(id)

    override suspend fun addMeal(meal: Meal) =
        mealDao.insertMeal(meal)

    override suspend fun updateMeal(meal: Meal) =
        mealDao.updateMeal(meal)

    override suspend fun deleteMeal(id: String) =
        mealDao.deleteMeal(id)
}
