package eric.bitria.minimalfit.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * A single meal logged in the food journal.
 *
 * @param loggedAt  When the entry was first created.
 * @param editedAt  When the entry was last modified, or null if never edited.
 */
data class MealEntry(
    val id: String,
    val meal: Meal,
    val servings: Float = 1f,
    val loggedAt: Instant = Instant.now(),
    val editedAt: Instant? = null
) {
    val totalCalories: Int get() = (meal.calories * servings).toInt()

    /** The calendar date this entry belongs to, derived from [loggedAt] in the system timezone. */
    val date: LocalDate
        get() = loggedAt.atZone(ZoneId.systemDefault()).toLocalDate()
}
