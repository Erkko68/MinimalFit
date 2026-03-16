package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.DietDatabase
import eric.bitria.minimalfit.data.entity.food.Diet
import kotlinx.coroutines.flow.Flow

class DefaultDietRepository(private val dietDatabase: DietDatabase) : DietRepository {

    override fun getDiet(id: String): Flow<Diet?> =
        dietDatabase.getDiet(id)

    override fun getDiets(query: String): Flow<List<Diet>> =
        dietDatabase.getDiets(query)

    override suspend fun addDiet(diet: Diet) =
        dietDatabase.addDiet(diet)

    override suspend fun updateDiet(diet: Diet) =
        dietDatabase.updateDiet(diet)

    override suspend fun deleteDiet(id: String) =
        dietDatabase.deleteDiet(id)
}
