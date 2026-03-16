package eric.bitria.minimalfit.data.datasource

import eric.bitria.minimalfit.data.model.food.Diet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory diet database.
 */
class DietDatabase {
    private val _diets = MutableStateFlow(listOf(
        Diet("diet-keto", "Keto Diet", "High-fat, low-carb diet.", "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-mediterranean", "Mediterranean", "Emphasis on plant-based foods and healthy fats.", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-vegan", "Vegan", "Excludes all animal products.", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-paleo", "Paleo", "Focuses on foods similar to what might have been eaten during the Paleolithic era.", "")
    ))

    fun getDiets(query: String): Flow<List<Diet>> {
        return _diets.map { diets ->
            if (query.isBlank()) diets
            else diets.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }

    fun getDiet(id: String): Flow<Diet?> =
        _diets.map { diets -> diets.find { it.id == id } }

    suspend fun addDiet(diet: Diet) {
        val newDiet = if (diet.id.isBlank()) diet.copy(id = UUID.randomUUID().toString()) else diet
        _diets.value += newDiet
    }

    suspend fun updateDiet(diet: Diet) {
        _diets.value = _diets.value.map {
            if (it.id == diet.id) diet else it
        }
    }

    suspend fun deleteDiet(id: String) {
        _diets.value = _diets.value.filter { it.id != id }
    }
}
