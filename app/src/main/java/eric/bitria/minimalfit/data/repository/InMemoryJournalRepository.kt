package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class InMemoryJournalRepository : JournalRepository {

    private val _logs = MutableStateFlow<Map<LocalDate, DailyLog>>(emptyMap())
    override val logs: StateFlow<Map<LocalDate, DailyLog>> = _logs.asStateFlow()

    override fun last7Days(): List<LocalDate> {
        val today = LocalDate.now()
        return (6 downTo 0).map { today.minusDays(it.toLong()) }
    }

    override fun todayIndex(): Int = 6

    override fun dayLabel(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()

    override fun getLog(date: LocalDate): DailyLog =
        _logs.value[date] ?: DailyLog(date = date)

    override fun addMeal(date: LocalDate, meal: Meal) {
        val current = _logs.value.toMutableMap()
        val existing = current[date] ?: DailyLog(date = date)
        current[date] = existing.copy(meals = existing.meals + meal)
        _logs.value = current
    }
}

