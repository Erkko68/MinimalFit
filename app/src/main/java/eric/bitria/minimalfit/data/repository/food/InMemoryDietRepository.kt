package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.datasource.DietDatabase
import eric.bitria.minimalfit.data.model.food.Diet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class InMemoryDietRepository(private val dietDatabase: DietDatabase) : DietRepository {
    private val _diets = MutableStateFlow(dietDatabase.diets)

    override fun getDiet(id: String): Flow<Diet?> =
        _diets.map { diets -> diets.find { it.id == id } }

    override fun getDiets(query: String): Flow<List<Diet>> {
        return _diets.map { diets ->
            if (query.isBlank()) diets
            else diets.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun addDiet(diet: Diet) {
        val newDiet = if (diet.id.isBlank()) diet.copy(id = UUID.randomUUID().toString()) else diet
        _diets.value += newDiet
    }

    override suspend fun updateDiet(diet: Diet) {
        _diets.value = _diets.value.map {
            if (it.id == diet.id) diet else it
        }
    }

    override suspend fun deleteDiet(id: String) {
        _diets.value = _diets.value.filter { it.id != id }
    }
}
