package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.UUID

@Entity(tableName = "gym_exercises")
data class GymExerciseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isBodyweight: Boolean = false,
    val muscleGroup: String? = null,
)

@Entity(tableName = "gym_sessions")
data class GymSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val status: GymSessionStatus = GymSessionStatus.ACTIVE,
    val notes: String = ""
)

@Entity(tableName = "gym_sets")
data class GymSetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val exerciseId: String,
    val orderInSession: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Float? = null,
    val isCompleted: Boolean = false,
    val isWarmup: Boolean = false,
    val notes: String = ""
)

enum class GymSessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

// Relations

data class GymSessionWithSets(
    @Embedded val session: GymSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<GymSetEntity>
)

data class GymSetWithSession(
    @Embedded val set: GymSetEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "id"
    )
    val session: GymSessionEntity?
)
