package eric.bitria.minimalfit.data.datasource

import eric.bitria.minimalfit.data.model.Meal

/**
 * In-memory food catalogue. In the future this could be backed by a
 * local database or a remote API.
 */
class FoodDatabase {

    val meals: List<Meal> = listOf(
        Meal(1, "Oatmeal with Berries", 350,
            imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop",
            description = "Healthy breakfast"),
        Meal(2, "Grilled Chicken Salad", 450,
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop",
            description = "Lunch"),
        Meal(3, "Protein Shake", 200,
            description = "Post-workout"),
        Meal(4, "Salmon and Asparagus", 600,
            imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop",
            description = "Dinner"),
        Meal(5, "Greek Yogurt", 150,
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500&auto=format&fit=crop",
            description = "Snack"),
        Meal(6, "Avocado Toast", 300,
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop",
            description = "Breakfast"),
        Meal(7, "Beef Stir Fry", 550,
            description = "Lunch")
    )

    fun search(query: String): List<Meal> =
        if (query.isBlank()) meals
        else meals.filter { it.name.contains(query, ignoreCase = true) }
}

