package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.DietDao
import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.relations.DietMealCrossRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DefaultDietRepository(
    private val dietDao: DietDao,
    private val mealDao: MealDao,
    private val foodCatalog: FoodCatalogRepository
) : DietRepository {

    override fun getDiet(id: String): Flow<Diet?> =
        dietDao.getDiet(id)

    override fun getDiets(query: String): Flow<List<Diet>> =
        if (query.isBlank()) dietDao.getAllDiets()
        else dietDao.searchDiets(query)

    override suspend fun addDiet(diet: Diet) =
        dietDao.insertDiet(diet)

    override suspend fun updateDiet(diet: Diet) =
        dietDao.updateDiet(diet)

    override suspend fun deleteDiet(id: String) =
        dietDao.deleteDiet(id)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMealsForDiet(dietId: String): Flow<List<Meal>> =
        dietDao.getMealsForDiet(dietId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(emptyList())
            else combine(refs.map { ref ->
                mealDao.getMeal(ref.mealId).map { it }
            }) { it.filterNotNull().toList() }
        }

    override fun getMealAmountInDiet(dietId: String, mealId: String): Flow<Float> =
        dietDao.getMealsForDiet(dietId).map { refs ->
            refs.find { it.mealId == mealId }?.amount ?: 0f
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDietCalories(dietId: String): Flow<Int> =
        dietDao.getMealsForDiet(dietId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(0)
            else combine(refs.map { ref ->
                combine(
                    foodCatalog.getMealCalories(ref.mealId),
                    foodCatalog.getMealWeight(ref.mealId)
                ) { baseCalories, baseWeight ->
                    val scale = if (baseWeight > 0f) ref.amount / baseWeight else 1f
                    (baseCalories * scale).toInt()
                }
            }) { it.sum() }
        }

    override suspend fun addMealToDiet(dietId: String, mealId: String, amount: Float) {
        dietDao.insertDietMealCrossRef(DietMealCrossRef(dietId, mealId, amount))
    }

    override suspend fun removeMealFromDiet(dietId: String, mealId: String) {
        dietDao.deleteMealFromDiet(dietId, mealId)
    }
}
