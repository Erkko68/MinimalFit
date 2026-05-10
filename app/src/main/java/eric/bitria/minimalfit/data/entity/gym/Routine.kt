package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
