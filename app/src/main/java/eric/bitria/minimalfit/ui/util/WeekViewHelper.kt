package eric.bitria.minimalfit.ui.util

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Helper utility for week-based view calculations.
 * Handles date ranges, labels, and index conversions for weekly displays.
 */
class WeekViewHelper {

    /** Returns the last 7 days ending today (index 0 = oldest, index 6 = today). */
    fun last7Days(): List<LocalDate> {
        val today = LocalDate.now()
        return (6 downTo 0).map { today.minusDays(it.toLong()) }
    }

    /** Human-readable short day label for the given date (e.g., "MON", "TUE"). */
    fun dayLabel(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
}

