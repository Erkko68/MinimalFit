package eric.bitria.minimalfit.data.model.food

import kotlinx.serialization.Serializable

@Serializable
data class Diet(
    val id: String,
    val name: String,
    val description: String = "",
    val imageUrl: String? = null
)
