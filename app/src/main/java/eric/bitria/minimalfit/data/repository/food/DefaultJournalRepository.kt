package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow

class DefaultJournalRepository(
    private val mealLogDao: MealLogDao
) : JournalRepository {

    override fun getMealLogsInRange(start: Long, end: Long): Flow<List<MealLog>> =
        mealLogDao.getMealLogsInRange(start, end)

    override suspend fun addMealLog(mealLog: MealLog) {
        mealLogDao.insertMealLog(mealLog)
    }

    override suspend fun removeMealLog(id: String) {
        mealLogDao.deleteMealLog(id)
    }
}
