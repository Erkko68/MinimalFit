package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LoggedMeal(
    val mealId: String,
    val amount: Float // The portion size consumed (e.g., 200g, 0.5 pieces)
)

/**
 * A record representing all meals logged for a specific date.
 */
@Serializable
@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis(),
    val loggedMeals: List<LoggedMeal> = emptyList()
)
