package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.entity.food.LoggedMeal
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

class DefaultJournalRepository(
    private val mealLogDao: MealLogDao
) : JournalRepository {

    override fun getMealLog(date: LocalDate): Flow<MealLog?> =
        mealLogDao.getMealLogForDate(date)

    override fun getMealLogs(start: LocalDate, end: LocalDate): Flow<List<MealLog>> =
        mealLogDao.getMealLogsInRange(start, end)

    override suspend fun addMealToLog(date: LocalDate, loggedMeal: LoggedMeal) {
        val currentLog = mealLogDao.getMealLogForDate(date).firstOrNull()
        if (currentLog != null) {
            mealLogDao.updateMealLog(currentLog.copy(loggedMeals = currentLog.loggedMeals + loggedMeal))
        } else {
            mealLogDao.insertMealLog(MealLog(date = date, loggedMeals = listOf(loggedMeal)))
        }
    }

    override suspend fun removeMealFromLog(date: LocalDate, mealId: String) {
        val currentLog = mealLogDao.getMealLogForDate(date).firstOrNull() ?: return
        val updatedMeals = currentLog.loggedMeals.filterNot { it.mealId == mealId }
        mealLogDao.updateMealLog(currentLog.copy(loggedMeals = updatedMeals))
    }
}
