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
    tableName = "routine_exercises",
    indices = [Index(value = ["routineId"]), Index(value = ["exerciseId"])]
)
data class RoutineExercise(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val routineId: String,
    val exerciseId: String,
    val createdAt: Instant = nowInstant()
)
