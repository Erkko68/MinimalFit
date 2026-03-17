package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.LoggedMeal
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for managing daily food logs.
 */
interface JournalRepository {

    /** Returns the log for the given date as a stream. */
    fun getMealLog(date: LocalDate): Flow<MealLog?>

    /** Returns logs within a specific date range as a stream. */
    fun getMealLogs(start: LocalDate, end: LocalDate): Flow<List<MealLog>>

    suspend fun addMealToLog(date: LocalDate, loggedMeal: LoggedMeal)
    suspend fun removeMealFromLog(date: LocalDate, mealId: String)
}
