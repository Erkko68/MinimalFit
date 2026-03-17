package eric.bitria.minimalfit.data.entity.food.relations

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "diet_meal_cross_ref",
    primaryKeys = ["dietId", "mealId"]
)
data class DietMealCrossRef(
    val dietId: String,
    val mealId: String,
    val amount: Float = 1f
)
