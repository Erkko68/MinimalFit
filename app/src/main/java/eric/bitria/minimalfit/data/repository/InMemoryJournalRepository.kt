package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import java.time.LocalDate

/**
 * In-memory implementation of the journal repository.
 * Stores daily logs in a map keyed by date.
 */
class InMemoryJournalRepository : JournalRepository {

    private val logs = mutableMapOf<LocalDate, DailyLog>()

    override fun getLog(date: LocalDate): DailyLog =
        logs[date] ?: DailyLog(date = date)

    override fun addMeal(date: LocalDate, meal: Meal) {
        val existing = logs[date] ?: DailyLog(date = date)
        logs[date] = existing.copy(meals = existing.meals + meal)
    }

    override fun removeMeal(date: LocalDate, mealId: Int) {
        val existing = logs[date] ?: return
        logs[date] = existing.copy(meals = existing.meals.filter { it.id != mealId })
    }

    override fun updateMeal(date: LocalDate, meal: Meal) {
        val existing = logs[date] ?: return
        logs[date] = existing.copy(
            meals = existing.meals.map { if (it.id == meal.id) meal else it }
        )
    }

    override fun updateCalorieGoal(date: LocalDate, goal: Int) {
        val existing = logs[date] ?: DailyLog(date = date)
        logs[date] = existing.copy(calorieGoal = goal)
    }
}

