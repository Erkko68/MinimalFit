package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.entity.food.relations.MealLogMealCrossRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class DefaultJournalRepository(
    private val mealLogDao: MealLogDao,
    private val mealDao: MealDao,
    private val foodCatalog: FoodCatalogRepository
) : JournalRepository {

    override fun getMealLogsInRange(start: Instant, end: Instant): Flow<List<MealLog>> =
        mealLogDao.getMealLogsInRange(start, end)

    override suspend fun addMealLog(mealLog: MealLog) {
        mealLogDao.insertMealLog(mealLog)
    }

    override suspend fun removeMealLog(id: String) {
        mealLogDao.deleteMealLog(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMealsForLog(mealLogId: String): Flow<List<Meal>> =
        mealLogDao.getMealsForMealLog(mealLogId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(emptyList())
            else combine(refs.map { ref ->
                mealDao.getMeal(ref.mealId).map { it }
            }) { it.filterNotNull().toList() }
        }

    override fun getMealAmountInLog(mealLogId: String, mealId: String): Flow<Float> =
        mealLogDao.getMealsForMealLog(mealLogId).map { refs ->
            refs.find { it.mealId == mealId }?.amount ?: 0f
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMealCaloriesInLog(mealLogId: String, mealId: String): Flow<Int> =
        combine(
            foodCatalog.getMealCalories(mealId),
            foodCatalog.getMealWeight(mealId),
            getMealAmountInLog(mealLogId, mealId)
        ) { baseCalories, baseWeight, loggedAmount ->
            val scale = if (baseWeight > 0) loggedAmount / baseWeight else 1f
            (baseCalories * scale).toInt()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLogCalories(mealLogId: String): Flow<Int> =
        mealLogDao.getMealsForMealLog(mealLogId).flatMapLatest { refs ->
            if (refs.isEmpty()) flowOf(0)
            else combine(refs.map { ref ->
                getMealCaloriesInLog(mealLogId, ref.mealId)
            }) { it.sum() }
        }

    override suspend fun addMealToLog(mealLogId: String, mealId: String, amount: Float) {
        mealLogDao.insertMealLogMealCrossRef(MealLogMealCrossRef(mealLogId, mealId, amount))
    }
}
