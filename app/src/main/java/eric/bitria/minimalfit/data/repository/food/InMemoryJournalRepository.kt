package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.food.DailyMealLog
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.data.model.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * In-memory implementation of the journal repository.
 * Stores daily logs in a map keyed by date.
 * Uses Flow for reactive updates across ViewModels.
 */
class InMemoryJournalRepository : JournalRepository {

    private val _logsFlow = MutableStateFlow<Map<LocalDate, DailyMealLog>>(emptyMap())

    override fun getAllLogsFlow(): Flow<Map<LocalDate, DailyMealLog>> = _logsFlow

    override fun getLogFlow(date: LocalDate): Flow<DailyMealLog> =
        _logsFlow.map { logs -> logs[date] ?: DailyMealLog(date = date) }

    override fun getLog(date: LocalDate): DailyMealLog =
        _logsFlow.value[date] ?: DailyMealLog(date = date)

    override fun addMeal(date: LocalDate, meal: Meal) {
        val existing = _logsFlow.value[date] ?: DailyMealLog(date = date)
        val entry = MealLog(meal = meal)
        _logsFlow.value += (date to existing.copy(meals = existing.meals + entry))
    }

    override fun removeMeal(date: LocalDate, mealLog: MealLog) {
        val existing = _logsFlow.value[date] ?: return
        _logsFlow.value += (date to existing.copy(
            meals = existing.meals.filter { it.id != mealLog.id }
        ))
    }

    override fun updateMeal(date: LocalDate, mealLog: MealLog) {
        val existing = _logsFlow.value[date] ?: return
        _logsFlow.value += (date to existing.copy(
            meals = existing.meals.map { if (it.id == mealLog.id) mealLog else it }
        ))
    }
}
