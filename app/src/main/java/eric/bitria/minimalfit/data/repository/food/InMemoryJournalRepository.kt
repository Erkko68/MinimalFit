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
 */
class InMemoryJournalRepository : JournalRepository {

    private val _logs = MutableStateFlow<Map<LocalDate, DailyMealLog>>(emptyMap())

    override fun getLog(date: LocalDate): Flow<DailyMealLog> =
        _logs.map { logs -> logs[date] ?: DailyMealLog(date = date) }

    override fun getLogs(start: LocalDate, end: LocalDate): Flow<List<DailyMealLog>> {
        return _logs.map { logs ->
            logs.filterKeys { date -> !date.isBefore(start) && !date.isAfter(end) }
                .values
                .toList()
                .sortedByDescending { it.date }
        }
    }

    override fun getMeals(query: String): Flow<List<MealLog>> {
        return _logs.map { logs ->
            if (query.isBlank()) emptyList()
            else logs.values.flatMap { it.meals }
                .filter { it.meal.name.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun addMeal(date: LocalDate, meal: Meal) {
        val existing = _logs.value[date] ?: DailyMealLog(date = date)
        val entry = MealLog(meal = meal)
        _logs.value += (date to existing.copy(meals = existing.meals + entry))
    }

    override suspend fun updateMeal(date: LocalDate, mealLog: MealLog) {
        val existing = _logs.value[date] ?: return
        _logs.value += (date to existing.copy(
            meals = existing.meals.map { if (it.id == mealLog.id) mealLog else it }
        ))
    }

    override suspend fun deleteMeal(date: LocalDate, mealLog: MealLog) {
        val existing = _logs.value[date] ?: return
        _logs.value += (date to existing.copy(
            meals = existing.meals.filter { it.id != mealLog.id }
        ))
    }
}
