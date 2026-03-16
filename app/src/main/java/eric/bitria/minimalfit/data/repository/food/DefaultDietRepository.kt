package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.database.dao.DietDao
import eric.bitria.minimalfit.data.entity.food.Diet
import kotlinx.coroutines.flow.Flow

class DefaultDietRepository(private val dietDao: DietDao) : DietRepository {

    override fun getDiet(id: String): Flow<Diet?> =
        dietDao.getDiet(id)

    override fun getDiets(query: String): Flow<List<Diet>> =
        if (query.isBlank()) dietDao.getAllDiets()
        else dietDao.searchDiets(query)

    override suspend fun addDiet(diet: Diet) =
        dietDao.insertDiet(diet)

    override suspend fun updateDiet(diet: Diet) =
        dietDao.updateDiet(diet)

    override suspend fun deleteDiet(id: String) =
        dietDao.deleteDiet(id)
}
