package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.Diet
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {
    @Query("SELECT * FROM diets")
    fun getAllDiets(): Flow<List<Diet>>

    @Query("SELECT * FROM diets WHERE name LIKE '%' || :query || '%'")
    fun searchDiets(query: String): Flow<List<Diet>>

    @Query("SELECT * FROM diets WHERE id = :id")
    fun getDiet(id: String): Flow<Diet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    @Update
    suspend fun updateDiet(diet: Diet)

    @Query("DELETE FROM diets WHERE id = :id")
    suspend fun deleteDiet(id: String)
}
