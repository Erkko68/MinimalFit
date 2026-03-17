package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.IngredientDao
import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Room implementation of the food catalog repository.
 */
class DefaultFoodCatalogRepository(
    private val mealDao: MealDao,
    private val ingredientDao: IngredientDao
) : FoodCatalogRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMeals(query: String): Flow<List<Meal>> =
        mealDao.searchMeals(query).flatMapLatest { meals ->
            if (meals.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(meals.map { resolveMeal(it) }) { resolved ->
                resolved.toList()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMeal(id: String): Flow<Meal?> =
        mealDao.getMeal(id).flatMapLatest { meal ->
            if (meal == null) return@flatMapLatest flowOf(null)
            resolveMeal(meal)
        }

    /**
     * Resolves a meal to calculate its total calories and total amount by fetching its ingredients.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun resolveMeal(meal: Meal): Flow<Meal> {
        val ingredientRefs = meal.ingredients
        if (ingredientRefs.isEmpty()) return flowOf(meal.copy(totalCalories = 0, totalAmount = 0f))

        // Fetch all referenced ingredients
        val ingredientDataFlows = ingredientRefs.map { ref ->
            ingredientDao.getIngredient(ref.ingredientId).map { ingredient ->
                if (ingredient == null) 0f to 0f
                else {
                    val calories = calculateIngredientCalories(ingredient, ref.amount)
                    calories to ref.amount
                }
            }
        }

        return combine(ingredientDataFlows) { dataArray ->
            val totalCalories = dataArray.sumOf { it.first.toDouble() }.toInt()
            val totalAmount = dataArray.sumOf { it.second.toDouble() }.toFloat()
            meal.copy(totalCalories = totalCalories, totalAmount = totalAmount)
        }
    }

    private fun calculateIngredientCalories(ingredient: Ingredient, amount: Float): Float {
        return when (ingredient.measurementUnit) {
            MeasurementUnit.PIECE -> ingredient.baseCalories * amount
            MeasurementUnit.GRAMS, MeasurementUnit.MILLILITERS -> (ingredient.baseCalories / 100f) * amount
        }
    }

    override suspend fun addMeal(meal: Meal) =
        mealDao.insertMeal(meal)

    override suspend fun updateMeal(meal: Meal) =
        mealDao.updateMeal(meal)

    override suspend fun deleteMeal(id: String) =
        mealDao.deleteMeal(id)

    // New methods for Ingredients
    override fun getIngredients(query: String): Flow<List<Ingredient>> =
        ingredientDao.searchIngredients(query)

    override fun getIngredient(id: String): Flow<Ingredient?> =
        ingredientDao.getIngredient(id)

    override suspend fun addIngredient(ingredient: Ingredient) =
        ingredientDao.insertIngredient(ingredient)
}
