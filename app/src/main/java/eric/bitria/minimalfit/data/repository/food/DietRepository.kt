package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.Diet
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    fun getDiets(): Flow<List<Diet>>
    fun getDietById(id: Int): Diet?
}
