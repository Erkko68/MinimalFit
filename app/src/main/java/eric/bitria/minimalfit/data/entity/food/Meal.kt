package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class IngredientReference(
    val ingredientId: String,
    val amount: Float // e.g., 150.0 grams, 1.5 pieces
)

@Serializable
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val imageUrl: String? = null,
    val ingredients: List<IngredientReference> = emptyList(),
    val measurementUnit: MeasurementUnit = MeasurementUnit.GRAMS,
    
    // Calculated fields for UI, not persisted in DB
    val totalCalories: Int = 0,
    val totalAmount: Float = 0f
)
