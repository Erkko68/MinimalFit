package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.MealLog
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

    private val _logsFlow = MutableStateFlow<Map<LocalDate, DailyLog>>(emptyMap())

    override fun getAllLogsFlow(): Flow<Map<LocalDate, DailyLog>> = _logsFlow

    override fun getLogFlow(date: LocalDate): Flow<DailyLog> =
        _logsFlow.map { logs -> logs[date] ?: DailyLog(date = date) }

    override fun getLog(date: LocalDate): DailyLog =
        _logsFlow.value[date] ?: DailyLog(date = date)

    override fun addMeal(date: LocalDate, meal: Meal) {
        val existing = _logsFlow.value[date] ?: DailyLog(date = date)
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

    override fun updateCalorieGoal(date: LocalDate, goal: Int) {
        val existing = _logsFlow.value[date] ?: DailyLog(date = date)
        _logsFlow.value += (date to existing.copy(calorieGoal = goal))
    }
}
