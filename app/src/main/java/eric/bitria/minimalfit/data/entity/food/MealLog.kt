package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single instance of a meal being logged.
 */
@Serializable
@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val amount: Float,
    val createdAt: Long = System.currentTimeMillis()
)
