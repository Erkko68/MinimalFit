package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' LIMIT :limit")
    fun getExercises(query: String = "", limit: Int = -1): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExercise(id: String): Flow<Exercise?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)
}
