package eric.bitria.minimalfit.data.remote.sync

import com.google.firebase.firestore.FirebaseFirestore
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

class FirestoreSyncRepository(
    private val firestore: FirebaseFirestore
) : SyncRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getGlobalIngredients(): Result<List<Ingredient>> = runCatching {
        val snapshot = firestore.collection("ingredients_global").get().await()
        snapshot.documents.map { doc ->
            json.decodeFromJsonElement<Ingredient>(json.parseToJsonElement(Json.encodeToString(doc.data)))
                .copy(id = doc.id, isGlobal = true)
        }
    }

    override suspend fun getGlobalExercises(): Result<List<Exercise>> = runCatching {
        val snapshot = firestore.collection("exercises_global").get().await()
        snapshot.documents.map { doc ->
            json.decodeFromJsonElement<Exercise>(json.parseToJsonElement(Json.encodeToString(doc.data)))
                .copy(id = doc.id, isGlobal = true)
        }
    }

    override suspend fun uploadUserIngredients(userId: String, ingredients: List<Ingredient>): Result<Unit> = runCatching {
        val batch = firestore.batch()
        val collection = firestore.collection("users").document(userId).collection("ingredients")
        
        ingredients.filter { !it.isGlobal }.forEach { ingredient ->
            val docRef = collection.document(ingredient.id)
            // Manual mapping for now to ensure Firestore compatibility
            val map = mapOf(
                "name" to ingredient.name,
                "baseCalories" to ingredient.baseCalories,
                "measurementUnit" to ingredient.measurementUnit.name,
                "imageUrl" to ingredient.imageUrl,
                "isGlobal" to false,
                "creatorId" to userId
            )
            batch.set(docRef, map)
        }
        batch.commit().await()
    }

    override suspend fun downloadUserIngredients(userId: String): Result<List<Ingredient>> = runCatching {
        val snapshot = firestore.collection("users").document(userId).collection("ingredients").get().await()
        snapshot.documents.map { doc ->
            json.decodeFromJsonElement<Ingredient>(json.parseToJsonElement(Json.encodeToString(doc.data)))
                .copy(id = doc.id, isGlobal = false, creatorId = userId)
        }
    }

    override suspend fun uploadUserMeals(userId: String, meals: List<Meal>): Result<Unit> = runCatching {
        val batch = firestore.batch()
        val collection = firestore.collection("users").document(userId).collection("meals")
        meals.forEach { meal ->
            val docRef = collection.document(meal.id)
            val map = mapOf(
                "name" to meal.name,
                "imageUrl" to meal.imageUrl
            )
            batch.set(docRef, map)
        }
        batch.commit().await()
    }

    override suspend fun downloadUserMeals(userId: String): Result<List<Meal>> = runCatching {
        val snapshot = firestore.collection("users").document(userId).collection("meals").get().await()
        snapshot.documents.map { doc ->
             json.decodeFromJsonElement<Meal>(json.parseToJsonElement(Json.encodeToString(doc.data)))
                .copy(id = doc.id)
        }
    }

    override suspend fun uploadUserExercises(userId: String, exercises: List<Exercise>): Result<Unit> = runCatching {
        val batch = firestore.batch()
        val collection = firestore.collection("users").document(userId).collection("exercises")
        exercises.filter { !it.isGlobal }.forEach { exercise ->
            val docRef = collection.document(exercise.id)
            val map = mapOf(
                "name" to exercise.name,
                "isBodyweight" to exercise.isBodyweight,
                "muscleGroup" to exercise.muscleGroup,
                "restSeconds" to exercise.restSeconds,
                "isGlobal" to false,
                "creatorId" to userId
            )
            batch.set(docRef, map)
        }
        batch.commit().await()
    }

    override suspend fun downloadUserExercises(userId: String): Result<List<Exercise>> = runCatching {
        val snapshot = firestore.collection("users").document(userId).collection("exercises").get().await()
        snapshot.documents.map { doc ->
            json.decodeFromJsonElement<Exercise>(json.parseToJsonElement(Json.encodeToString(doc.data)))
                .copy(id = doc.id, isGlobal = false, creatorId = userId)
        }
    }
}
