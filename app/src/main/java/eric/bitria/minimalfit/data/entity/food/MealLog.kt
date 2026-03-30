package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import java.util.UUID

/**
 * Represents a session of logging food (e.g., "Breakfast").
 */
@Serializable
@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Instant = nowInstant()
)
