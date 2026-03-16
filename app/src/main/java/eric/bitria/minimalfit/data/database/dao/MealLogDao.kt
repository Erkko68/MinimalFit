package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE date = :date")
    fun getMealLogForDate(date: LocalDate): Flow<MealLog?>

    @Query("SELECT * FROM meal_logs WHERE date BETWEEN :start AND :end")
    fun getMealLogsInRange(start: LocalDate, end: LocalDate): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE date = :date")
    suspend fun deleteMealLogForDate(date: LocalDate)
}
