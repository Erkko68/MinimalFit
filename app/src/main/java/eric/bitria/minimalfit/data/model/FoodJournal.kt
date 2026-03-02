package eric.bitria.minimalfit.data.model

import java.time.LocalDate

/**
 * A complete food journal for a single date, aggregating all logged entries.
 */
data class FoodJournal(
    val date: LocalDate,
    val entries: List<MealEntry> = emptyList()
) {
    val totalCalories: Int get() = entries.sumOf { it.totalCalories }
}
