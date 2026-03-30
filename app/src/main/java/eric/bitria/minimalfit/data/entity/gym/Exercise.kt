package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isBodyweight: Boolean = false,
    val muscleGroup: String? = null,
)

