package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing daily food logs.
 */
interface JournalRepository {

    /** Returns the logs within a specific time range. */
    fun getMealLogsInRange(start: Long, end: Long): Flow<List<MealLog>>

    suspend fun addMealLog(mealLog: MealLog)
    suspend fun removeMealLog(id: String)
}
