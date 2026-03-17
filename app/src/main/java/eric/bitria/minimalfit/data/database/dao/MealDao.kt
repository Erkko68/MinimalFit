package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.relations.MealIngredientCrossRef
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

    // Relation methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealIngredientCrossRef(crossRef: MealIngredientCrossRef)

    @Query("DELETE FROM meal_ingredient_cross_ref WHERE mealId = :mealId")
    suspend fun deleteIngredientsForMeal(mealId: String)

    @Query("SELECT * FROM meal_ingredient_cross_ref WHERE mealId = :mealId")
    fun getIngredientsForMeal(mealId: String): Flow<List<MealIngredientCrossRef>>
}
