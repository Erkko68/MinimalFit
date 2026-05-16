package eric.bitria.minimalfit.data.remote.firestore.dto

import kotlinx.serialization.Serializable

// ── Exercise ────────────────────────────────────────────────────────────────

/** A single exercise as stored in Firestore (both global and per-user). */
@Serializable
data class ExerciseDto(
    val id: String = "",
    val name: String = "",
    val isBodyweight: Boolean = false,
    val muscleGroup: String? = null,
    val restSeconds: Int = 120,
    val isGlobal: Boolean = false,
    val creatorId: String? = null,
    val updatedAt: String = ""  // ISO-8601
)

// ── Routine ─────────────────────────────────────────────────────────────────

@Serializable
data class RoutineSetDto(
    val id: String = "",
    val weight: Float = 0f,
    val reps: Int = 0,
    val type: String = "REPS",
    val durationSeconds: Int = 0,
    val preparationSeconds: Int = 5
)

/** An exercise within a routine, bundled with its planned sets. */
@Serializable
data class RoutineExerciseDto(
    val routineExerciseId: String = "",
    val exercise: ExerciseDto = ExerciseDto(),
    val sets: List<RoutineSetDto> = emptyList(),
    val createdAt: String = ""
)

/** A complete routine document as stored in Firestore. */
@Serializable
data class RoutineDocument(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val daysOfWeekStr: String = "",
    val exercises: List<RoutineExerciseDto> = emptyList(),
    val updatedAt: String = ""
)

// ── Session ─────────────────────────────────────────────────────────────────

@Serializable
data class SetDto(
    val id: String = "",
    val weight: Float = 0f,
    val reps: Int = 0,
    val type: String = "REPS",
    val durationSeconds: Int = 0,
    val preparationSeconds: Int = 5,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: String = ""
)

/** An exercise within a session, bundled with its performed sets. */
@Serializable
data class SessionExerciseDto(
    val sessionExerciseId: String = "",
    val exercise: ExerciseDto = ExerciseDto(),
    val sets: List<SetDto> = emptyList(),
    val createdAt: String = ""
)

/** A complete session document as stored in Firestore. */
@Serializable
data class SessionDocument(
    val id: String = "",
    val startTime: String = "",
    val title: String = "",
    val durationSeconds: Long = 0L,
    val notes: String = "",
    val isFinished: Boolean = false,
    val exercises: List<SessionExerciseDto> = emptyList(),
    val updatedAt: String = ""
)
