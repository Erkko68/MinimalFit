package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getMealLogsInRange(start: Long, end: Long): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: String)
}
