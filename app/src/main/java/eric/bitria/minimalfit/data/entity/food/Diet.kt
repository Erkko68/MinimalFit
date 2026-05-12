package eric.bitria.minimalfit.data.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import java.util.UUID

@Serializable
@Entity(tableName = "diets")
data class Diet(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val imageUrl: String? = null,
    val updatedAt: Instant = nowInstant()
)
