package eric.bitria.minimalfit.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import eric.bitria.minimalfit.data.remote.firestore.dto.TrackDocument
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Raw Firestore CRUD for track-related collections.
 * Knows nothing about Room — operates purely on DTOs.
 */
class TrackFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun uploadTracks(uid: String, tracks: List<TrackDocument>) {
        tracks.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            val col = firestore.collection("users").document(uid).collection("tracks")
            chunk.forEach { doc ->
                val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
                batch.set(col.document(doc.id), map)
            }
            batch.commit().await()
        }
    }

    suspend fun downloadTracks(uid: String): List<TrackDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("tracks").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val jsonObj = doc.data?.toJsonObject() ?: return@mapNotNull null
                json.decodeFromJsonElement<TrackDocument>(jsonObj).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteTrack(uid: String, trackId: String) {
        firestore.collection("users").document(uid)
            .collection("tracks").document(trackId).delete().await()
    }
}
