package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.model.Diet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryDietRepository : DietRepository {
    private val _diets = MutableStateFlow(
        listOf(
            Diet(1, "Keto Diet", "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=2070&auto=format&fit=crop"),
            Diet(2, "Mediterranean", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
            Diet(3, "Vegan", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
            Diet(4, "Paleo", null)
        )
    )

    override fun getDiets(): Flow<List<Diet>> = _diets.asStateFlow()

    override fun getDietById(id: Int): Diet? = _diets.value.find { it.id == id }
}
