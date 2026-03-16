package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A single logged meal entry. Wraps a catalog [Meal] with a unique [id]
 * so the same meal can appear multiple times in a day's log and each
 * entry can be individually removed or updated.
 */
@Serializable
@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate, // Added for easier querying by day
    val createdAt: Long = System.currentTimeMillis(),
    val mealId: String,
    val mealName: String, // Denormalized for quick display
    val calories: Int
)
