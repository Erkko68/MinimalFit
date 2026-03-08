package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import java.time.LocalDate

/**
 * Repository for managing daily food logs.
 * Handles CRUD operations for meals and calorie goals.
 */
interface JournalRepository {

    /** Returns the log for the given date, or a default empty one. */
    fun getLog(date: LocalDate): DailyLog

    /** Appends a meal to the given date's log. */
    fun addMeal(date: LocalDate, meal: Meal)

    /** Removes a meal from the given date's log by meal ID. */
    fun removeMeal(date: LocalDate, mealId: Int)

    /** Updates a meal in the given date's log. */
    fun updateMeal(date: LocalDate, meal: Meal)

    /** Updates the calorie goal for the given date. */
    fun updateCalorieGoal(date: LocalDate, goal: Int)
}
