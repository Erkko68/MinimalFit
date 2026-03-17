package eric.bitria.minimalfit.data.entity.food.relations

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "meal_log_meal_cross_ref",
    primaryKeys = ["mealLogId", "mealId"]
)
data class MealLogMealCrossRef(
    val mealLogId: String,
    val mealId: String,
    val amount: Float
)
