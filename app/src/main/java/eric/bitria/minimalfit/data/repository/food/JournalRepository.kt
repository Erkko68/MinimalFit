package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.MealLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for managing daily food logs.
 * Handles CRUD operations for meals and calorie goals.
 */
interface JournalRepository {

    /** Returns all logs as a Flow that emits on changes. */
    fun getAllLogsFlow(): Flow<Map<LocalDate, DailyLog>>

    /** Returns the log for the given date as a Flow that emits on changes. */
    fun getLogFlow(date: LocalDate): Flow<DailyLog>

    /** Returns the log for the given date (snapshot), or a default empty one. */
    fun getLog(date: LocalDate): DailyLog

    /** Wraps the meal in a [MealLog] with a unique ID and appends it to the date's log. */
    fun addMeal(date: LocalDate, meal: Meal)

    /** Removes a specific log entry from the given date's log. */
    fun removeMeal(date: LocalDate, mealLog: MealLog)

    /** Updates a specific log entry in the given date's log, matched by [MealLog.id]. */
    fun updateMeal(date: LocalDate, mealLog: MealLog)
}
