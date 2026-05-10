package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExerciseCrossRef
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query(
        """
        SELECT routines.id, routines.name, COUNT(routine_exercise_cross_refs.exerciseId) AS exerciseCount
        FROM routines
        LEFT JOIN routine_exercise_cross_refs ON routines.id = routine_exercise_cross_refs.routineId
        GROUP BY routines.id, routines.name
        ORDER BY routines.name COLLATE NOCASE ASC
        """
    )
    fun getRoutineSummaries(): Flow<List<RoutineSummary>>

    @Query("SELECT exerciseId FROM routine_exercise_cross_refs WHERE routineId = :routineId")
    suspend fun getRoutineExerciseIds(routineId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(crossRefs: List<RoutineExerciseCrossRef>)

    @Query("DELETE FROM routine_exercise_cross_refs WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: String)

    @Query("UPDATE routines SET name = :name WHERE id = :routineId")
    suspend fun renameRoutine(routineId: String, name: String)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)

    @Transaction
    suspend fun createRoutine(name: String, exerciseIds: List<String>) {
        val routine = Routine(name = name)
        insertRoutine(routine)
        insertRoutineExercises(
            exerciseIds.distinct().map { exerciseId ->
                RoutineExerciseCrossRef(
                    routineId = routine.id,
                    exerciseId = exerciseId
                )
            }
        )
    }

    @Transaction
    suspend fun deleteRoutineWithExercises(routineId: String) {
        deleteRoutineExercises(routineId)
        deleteRoutine(routineId)
    }
}
