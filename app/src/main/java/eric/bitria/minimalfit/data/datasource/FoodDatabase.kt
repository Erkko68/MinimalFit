package eric.bitria.minimalfit.data.datasource

import eric.bitria.minimalfit.data.model.food.Meal

/**
 * In-memory food catalogue. In the future this could be backed by a
 * local database or a remote API.
 */
class FoodDatabase {

    val meals: List<Meal> = listOf(
        Meal(
            id = "meal-oatmeal-berries",
            name = "Oatmeal with Berries",
            calories = 350,
            imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop",
            description = "Healthy breakfast bowl with fiber and antioxidants.",
            relatedMealIds = listOf("meal-greek-yogurt", "meal-protein-shake")
        ),
        Meal(
            id = "meal-grilled-chicken-salad",
            name = "Grilled Chicken Salad",
            calories = 450,
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop",
            description = "Balanced lunch with lean protein and greens.",
            relatedMealIds = listOf("meal-avocado-toast")
        ),
        Meal(
            id = "meal-protein-shake",
            name = "Protein Shake",
            calories = 200,
            description = "Post-workout recovery option.",
            relatedMealIds = listOf("meal-oatmeal-berries")
        ),
        Meal(
            id = "meal-salmon-asparagus",
            name = "Salmon and Asparagus",
            calories = 600,
            imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop",
            description = "Omega-3 rich dinner.",
            relatedMealIds = listOf("meal-beef-stir-fry")
        ),
        Meal(
            id = "meal-greek-yogurt",
            name = "Greek Yogurt",
            calories = 150,
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500&auto=format&fit=crop",
            description = "High-protein snack.",
            relatedMealIds = listOf("meal-oatmeal-berries")
        ),
        Meal(
            id = "meal-avocado-toast",
            name = "Avocado Toast",
            calories = 300,
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop",
            description = "Simple breakfast with healthy fats.",
            relatedMealIds = listOf("meal-grilled-chicken-salad")
        ),
        Meal(
            id = "meal-beef-stir-fry",
            name = "Beef Stir Fry",
            calories = 550,
            // Local resource token example; UI support can be added later without changing model types.
            imageUrl = "drawable:meal_beef_stir_fry",
            description = "Lunch option with veggies and protein.",
            relatedMealIds = listOf("meal-salmon-asparagus")
        )
    )

    fun search(query: String): List<Meal> =
        if (query.isBlank()) meals
        else meals.filter { it.name.contains(query, ignoreCase = true) }
}
