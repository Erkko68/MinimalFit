package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.GymDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.relations.SetSessionCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class DefaultSetRepository(
    private val gymDao: GymDao
) : SetRepository {

    override fun getSetsForExercise(exerciseId: String): Flow<List<Set>> =
        gymDao.getSetsForExercise(exerciseId)

    override fun getSetsForSession(sessionId: String): Flow<List<Set>> =
        gymDao.getSetsForSession(sessionId)

    override fun getSessionForSet(setId: String): Flow<Session?> =
        gymDao.getSessionForSet(setId)

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
        val set = Set(
            exerciseId = exerciseId,
            orderInSession = order,
            weight = weight,
            reps = reps,
            rpe = rpe,
            isWarmup = isWarmup,
            notes = notes
        )
        gymDao.insertSet(set)
        gymDao.insertSetSessionCrossRef(SetSessionCrossRef(setId = set.id, sessionId = sessionId))
    }

    override suspend fun updateSet(set: Set) {
        gymDao.updateSet(set)
    }

    override suspend fun deleteSet(setId: String) {
        gymDao.deleteSetAndRelations(setId)
    }

    override suspend fun copyPreviousSet(sessionId: String, exerciseId: String): Set? {
        val sets = gymDao.getSetsForSession(sessionId).first()
        val lastSet = sets.lastOrNull { it.exerciseId == exerciseId } ?: return null
        val order = (sets.maxOfOrNull { it.orderInSession } ?: 0) + 1
        val newSet = lastSet.copy(
            id = UUID.randomUUID().toString(),
            orderInSession = order,
            isCompleted = false
        )
        gymDao.insertSet(newSet)
        gymDao.insertSetSessionCrossRef(SetSessionCrossRef(setId = newSet.id, sessionId = sessionId))
        return newSet
    }
}

