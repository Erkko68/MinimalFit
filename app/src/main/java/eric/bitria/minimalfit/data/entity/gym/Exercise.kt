package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isBodyweight: Boolean = false,
    val muscleGroup: String? = null,
    val restSeconds: Int = 120,
    val isGlobal: Boolean = false,
    val creatorId: String? = null
)

