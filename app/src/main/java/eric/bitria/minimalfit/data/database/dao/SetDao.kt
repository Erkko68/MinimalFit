package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.relations.SetSessionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {

    @Query(
        """
        SELECT sets.*
        FROM sets
        INNER JOIN set_session_cross_ref ON sets.id = set_session_cross_ref.setId
        INNER JOIN sessions ON set_session_cross_ref.sessionId = sessions.id
        WHERE sets.exerciseId = :exerciseId
        ORDER BY sessions.startTime ASC
        """
    )
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>

    @Query(
        """
        SELECT sessions.*
        FROM sessions
        INNER JOIN set_session_cross_ref ON sessions.id = set_session_cross_ref.sessionId
        WHERE set_session_cross_ref.setId = :setId
        LIMIT 1
        """
    )
    fun getSessionForSet(setId: String): Flow<Session?>

    @Query(
        """
        SELECT sets.*
        FROM sets
        INNER JOIN set_session_cross_ref ON sets.id = set_session_cross_ref.setId
        WHERE set_session_cross_ref.sessionId = :sessionId
        ORDER BY sets.orderInSession ASC
        """
    )
    fun getSetsForSession(sessionId: String): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE id = :setId LIMIT 1")
    suspend fun getSetById(setId: String): Set?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetSessionCrossRef(crossRef: SetSessionCrossRef)

    @Update
    suspend fun updateSet(set: Set)

    @Query("DELETE FROM sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("DELETE FROM set_session_cross_ref WHERE setId = :setId")
    suspend fun deleteSetSessionCrossRefBySetId(setId: String)

    @Query("SELECT setId FROM set_session_cross_ref WHERE sessionId = :sessionId")
    suspend fun getSetIdsForSession(sessionId: String): List<String>

    @Query("DELETE FROM sets WHERE id IN (:setIds)")
    suspend fun deleteSetsByIds(setIds: List<String>)

    @Query("DELETE FROM set_session_cross_ref WHERE sessionId = :sessionId")
    suspend fun deleteSetSessionCrossRefsForSession(sessionId: String)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Transaction
    suspend fun deleteSessionAndSets(sessionId: String) {
        val setIds = getSetIdsForSession(sessionId)
        if (setIds.isNotEmpty()) {
            deleteSetsByIds(setIds)
        }
        deleteSetSessionCrossRefsForSession(sessionId)
        deleteSession(sessionId)
    }

    @Transaction
    suspend fun deleteSetAndRelations(setId: String) {
        deleteSetSessionCrossRefBySetId(setId)
        deleteSet(setId)
    }
}

