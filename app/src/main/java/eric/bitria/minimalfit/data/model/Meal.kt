package eric.bitria.minimalfit.data.model

data class Meal(
    val id: Int,
    val name: String,
    val calories: Int,
    val imageUrl: String? = null,
    val description: String? = null
)

