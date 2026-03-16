package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.food.DailyMealLog
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.data.model.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for managing daily food logs.
 */
interface JournalRepository {

    /** Returns the log for the given date as a stream. */
    fun getLog(date: LocalDate): Flow<DailyMealLog>

    /** Returns logs within a specific date range as a stream. */
    fun getLogs(start: LocalDate, end: LocalDate): Flow<List<DailyMealLog>>

    /** Searches for specific meals across history. */
    fun getMeals(query: String): Flow<List<MealLog>>

    suspend fun addMeal(date: LocalDate, meal: Meal)
    suspend fun updateMeal(date: LocalDate, mealLog: MealLog)
    suspend fun deleteMeal(date: LocalDate, mealLog: MealLog)
}
