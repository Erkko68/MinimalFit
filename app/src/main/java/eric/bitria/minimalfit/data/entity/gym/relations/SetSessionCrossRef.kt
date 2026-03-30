package eric.bitria.minimalfit.data.entity.gym.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set

@Entity(
    tableName = "set_session_cross_ref",
    primaryKeys = ["setId", "sessionId"],
    foreignKeys = [
        ForeignKey(
            entity = Set::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["setId"], unique = true),
        Index(value = ["sessionId"])
    ]
)
data class SetSessionCrossRef(
    val setId: String,
    val sessionId: String
)

