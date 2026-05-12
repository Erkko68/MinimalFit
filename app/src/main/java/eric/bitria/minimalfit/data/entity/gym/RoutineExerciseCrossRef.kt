package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity

@Entity(
    tableName = "routine_exercise_cross_refs",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseCrossRef(
    val routineId: String,
    val exerciseId: String,
    val targetSets: Int = 1,
    val targetReps: Int = 0,
    val targetWeight: Float = 0f,
    val position: Int = 0
)
