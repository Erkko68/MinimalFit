package eric.bitria.minimalfit.data.model.food

data class Diet(
    val id: String,
    val name: String,
    val description: String = "",
    val imageUrl: String? = null
)
