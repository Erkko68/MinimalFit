package eric.bitria.minimalfit.data.entity.food.relations

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "meal_ingredient_cross_ref",
    primaryKeys = ["mealId", "ingredientId"]
)
data class MealIngredientCrossRef(
    val mealId: String,
    val ingredientId: String,
    val amount: Float
)
