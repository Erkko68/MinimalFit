package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import java.util.UUID

@Serializable
@Entity(
    tableName = "sets",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["sessionExerciseId"])
    ]
)
data class Set(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionExerciseId: String,
    val sessionId: String,
    val weight: Float,
    val reps: Int,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Instant = nowInstant()
)
