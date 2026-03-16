package eric.bitria.minimalfit.data.entity.food

import kotlinx.serialization.Serializable

@Serializable
data class Meal(
    val id: String,
    val name: String,
    val description: String = "",
    val calories: Int,
    val imageUrl: String? = null,
    val relatedMealIds: List<String> = emptyList()
)
