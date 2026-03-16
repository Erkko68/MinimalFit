package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for managing daily food logs.
 */
interface JournalRepository {

    /** Returns the log for the given date as a stream. */
    fun getMealLogs(date: LocalDate): Flow<List<MealLog>>

    /** Returns logs within a specific date range as a stream. */
    fun getMealLogs(start: LocalDate, end: LocalDate): Flow<List<MealLog>>

    /** Searches for specific meals across history. */
    fun searchMealLogs(query: String): Flow<List<MealLog>>

    suspend fun addMealLog(date: LocalDate, meal: Meal)
    suspend fun updateMealLog(mealLog: MealLog)
    suspend fun deleteMealLog(id: String)
}
