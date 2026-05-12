package eric.bitria.minimalfit.data.database

import androidx.room.TypeConverter
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromDuration(value: Duration?): Long? = value?.inWholeMilliseconds

    @TypeConverter
    fun toDuration(value: Long?): Duration? = value?.milliseconds

    @TypeConverter
    fun fromTrackPointList(value: List<TrackPoint>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toTrackPointList(value: String?): List<TrackPoint>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromIntSet(value: Set<Int>?): String = value?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntSet(value: String?): Set<Int> =
        if (value.isNullOrEmpty()) emptySet()
        else value.split(",").mapNotNull { it.toIntOrNull() }.toSet()

}
