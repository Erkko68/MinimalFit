package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity

@Entity(
    tableName = "routine_exercise_cross_refs",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseCrossRef(
    val routineId: String,
    val exerciseId: String
)
