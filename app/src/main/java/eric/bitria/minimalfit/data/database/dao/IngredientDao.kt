package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.food.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' LIMIT :limit")
    fun getIngredients(query: String = "", limit: Int = -1): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    fun getIngredient(id: String): Flow<Ingredient?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredient(id: String)
}
