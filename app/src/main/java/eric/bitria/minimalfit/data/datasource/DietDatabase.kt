package eric.bitria.minimalfit.data.datasource

import eric.bitria.minimalfit.data.model.food.Diet

class DietDatabase {
    val diets = listOf(
        Diet("diet-keto", "Keto Diet", "High-fat, low-carb diet.", "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-mediterranean", "Mediterranean", "Emphasis on plant-based foods and healthy fats.", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-vegan", "Vegan", "Excludes all animal products.", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"),
        Diet("diet-paleo", "Paleo", "Focuses on foods similar to what might have been eaten during the Paleolithic era.", "")
    )

    fun search(query: String): List<Diet> =
        if (query.isBlank()) diets
        else diets.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
}
