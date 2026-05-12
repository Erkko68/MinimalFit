package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.database.dao.ExerciseDao
import eric.bitria.minimalfit.data.database.dao.RoutineDao
import eric.bitria.minimalfit.data.database.dao.RoutineExerciseDao
import eric.bitria.minimalfit.data.database.dao.RoutineSetDao
import eric.bitria.minimalfit.data.database.dao.SessionDao
import eric.bitria.minimalfit.data.database.dao.SessionExerciseDao
import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.firestore.GymFirestoreDataSource
import eric.bitria.minimalfit.data.remote.firestore.dto.ExerciseDto
import eric.bitria.minimalfit.data.remote.firestore.dto.RoutineDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.RoutineExerciseDto
import eric.bitria.minimalfit.data.remote.firestore.dto.RoutineSetDto
import eric.bitria.minimalfit.data.remote.firestore.dto.SessionDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.SessionExerciseDto
import eric.bitria.minimalfit.data.remote.firestore.dto.SetDto
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Instant

class GymSyncService(
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao,
    private val routineSetDao: RoutineSetDao,
    private val sessionDao: SessionDao,
    private val sessionExerciseDao: SessionExerciseDao,
    private val setDao: SetDao,
    private val firestoreDataSource: GymFirestoreDataSource,
    private val authRepository: AuthRepository,
    private val log: SyncLogStore
) {
    companion object {
        private const val TAG = "GymSyncService"
    }

    // ── Global exercises ────────────────────────────────────────────────────

    suspend fun syncGlobalExercises(): Result<Unit> = runCatching {
        val remote = firestoreDataSource.fetchGlobalExercises()
        remote.forEach { dto -> exerciseDao.insertExercise(dto.toEntity()) }
        log.d(TAG, "Synced ${remote.size} global exercises")
    }

    // ── User exercises ──────────────────────────────────────────────────────

    suspend fun backupUserExercises(): Result<Unit> = runCatching {
        val uid = requireUid()
        val userExercises = (exerciseDao.getExercises().firstOrNull() ?: emptyList()).filter { !it.isGlobal }
        if (userExercises.isEmpty()) return@runCatching
        firestoreDataSource.uploadUserExercises(uid, userExercises.map { it.toDto() })
        log.d(TAG, "Backed up ${userExercises.size} user exercises")
    }

    suspend fun restoreUserExercises(): Result<Unit> = runCatching {
        val uid = requireUid()
        val remote = firestoreDataSource.downloadUserExercises(uid)
        remote.forEach { dto -> exerciseDao.insertExercise(dto.toEntity()) }
        log.d(TAG, "Restored ${remote.size} user exercises")
    }

    // ── Routines ────────────────────────────────────────────────────────────

    suspend fun backupRoutines(): Result<Unit> = runCatching {
        val uid = requireUid()
        val routines = routineDao.getAll().firstOrNull() ?: emptyList()
        if (routines.isEmpty()) return@runCatching
        val documents = routines.map { buildRoutineDocument(it) }
        firestoreDataSource.uploadRoutines(uid, documents)
        log.d(TAG, "Backed up ${documents.size} routines")
    }

    suspend fun restoreRoutines(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadRoutines(uid)
        documents.forEach { doc ->
            routineDao.insert(Routine(id = doc.id, name = doc.name, description = doc.description, daysOfWeekStr = doc.daysOfWeekStr, updatedAt = parseInstant(doc.updatedAt)))
            doc.exercises.forEach { reDto ->
                exerciseDao.insertExercise(reDto.exercise.toEntity())
                routineExerciseDao.insert(RoutineExercise(id = reDto.routineExerciseId, routineId = doc.id, exerciseId = reDto.exercise.id, createdAt = parseInstant(reDto.createdAt)))
                reDto.sets.forEach { setDto ->
                    routineSetDao.insert(RoutineSet(id = setDto.id, routineExerciseId = reDto.routineExerciseId, weight = setDto.weight, reps = setDto.reps))
                }
            }
        }
        log.d(TAG, "Restored ${documents.size} routines")
    }

    // ── Sessions ────────────────────────────────────────────────────────────

    suspend fun backupSessions(): Result<Unit> = runCatching {
        val uid = requireUid()
        val finishedSessions = (sessionDao.getSessions().firstOrNull() ?: emptyList()).filter { it.isFinished }
        if (finishedSessions.isEmpty()) return@runCatching
        val documents = finishedSessions.map { buildSessionDocument(it) }
        firestoreDataSource.uploadSessions(uid, documents)
        log.d(TAG, "Backed up ${documents.size} sessions")
    }

    suspend fun restoreSessions(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadSessions(uid)
        documents.forEach { doc ->
            sessionDao.insertSession(Session(id = doc.id, startTime = parseInstant(doc.startTime), title = doc.title, durationSeconds = doc.durationSeconds, notes = doc.notes, isFinished = doc.isFinished, updatedAt = parseInstant(doc.updatedAt)))
            doc.exercises.forEach { seDto ->
                exerciseDao.insertExercise(seDto.exercise.toEntity())
                sessionExerciseDao.insert(SessionExercise(id = seDto.sessionExerciseId, sessionId = doc.id, exerciseId = seDto.exercise.id, createdAt = parseInstant(seDto.createdAt)))
                seDto.sets.forEach { setDto ->
                    setDao.insertSet(Set(id = setDto.id, sessionExerciseId = seDto.sessionExerciseId, sessionId = doc.id, weight = setDto.weight, reps = setDto.reps, notes = setDto.notes, isCompleted = setDto.isCompleted, createdAt = parseInstant(setDto.createdAt)))
                }
            }
        }
        log.d(TAG, "Restored ${documents.size} sessions")
    }

    // ── Single-entity upload / delete ───────────────────────────────────────

    suspend fun uploadSingleExercise(uid: String, exerciseId: String) {
        val exercise = exerciseDao.getExercise(exerciseId).firstOrNull() ?: return
        firestoreDataSource.uploadUserExercises(uid, listOf(exercise.toDto()))
        log.d(TAG, "Uploaded single exercise $exerciseId")
    }

    suspend fun deleteSingleExercise(uid: String, exerciseId: String) {
        firestoreDataSource.deleteUserExercise(uid, exerciseId)
        log.d(TAG, "Deleted single exercise $exerciseId")
    }

    suspend fun uploadSingleRoutine(uid: String, routineId: String) {
        val routine = routineDao.getById(routineId).firstOrNull() ?: return
        firestoreDataSource.uploadRoutines(uid, listOf(buildRoutineDocument(routine)))
        log.d(TAG, "Uploaded single routine $routineId")
    }

    suspend fun deleteSingleRoutine(uid: String, routineId: String) {
        firestoreDataSource.deleteRoutine(uid, routineId)
        log.d(TAG, "Deleted single routine $routineId")
    }

    suspend fun uploadSingleSession(uid: String, sessionId: String) {
        val session = sessionDao.getSession(sessionId).firstOrNull() ?: return
        if (!session.isFinished) return
        firestoreDataSource.uploadSessions(uid, listOf(buildSessionDocument(session)))
        log.d(TAG, "Uploaded single session $sessionId")
    }

    suspend fun deleteSingleSession(uid: String, sessionId: String) {
        firestoreDataSource.deleteSession(uid, sessionId)
        log.d(TAG, "Deleted single session $sessionId")
    }

    // ── Full sync ───────────────────────────────────────────────────────────

    suspend fun fullSync(): Result<Unit> = runCatching {
        syncGlobalExercises().getOrThrow()
        backupUserExercises().getOrThrow()
        backupRoutines().getOrThrow()
        backupSessions().getOrThrow()
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private suspend fun buildRoutineDocument(routine: Routine): RoutineDocument {
        val routineExercises = routineExerciseDao.getForRoutine(routine.id).firstOrNull() ?: emptyList()
        val exerciseDtos = routineExercises.map { re ->
            val exercise = exerciseDao.getExercise(re.exerciseId).firstOrNull()
            val sets = routineSetDao.getForRoutineExercise(re.id).firstOrNull() ?: emptyList()
            RoutineExerciseDto(routineExerciseId = re.id, exercise = exercise?.toDto() ?: ExerciseDto(id = re.exerciseId), sets = sets.map { it.toDto() }, createdAt = re.createdAt.toString())
        }
        return RoutineDocument(id = routine.id, name = routine.name, description = routine.description, daysOfWeekStr = routine.daysOfWeekStr, exercises = exerciseDtos, updatedAt = routine.updatedAt.toString())
    }

    private suspend fun buildSessionDocument(session: Session): SessionDocument {
        val sessionExercises = sessionExerciseDao.getSessionExercises(session.id).firstOrNull() ?: emptyList()
        val exerciseDtos = sessionExercises.map { se ->
            val exercise = exerciseDao.getExercise(se.exerciseId).firstOrNull()
            val sets = setDao.getSetsForSessionExercise(se.id).firstOrNull() ?: emptyList()
            SessionExerciseDto(sessionExerciseId = se.id, exercise = exercise?.toDto() ?: ExerciseDto(id = se.exerciseId), sets = sets.map { it.toDto() }, createdAt = se.createdAt.toString())
        }
        return SessionDocument(id = session.id, startTime = session.startTime.toString(), title = session.title, durationSeconds = session.durationSeconds, notes = session.notes, isFinished = session.isFinished, exercises = exerciseDtos, updatedAt = session.updatedAt.toString())
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private fun Exercise.toDto() = ExerciseDto(id = id, name = name, isBodyweight = isBodyweight, muscleGroup = muscleGroup, restSeconds = restSeconds, isGlobal = isGlobal, creatorId = creatorId, updatedAt = updatedAt.toString())
    private fun ExerciseDto.toEntity() = Exercise(id = id, name = name, isBodyweight = isBodyweight, muscleGroup = muscleGroup, restSeconds = restSeconds, isGlobal = isGlobal, creatorId = creatorId, updatedAt = parseInstant(updatedAt))
    private fun RoutineSet.toDto() = RoutineSetDto(id = id, weight = weight, reps = reps)
    private fun Set.toDto() = SetDto(id = id, weight = weight, reps = reps, notes = notes, isCompleted = isCompleted, createdAt = createdAt.toString())

    private fun requireUid(): String = authRepository.currentUser.value?.uid ?: throw IllegalStateException("User not authenticated")
    private fun parseInstant(value: String): Instant = if (value.isBlank()) nowInstant() else try { Instant.parse(value) } catch (_: Exception) { nowInstant() }
}
