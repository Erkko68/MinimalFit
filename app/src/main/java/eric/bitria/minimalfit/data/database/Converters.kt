package eric.bitria.minimalfit.data.database

import androidx.room.TypeConverter
import eric.bitria.minimalfit.data.entity.food.IngredientReference
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromDuration(value: Duration?): Long? = value?.inWholeNanoseconds

    @TypeConverter
    fun toDuration(value: Long?): Duration? = value?.nanoseconds

    @TypeConverter
    fun fromTrackPointList(value: List<TrackPoint>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toTrackPointList(value: String?): List<TrackPoint>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromIngredientList(value: List<IngredientReference>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toIngredientList(value: String?): List<IngredientReference>? = value?.let { Json.decodeFromString(it) }
}
