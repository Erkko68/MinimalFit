package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class DefaultJournalRepository(
    private val mealLogDao: MealLogDao
) : JournalRepository {

    override fun getMealLogs(date: LocalDate): Flow<List<MealLog>> =
        mealLogDao.getMealLogsForDate(date)

    override fun getMealLogs(start: LocalDate, end: LocalDate): Flow<List<MealLog>> {
        return mealLogDao.getMealLogsInRange(start, end)
    }

    override fun searchMealLogs(query: String): Flow<List<MealLog>> =
        mealLogDao.searchMealLogs(query)

    override suspend fun addMealLog(date: LocalDate, meal: Meal) {
        val log = MealLog(
            date = date,
            mealId = meal.id,
            mealName = meal.name,
            calories = meal.calories
        )
        mealLogDao.insertMealLog(log)
    }

    override suspend fun updateMealLog(mealLog: MealLog) {
        mealLogDao.insertMealLog(mealLog)
    }

    override suspend fun deleteMealLog(id: String) {
        mealLogDao.deleteMealLog(id)
    }
}
