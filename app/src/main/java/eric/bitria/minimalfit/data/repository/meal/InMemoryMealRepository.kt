package eric.bitria.minimalfit.data.repository.meal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.Nutrient
import eric.bitria.minimalfit.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class InMemoryMealRepository : MealRepository {

    private val _meals = MutableStateFlow(defaultMeals())

    override fun getMeals(): Flow<List<Meal>> = _meals


    override suspend fun getMealById(id: String): Meal? =
        _meals.value.find { it.id == id }

    override suspend fun addMeal(meal: Meal) {
        _meals.update { it + meal }
    }

    override suspend fun updateMeal(meal: Meal) {
        _meals.update { meals -> meals.map { if (it.id == meal.id) meal else it } }
    }

    override suspend fun deleteMeal(id: String) {
        _meals.update { meals -> meals.filter { it.id != id } }
    }

    // ── Seed data ─────────────────────────────────────────────────────────────

    private fun defaultMeals(): List<Meal> {
        fun meal(
            name: String,
            kcal: Int,
            desc: String,
            tags: List<String>,
            color: androidx.compose.ui.graphics.Color,
            icon: androidx.compose.ui.graphics.vector.ImageVector
        ) = Meal(
            id = UUID.randomUUID().toString(),
            name = name,
            description = desc,
            tags = tags,
            color = color,
            icon = icon,
            nutrition = mapOf(Nutrient.CALORIES to kcal.toFloat())
        )

        return listOf(
            meal("Oatmeal & Berries",     350, "Rolled oats, almond milk, strawberries.",  listOf("Breakfast"),               Vivid8, Icons.Filled.BreakfastDining),
            meal("Greek Yogurt",          200, "Low fat yogurt with honey.",               listOf("Breakfast"),               Vivid7, Icons.Filled.BreakfastDining),
            meal("Grilled Chicken Salad", 450, "Mixed greens, chicken breast.",            listOf("Lunch"),                   Vivid6, Icons.Filled.LunchDining),
            meal("Quinoa Bowl",           400, "Quinoa, roasted veggies, chickpeas.",      listOf("Lunch"),                   Vivid5, Icons.Filled.LunchDining),
            meal("Salmon & Asparagus",    500, "Baked salmon with grilled asparagus.",     listOf("Dinner"),                  Vivid4, Icons.Filled.DinnerDining),
            meal("Protein Shake",         150, "Whey protein with water.",                 listOf("Snack"),                   Vivid3, Icons.Filled.Restaurant),
            meal("Apple & Peanut Butter", 180, "Sliced apple with 1 tbsp peanut butter.", listOf("Snack"),                   Vivid2, Icons.Filled.Restaurant),
            meal("Egg",                    70, "1 large boiled egg.",                      listOf("Breakfast", "Ingredient"), Vivid1, Icons.Filled.Egg),
            meal("Chicken Breast",        165, "100g grilled chicken breast.",             listOf("Lunch",    "Ingredient"),  Vivid2, Icons.Filled.Restaurant),
            meal("Almonds",               160, "28g of raw almonds.",                      listOf("Snack",    "Ingredient"),  Vivid3, Icons.Filled.Restaurant),
        )
    }
}

