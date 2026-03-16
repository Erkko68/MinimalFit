package eric.bitria.minimalfit.data.model.food

data class Meal(
    val id: String,
    val name: String,
    val description: String = "",
    val calories: Int,
    val imageUrl: String? = null,
    val relatedMealIds: List<String> = emptyList()
)
