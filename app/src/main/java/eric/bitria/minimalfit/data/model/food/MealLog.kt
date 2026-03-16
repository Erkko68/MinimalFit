package eric.bitria.minimalfit.data.model.food

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A single logged meal entry. Wraps a catalog [Meal] with a unique [id]
 * so the same meal can appear multiple times in a day's log and each
 * entry can be individually removed or updated.
 */
@Serializable
data class MealLog(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val meal: Meal
)

