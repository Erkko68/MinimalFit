package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.relations.DietMealCrossRef
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

    // Relation methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietMealCrossRef(crossRef: DietMealCrossRef)

    @Query("DELETE FROM diet_meal_cross_ref WHERE dietId = :dietId")
    suspend fun deleteMealsForDiet(dietId: String)

    @Query("DELETE FROM diet_meal_cross_ref WHERE dietId = :dietId AND mealId = :mealId")
    suspend fun deleteMealFromDiet(dietId: String, mealId: String)

    @Query("SELECT * FROM diet_meal_cross_ref WHERE dietId = :dietId")
    fun getMealsForDiet(dietId: String): Flow<List<DietMealCrossRef>>
}
