package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    
    /** 
     * Returns diets matching the optional filters.
     */
    fun getDiets(query: String = "", limit: Int = -1): Flow<List<Diet>>

    /** Returns a specific diet by ID as a stream. */
    fun getDiet(id: String): Flow<Diet?>
    suspend fun addDiet(diet: Diet)
    suspend fun updateDiet(diet: Diet)
    suspend fun deleteDiet(id: String)

    // Relation methods
    /** Returns all meals associated with a diet. */
    fun getMealsForDiet(dietId: String): Flow<List<Meal>>
    
    /** Returns the amount/serving size of a specific meal within a diet. */
    fun getMealAmountInDiet(dietId: String, mealId: String): Flow<Float>

    /** Returns the total calories of a diet. */
    fun getDietCalories(dietId: String): Flow<Int>

    suspend fun addMealToDiet(dietId: String, mealId: String, amount: Float)
    suspend fun removeMealFromDiet(dietId: String, mealId: String)
}
