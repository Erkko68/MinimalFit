package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealLogDao
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

    override suspend fun addMealToLog(date: LocalDate, mealId: String) {
        val currentLog = mealLogDao.getMealLogForDate(date).firstOrNull()
        if (currentLog != null) {
            mealLogDao.updateMealLog(currentLog.copy(mealIds = currentLog.mealIds + mealId))
        } else {
            mealLogDao.insertMealLog(MealLog(date = date, mealIds = listOf(mealId)))
        }
    }

    override suspend fun removeMealFromLog(date: LocalDate, mealId: String) {
        val currentLog = mealLogDao.getMealLogForDate(date).firstOrNull() ?: return
        val updatedIds = currentLog.mealIds.toMutableList().apply { remove(mealId) }
        mealLogDao.updateMealLog(currentLog.copy(mealIds = updatedIds))
    }
}
