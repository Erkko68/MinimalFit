package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "diets")
data class Diet(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val imageUrl: String? = null
)
