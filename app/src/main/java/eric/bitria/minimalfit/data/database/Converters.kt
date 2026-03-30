package eric.bitria.minimalfit.data.database

import androidx.room.TypeConverter
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromTrackPointList(value: List<TrackPoint>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toTrackPointList(value: String?): List<TrackPoint>? = value?.let { Json.decodeFromString(it) }

}
