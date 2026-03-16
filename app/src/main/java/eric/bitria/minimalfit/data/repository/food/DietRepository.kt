package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    /** Returns a specific diet by ID as a stream. */
    fun getDiet(id: String): Flow<Diet?>
    
    /** Returns diets matching the query as a stream. */
    fun getDiets(query: String = ""): Flow<List<Diet>>
    
    suspend fun addDiet(diet: Diet)
    suspend fun updateDiet(diet: Diet)
    suspend fun deleteDiet(id: String)
}
