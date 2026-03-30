package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.entity.food.relations.MealLogMealCrossRef
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getMealLogsInRange(start: Instant, end: Instant): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: String)

    // Relation methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogMealCrossRef(crossRef: MealLogMealCrossRef)

    @Query("DELETE FROM meal_log_meal_cross_ref WHERE mealLogId = :mealLogId")
    suspend fun deleteMealsForMealLog(mealLogId: String)

    @Query("SELECT * FROM meal_log_meal_cross_ref WHERE mealLogId = :mealLogId")
    fun getMealsForMealLog(mealLogId: String): Flow<List<MealLogMealCrossRef>>
}
