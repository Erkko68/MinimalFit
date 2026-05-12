package eric.bitria.minimalfit.data.gym

data class RoutineExercisePlan(
    val exerciseId: String,
    val targetSets: Int = 1,
    val targetReps: Int = 0,
    val targetWeight: Float = 0f
)
