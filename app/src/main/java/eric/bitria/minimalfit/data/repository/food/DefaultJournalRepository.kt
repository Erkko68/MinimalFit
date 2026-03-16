package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.datasource.JournalDatabase
import eric.bitria.minimalfit.data.model.food.DailyMealLog
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.data.model.food.MealLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * In-memory implementation of the journal repository.
 */
class DefaultJournalRepository(
    private val database: JournalDatabase
) : JournalRepository {

    override fun getLog(date: LocalDate): Flow<DailyMealLog> =
        database.getLog(date)

    override fun getLogs(start: LocalDate, end: LocalDate): Flow<List<DailyMealLog>> =
        database.getLogs(start, end)

    override fun getMeals(query: String): Flow<List<MealLog>> =
        database.getMeals(query)

    override suspend fun addMeal(date: LocalDate, meal: Meal) =
        database.addMeal(date, meal)

    override suspend fun updateMeal(date: LocalDate, mealLog: MealLog) =
        database.updateMeal(date, mealLog)

    override suspend fun deleteMeal(date: LocalDate, mealLog: MealLog) =
        database.deleteMeal(date, mealLog)
}
