package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.model.food.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory implementation of the food catalog repository.
 */
class InMemoryFoodCatalogRepository(
    private val foodDatabase: FoodDatabase
) : FoodCatalogRepository {

    private val _meals = MutableStateFlow(foodDatabase.meals)

    override fun getMeals(query: String): Flow<List<Meal>> {
        return _meals.map { meals ->
            if (query.isBlank()) meals
            else meals.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    override fun getMeal(id: String): Flow<Meal?> =
        _meals.map { meals -> meals.find { it.id == id } }

    override suspend fun addMeal(meal: Meal) {
        val newMeal = if (meal.id.isBlank()) meal.copy(id = UUID.randomUUID().toString()) else meal
        _meals.value += newMeal
    }

    override suspend fun updateMeal(meal: Meal) {
        _meals.value = _meals.value.map {
            if (it.id == meal.id) meal else it
        }
    }

    override suspend fun deleteMeal(id: String) {
        _meals.value = _meals.value.filter { it.id != id }
    }
}
