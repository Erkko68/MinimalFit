package eric.bitria.minimalfit.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import eric.bitria.minimalfit.data.remote.firestore.dto.ExerciseDto
import eric.bitria.minimalfit.data.remote.firestore.dto.RoutineDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.SessionDocument
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Raw Firestore CRUD for gym-related collections.
 * Knows nothing about Room — operates purely on DTOs.
 */
class GymFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ── Global exercises ────────────────────────────────────────────────────

    suspend fun fetchGlobalExercises(): List<ExerciseDto> {
        val snapshot = firestore.collection("global")
            .document("exercises")
            .collection("items")
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val jsonObj = doc.data?.toJsonObject() ?: return@mapNotNull null
                json.decodeFromJsonElement<ExerciseDto>(jsonObj).copy(id = doc.id, isGlobal = true)
            } catch (_: Exception) { null }
        }
    }

    // ── User exercises ──────────────────────────────────────────────────────

    suspend fun uploadUserExercises(uid: String, exercises: List<ExerciseDto>) {
        val batch = firestore.batch()
        val col = firestore.collection("users").document(uid).collection("exercises")
        exercises.forEach { dto ->
            val map = json.encodeToJsonElement(dto).jsonObject.toFirestoreMap()
            batch.set(col.document(dto.id), map)
        }
        batch.commit().await()
    }

    suspend fun downloadUserExercises(uid: String): List<ExerciseDto> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("exercises").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val jsonObj = doc.data?.toJsonObject() ?: return@mapNotNull null
                json.decodeFromJsonElement<ExerciseDto>(jsonObj).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteUserExercise(uid: String, exerciseId: String) {
        firestore.collection("users").document(uid)
            .collection("exercises").document(exerciseId).delete().await()
    }

    // ── Routines ────────────────────────────────────────────────────────────

    suspend fun uploadRoutines(uid: String, routines: List<RoutineDocument>) {
        val batch = firestore.batch()
        val col = firestore.collection("users").document(uid).collection("routines")
        routines.forEach { doc ->
            val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
            batch.set(col.document(doc.id), map)
        }
        batch.commit().await()
    }

    suspend fun downloadRoutines(uid: String): List<RoutineDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("routines").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val jsonObj = doc.data?.toJsonObject() ?: return@mapNotNull null
                json.decodeFromJsonElement<RoutineDocument>(jsonObj).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteRoutine(uid: String, routineId: String) {
        firestore.collection("users").document(uid)
            .collection("routines").document(routineId).delete().await()
    }

    // ── Sessions ────────────────────────────────────────────────────────────

    suspend fun uploadSessions(uid: String, sessions: List<SessionDocument>) {
        // Firestore batch limit is 500 writes; chunk if needed.
        sessions.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            val col = firestore.collection("users").document(uid).collection("sessions")
            chunk.forEach { doc ->
                val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
                batch.set(col.document(doc.id), map)
            }
            batch.commit().await()
        }
    }

    suspend fun downloadSessions(uid: String): List<SessionDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("sessions").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val jsonObj = doc.data?.toJsonObject() ?: return@mapNotNull null
                json.decodeFromJsonElement<SessionDocument>(jsonObj).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteSession(uid: String, sessionId: String) {
        firestore.collection("users").document(uid)
            .collection("sessions").document(sessionId).delete().await()
    }
}
