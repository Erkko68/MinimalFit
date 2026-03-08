package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class JournalRepository {

    // In-memory store: date → DailyLog
    private val _logs = MutableStateFlow<Map<LocalDate, DailyLog>>(emptyMap())
    val logs: StateFlow<Map<LocalDate, DailyLog>> = _logs.asStateFlow()

    // Catalogue of available meals to add — later could come from a food database
    val catalogue: List<Meal> = listOf(
        Meal(1, "Oatmeal with Berries", 350,
            imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop",
            description = "Healthy breakfast"),
        Meal(2, "Grilled Chicken Salad", 450,
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop",
            description = "Lunch"),
        Meal(3, "Protein Shake", 200,
            description = "Post-workout"),
        Meal(4, "Salmon and Asparagus", 600,
            imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop",
            description = "Dinner"),
        Meal(5, "Greek Yogurt", 150,
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500&auto=format&fit=crop",
            description = "Snack"),
        Meal(6, "Avocado Toast", 300,
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop",
            description = "Breakfast"),
        Meal(7, "Beef Stir Fry", 550,
            description = "Lunch")
    )

    /** Returns the last 7 days ending today, index 0 = oldest, index 6 = today. */
    fun last7Days(): List<LocalDate> {
        val today = LocalDate.now()
        return (6 downTo 0).map { today.minusDays(it.toLong()) }
    }

    /** Index within last7Days() that corresponds to today (always 6). */
    fun todayIndex(): Int = 6

    /** Human-readable 3-letter day name for a date. */
    fun dayLabel(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()

    fun getLog(date: LocalDate): DailyLog =
        _logs.value[date] ?: DailyLog(date = date)

    fun addMeal(date: LocalDate, meal: Meal) {
        val current = _logs.value.toMutableMap()
        val existing = current[date] ?: DailyLog(date = date)
        current[date] = existing.copy(meals = existing.meals + meal)
        _logs.value = current
    }
}

