package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY createdAt DESC")
    fun getMealLogsForDate(date: LocalDate): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getMealLogsInRange(start: LocalDate, end: LocalDate): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE mealName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMealLogs(query: String): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: String)
}
