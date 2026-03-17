package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseCalories: Int, // per 100g/100ml or per piece
    val measurementUnit: MeasurementUnit = MeasurementUnit.GRAMS,
    val imageUrl: String? = null
)
