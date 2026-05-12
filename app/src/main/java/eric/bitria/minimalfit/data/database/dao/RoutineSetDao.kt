package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineSetDao {

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId = :routineExerciseId ORDER BY rowid ASC")
    fun getForRoutineExercise(routineExerciseId: String): Flow<List<RoutineSet>>

    @Query("""
        SELECT rs.* FROM routine_sets rs
        JOIN routine_exercises re ON rs.routineExerciseId = re.id
        WHERE re.routineId = :routineId
        ORDER BY re.createdAt ASC, rs.rowid ASC
    """)
    fun getForRoutine(routineId: String): Flow<List<RoutineSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routineSet: RoutineSet)

    @Update
    suspend fun update(routineSet: RoutineSet)

    @Query("DELETE FROM routine_sets WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId = :routineExerciseId")
    suspend fun deleteForRoutineExercise(routineExerciseId: String)
}
