package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface JournalRepository {

    /** Observable map of all stored daily logs. */
    val logs: StateFlow<Map<LocalDate, DailyLog>>

    /** Returns the last 7 days ending today (index 0 = oldest, index 6 = today). */
    fun last7Days(): List<LocalDate>

    /** Index within [last7Days] that corresponds to today (always 6). */
    fun todayIndex(): Int

    /** Human-readable short day label for the given date. */
    fun dayLabel(date: LocalDate): String

    /** Returns the log for the given date, or a default empty one. */
    fun getLog(date: LocalDate): DailyLog

    /** Appends a meal to the given date's log. */
    fun addMeal(date: LocalDate, meal: Meal)
}
