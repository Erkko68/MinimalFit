package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val calories: Int,
    val imageUrl: String? = null,
    val relatedMealIds: List<String> = emptyList()
)
