package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY createdAt ASC")
    fun getForRoutine(routineId: String): Flow<List<RoutineExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routineExercise: RoutineExercise)

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteForRoutine(routineId: String)

    @Query("SELECT routineId FROM routine_exercises WHERE id = :routineExerciseId LIMIT 1")
    suspend fun getRoutineIdForExercise(routineExerciseId: String): String?
}
