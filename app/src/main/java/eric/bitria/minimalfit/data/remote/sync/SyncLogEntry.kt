package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.util.nowInstant
import kotlin.time.Instant

data class SyncLogEntry(
    val time: Instant = nowInstant(),
    val tag: String,
    val message: String,
    val level: Level = Level.DEBUG
) {
    enum class Level { DEBUG, WARN, ERROR }
}
