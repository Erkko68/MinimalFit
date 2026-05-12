package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.database.dao.TrackDao
import eric.bitria.minimalfit.data.entity.track.Track
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.firestore.TrackFirestoreDataSource
import eric.bitria.minimalfit.data.remote.firestore.dto.TrackDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.TrackPointDto
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class TrackSyncService(
    private val trackDao: TrackDao,
    private val firestoreDataSource: TrackFirestoreDataSource,
    private val authRepository: AuthRepository,
    private val log: SyncLogStore
) {
    companion object {
        private const val TAG = "TrackSyncService"
    }

    suspend fun backupTracks(): Result<Unit> = runCatching {
        val uid = requireUid()
        val tracks = trackDao.getTracks().firstOrNull() ?: emptyList()
        if (tracks.isEmpty()) return@runCatching
        val documents = tracks.map { it.toDocument() }
        firestoreDataSource.uploadTracks(uid, documents)
        log.d(TAG, "Backed up ${documents.size} tracks")
    }

    suspend fun restoreTracks(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadTracks(uid)
        documents.forEach { doc -> trackDao.insertTrack(doc.toEntity()) }
        log.d(TAG, "Restored ${documents.size} tracks")
    }

    suspend fun uploadSingleTrack(uid: String, trackId: String) {
        val track = trackDao.getTrack(trackId).firstOrNull() ?: return
        firestoreDataSource.uploadTracks(uid, listOf(track.toDocument()))
        log.d(TAG, "Uploaded single track $trackId")
    }

    suspend fun deleteSingleTrack(uid: String, trackId: String) {
        firestoreDataSource.deleteTrack(uid, trackId)
        log.d(TAG, "Deleted single track $trackId")
    }

    suspend fun fullSync(): Result<Unit> = runCatching {
        backupTracks().getOrThrow()
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private fun Track.toDocument() = TrackDocument(id = id, startTime = startTime.toString(), durationMillis = duration.inWholeMilliseconds, name = name, distance = distance, pace = pace, routePoints = routePoints.map { TrackPointDto(it.latitude, it.longitude, it.timestamp.toString()) }, updatedAt = updatedAt.toString())
    private fun TrackDocument.toEntity() = Track(id = id, startTime = parseInstant(startTime), duration = durationMillis.milliseconds, name = name, distance = distance, pace = pace, routePoints = routePoints.map { TrackPoint(it.latitude, it.longitude, parseInstant(it.timestamp)) }, updatedAt = parseInstant(updatedAt))

    private fun requireUid(): String = authRepository.currentUser.value?.uid ?: throw IllegalStateException("User not authenticated")
    private fun parseInstant(value: String): Instant = if (value.isBlank()) nowInstant() else try { Instant.parse(value) } catch (_: Exception) { nowInstant() }
}
