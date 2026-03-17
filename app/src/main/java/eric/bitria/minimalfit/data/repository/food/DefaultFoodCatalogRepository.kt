package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.IngredientDao
import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.data.entity.food.relations.MealIngredientCrossRef
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getIngredientsForMeal(mealId: String): Flow<List<Ingredient>> =
        mealDao.getIngredientsForMeal(mealId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(emptyList())
            else combine(refs.map { ref -> 
                ingredientDao.getIngredient(ref.ingredientId).map { it } 
            }) { it.filterNotNull().toList() }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMealCalories(mealId: String): Flow<Int> =
        mealDao.getIngredientsForMeal(mealId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(0)
            else combine(refs.map { ref ->
                ingredientDao.getIngredient(ref.ingredientId).map { ingredient ->
                    if (ingredient == null) 0f
                    else calculateIngredientCalories(ingredient, ref.amount)
                }
            }) { it.sum().toInt() }
        }

    override fun getMealWeight(mealId: String): Flow<Float> =
        mealDao.getIngredientsForMeal(mealId).map { refs ->
            refs.sumOf { it.amount.toDouble() }.toFloat()
        }

    override fun getIngredientAmountInMeal(mealId: String, ingredientId: String): Flow<Float> =
        mealDao.getIngredientsForMeal(mealId).map { refs ->
            refs.find { it.ingredientId == ingredientId }?.amount ?: 0f
        }

    private fun calculateIngredientCalories(ingredient: Ingredient, amount: Float): Float {
        return when (ingredient.measurementUnit) {
            MeasurementUnit.PIECE -> ingredient.baseCalories * amount
            MeasurementUnit.GRAMS, MeasurementUnit.MILLILITERS -> (ingredient.baseCalories / 100f) * amount
        }
    }

    override suspend fun addIngredientToMeal(mealId: String, ingredientId: String, amount: Float) {
        mealDao.insertMealIngredientCrossRef(MealIngredientCrossRef(mealId, ingredientId, amount))
    }

    override suspend fun removeIngredientsFromMeal(mealId: String) {
        mealDao.deleteIngredientsForMeal(mealId)
    }

    override fun getIngredients(query: String): Flow<List<Ingredient>> =
        ingredientDao.searchIngredients(query)

    override fun getIngredient(id: String): Flow<Ingredient?> =
        ingredientDao.getIngredient(id)

    override suspend fun addIngredient(ingredient: Ingredient) =
        ingredientDao.insertIngredient(ingredient)
}
