package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.relations.SetSessionCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class DefaultSetRepository(
    private val setDao: SetDao
) : SetRepository {

    override fun getSetsForExercise(exerciseId: String): Flow<List<Set>> =
        setDao.getSetsForExercise(exerciseId)

    override fun getSetsForSession(sessionId: String): Flow<List<Set>> =
        setDao.getSetsForSession(sessionId)

    override fun getSessionForSet(setId: String): Flow<Session?> =
        setDao.getSessionForSet(setId)

    override suspend fun getSetById(setId: String): Set? {
        return setDao.getSetById(setId)
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
        val sets = setDao.getSetsForSession(sessionId).first()
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
        setDao.insertSet(set)
        setDao.insertSetSessionCrossRef(SetSessionCrossRef(setId = set.id, sessionId = sessionId))
    }

    override suspend fun updateSet(set: Set): Boolean {
        val previous = setDao.getSetById(set.id)
        setDao.updateSet(set)
        return previous?.isCompleted == false && set.isCompleted
    }

    override suspend fun completeLatestIncompleteSet(sessionId: String): Set? {
        val sets = setDao.getSetsForSession(sessionId).first()
        val latest = sets
            .filter { !it.isCompleted }
            .maxByOrNull { it.orderInSession }
            ?: return null

        val updated = latest.copy(isCompleted = true)
        setDao.updateSet(updated)
        return updated
    }

    override suspend fun deleteSet(setId: String) {
        setDao.deleteSetAndRelations(setId)
    }

    override suspend fun copyPreviousSet(sessionId: String, exerciseId: String): Set? {
        val sets = setDao.getSetsForSession(sessionId).first()
        val lastSet = sets.lastOrNull { it.exerciseId == exerciseId } ?: return null
        val order = (sets.maxOfOrNull { it.orderInSession } ?: 0) + 1
        val newSet = lastSet.copy(
            id = UUID.randomUUID().toString(),
            orderInSession = order,
            isCompleted = false
        )
        setDao.insertSet(newSet)
        setDao.insertSetSessionCrossRef(SetSessionCrossRef(setId = newSet.id, sessionId = sessionId))
        return newSet
    }
}

