package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.GymDao
import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionStatus
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.time.LocalDateTime

class DefaultGymRepository(
    private val gymDao: GymDao
) : GymRepository {

    override fun getRecentSessions(limit: Int): Flow<List<GymSessionEntity>> =
        gymDao.getRecentSessions(limit)
        
    override fun getRecentSessionsWithSets(limit: Int): Flow<List<GymSessionWithSets>> =
        gymDao.getRecentSessionsWithSets(limit)

    override fun getSession(sessionId: String): Flow<GymSessionWithSets?> =
        gymDao.getSessionWithSets(sessionId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveSession(): Flow<GymSessionWithSets?> =
        gymDao.getActiveSession().flatMapLatest { session ->
            if (session == null) flowOf(null)
            else gymDao.getSessionWithSets(session.id)
        }

    override suspend fun startSession(): String {
        val now = LocalDateTime.now()
        val date = LocalDate(now.year, now.monthValue, now.dayOfMonth)
        val time = LocalTime(now.hour, now.minute, now.second)
        val session = GymSessionEntity(
            date = date,
            startTime = time,
            status = GymSessionStatus.ACTIVE
        )
        gymDao.insertSession(session)
        return session.id
    }

    override suspend fun pauseSession() {
        val active = gymDao.getActiveSession().first() ?: return
        gymDao.updateSession(active.copy(status = GymSessionStatus.PAUSED))
    }

    override suspend fun resumeSession() {
        val active = gymDao.getActiveSession().first() ?: return
        gymDao.updateSession(active.copy(status = GymSessionStatus.ACTIVE))
    }

    override suspend fun finishSession() {
        val active = gymDao.getActiveSession().first() ?: return
        val now = LocalDateTime.now()
        val endTime = LocalTime(now.hour, now.minute, now.second)
        gymDao.updateSession(active.copy(status = GymSessionStatus.COMPLETED, endTime = endTime))
    }

    override fun getExercises(): Flow<List<GymExerciseEntity>> =
        gymDao.getExercises()

    override suspend fun addExercise(name: String): GymExerciseEntity {
        val exercise = GymExerciseEntity(name = name)
        gymDao.insertExercise(exercise)
        return exercise
    }

    override suspend fun addSet(
        sessionId: String,
        exerciseId: String,
        weight: Float,
        reps: Int,
        rpe: Float?,
        isWarmup: Boolean,
        notes: String
    ) {
        val sets = gymDao.getSetsForSession(sessionId).first()
        val order = (sets.maxOfOrNull { it.orderInSession } ?: 0) + 1
        val set = GymSetEntity(
            sessionId = sessionId,
            exerciseId = exerciseId,
            orderInSession = order,
            weight = weight,
            reps = reps,
            rpe = rpe,
            isWarmup = isWarmup,
            notes = notes
        )
        gymDao.insertSet(set)
    }

    override suspend fun updateSet(set: GymSetEntity) {
        gymDao.updateSet(set)
    }

    override suspend fun deleteSet(setId: String) {
        gymDao.deleteSet(setId)
    }

    override suspend fun deleteSession(sessionId: String) {
        gymDao.deleteSession(sessionId)
    }

    override suspend fun copyPreviousSet(sessionId: String, exerciseId: String): GymSetEntity? {
        val sets = gymDao.getSetsForSession(sessionId).first()
        val lastSet = sets.lastOrNull { it.exerciseId == exerciseId } ?: return null
        val order = (sets.maxOfOrNull { it.orderInSession } ?: 0) + 1
        val newSet = lastSet.copy(
            id = java.util.UUID.randomUUID().toString(),
            orderInSession = order,
            isCompleted = false
        )
        gymDao.insertSet(newSet)
        return newSet
    }
}
