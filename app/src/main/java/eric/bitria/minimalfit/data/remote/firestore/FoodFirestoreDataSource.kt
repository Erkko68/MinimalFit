package eric.bitria.minimalfit.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import eric.bitria.minimalfit.data.remote.firestore.dto.DietDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.IngredientDto
import eric.bitria.minimalfit.data.remote.firestore.dto.MealDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.MealLogDocument
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Raw Firestore CRUD for food-related collections.
 * Knows nothing about Room — operates purely on DTOs.
 */
class FoodFirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ── Global ingredients ──────────────────────────────────────────────────

    suspend fun fetchGlobalIngredients(): List<IngredientDto> {
        val snapshot = firestore.collection("global")
            .document("ingredients")
            .collection("items")
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val raw = Json.encodeToString(doc.data)
                json.decodeFromString<IngredientDto>(raw).copy(id = doc.id, isGlobal = true)
            } catch (_: Exception) { null }
        }
    }

    // ── User ingredients ────────────────────────────────────────────────────

    suspend fun uploadUserIngredients(uid: String, ingredients: List<IngredientDto>) {
        val batch = firestore.batch()
        val col = firestore.collection("users").document(uid).collection("ingredients")
        ingredients.forEach { dto ->
            val map = json.encodeToJsonElement(dto).jsonObject.toFirestoreMap()
            batch.set(col.document(dto.id), map)
        }
        batch.commit().await()
    }

    suspend fun downloadUserIngredients(uid: String): List<IngredientDto> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("ingredients").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val raw = Json.encodeToString(doc.data)
                json.decodeFromString<IngredientDto>(raw).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteUserIngredient(uid: String, ingredientId: String) {
        firestore.collection("users").document(uid)
            .collection("ingredients").document(ingredientId).delete().await()
    }

    // ── Meals ───────────────────────────────────────────────────────────────

    suspend fun uploadMeals(uid: String, meals: List<MealDocument>) {
        val batch = firestore.batch()
        val col = firestore.collection("users").document(uid).collection("meals")
        meals.forEach { doc ->
            val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
            batch.set(col.document(doc.id), map)
        }
        batch.commit().await()
    }

    suspend fun downloadMeals(uid: String): List<MealDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("meals").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val raw = Json.encodeToString(doc.data)
                json.decodeFromString<MealDocument>(raw).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteMeal(uid: String, mealId: String) {
        firestore.collection("users").document(uid)
            .collection("meals").document(mealId).delete().await()
    }

    // ── Diets ───────────────────────────────────────────────────────────────

    suspend fun uploadDiets(uid: String, diets: List<DietDocument>) {
        val batch = firestore.batch()
        val col = firestore.collection("users").document(uid).collection("diets")
        diets.forEach { doc ->
            val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
            batch.set(col.document(doc.id), map)
        }
        batch.commit().await()
    }

    suspend fun downloadDiets(uid: String): List<DietDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("diets").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val raw = Json.encodeToString(doc.data)
                json.decodeFromString<DietDocument>(raw).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteDiet(uid: String, dietId: String) {
        firestore.collection("users").document(uid)
            .collection("diets").document(dietId).delete().await()
    }

    // ── Meal Logs ───────────────────────────────────────────────────────────

    suspend fun uploadMealLogs(uid: String, logs: List<MealLogDocument>) {
        logs.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            val col = firestore.collection("users").document(uid).collection("meal_logs")
            chunk.forEach { doc ->
                val map = json.encodeToJsonElement(doc).jsonObject.toFirestoreMap()
                batch.set(col.document(doc.id), map)
            }
            batch.commit().await()
        }
    }

    suspend fun downloadMealLogs(uid: String): List<MealLogDocument> {
        val snapshot = firestore.collection("users").document(uid)
            .collection("meal_logs").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val raw = Json.encodeToString(doc.data)
                json.decodeFromString<MealLogDocument>(raw).copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }

    suspend fun deleteMealLog(uid: String, mealLogId: String) {
        firestore.collection("users").document(uid)
            .collection("meal_logs").document(mealLogId).delete().await()
    }
}
