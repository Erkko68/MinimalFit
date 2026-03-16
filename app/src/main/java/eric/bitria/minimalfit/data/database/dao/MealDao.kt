package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE name LIKE '%' || :query || '%'")
    fun searchMeals(query: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id")
    fun getMeal(id: String): Flow<Meal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: String)
}
