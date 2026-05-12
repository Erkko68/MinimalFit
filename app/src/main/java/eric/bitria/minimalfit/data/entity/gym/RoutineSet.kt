package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "routine_sets",
    indices = [Index(value = ["routineExerciseId"])]
)
data class RoutineSet(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val routineExerciseId: String,
    val weight: Float = 0f,
    val reps: Int = 0
)
