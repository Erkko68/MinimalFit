package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.SetType
import kotlinx.serialization.Serializable

@Serializable
data class PlannedSetPlan(
    val weight: Float = 0f,
    val reps: Int = 0,
    val type: String = SetType.Reps,
    val durationSeconds: Int = 0,
    val preparationSeconds: Int = 5
)

@Serializable
data class RoutineExercisePlan(
    val exerciseId: String,
    val targetSets: Int = 1,
    val targetReps: Int = 0,
    val targetWeight: Float = 0f,
    val targetType: String = SetType.Reps,
    val targetDurationSeconds: Int = 0,
    val targetPreparationSeconds: Int = 5,
    val plannedSets: List<PlannedSetPlan> = emptyList()
)
