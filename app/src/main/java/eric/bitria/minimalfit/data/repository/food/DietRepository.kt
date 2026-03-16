package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.food.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    fun getDiets(): Flow<List<Diet>>
    fun getDietById(id: String): Diet?
}
