package eric.bitria.minimalfit.data.entity.gym

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    @ColumnInfo(name = "days_of_week") val daysOfWeekStr: String = ""
) {
    @get:Ignore
    val daysOfWeek: kotlin.collections.Set<Int>
        get() = if (daysOfWeekStr.isBlank()) emptySet()
                else daysOfWeekStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()

    fun withDaysOfWeek(days: kotlin.collections.Set<Int>): Routine =
        copy(daysOfWeekStr = days.joinToString(","))
}
