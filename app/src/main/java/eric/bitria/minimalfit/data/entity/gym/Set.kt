package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sets")
data class Set(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val orderInSession: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Float? = null,
    val isCompleted: Boolean = false,
    val isWarmup: Boolean = false,
    val notes: String = ""
)

