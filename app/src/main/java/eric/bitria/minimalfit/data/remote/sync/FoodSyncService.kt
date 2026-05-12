package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.database.dao.DietDao
import eric.bitria.minimalfit.data.database.dao.IngredientDao
import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.data.entity.food.relations.DietMealCrossRef
import eric.bitria.minimalfit.data.entity.food.relations.MealIngredientCrossRef
import eric.bitria.minimalfit.data.entity.food.relations.MealLogMealCrossRef
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.firestore.FoodFirestoreDataSource
import eric.bitria.minimalfit.data.remote.firestore.dto.DietDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.DietMealDto
import eric.bitria.minimalfit.data.remote.firestore.dto.IngredientDto
import eric.bitria.minimalfit.data.remote.firestore.dto.MealDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.MealIngredientDto
import eric.bitria.minimalfit.data.remote.firestore.dto.MealLogDocument
import eric.bitria.minimalfit.data.remote.firestore.dto.MealLogMealDto
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Instant

class FoodSyncService(
    private val ingredientDao: IngredientDao,
    private val mealDao: MealDao,
    private val dietDao: DietDao,
    private val mealLogDao: MealLogDao,
    private val firestoreDataSource: FoodFirestoreDataSource,
    private val authRepository: AuthRepository,
    private val log: SyncLogStore
) {
    companion object {
        private const val TAG = "FoodSyncService"
    }

    // ── Global ingredients ──────────────────────────────────────────────────

    suspend fun syncGlobalIngredients(): Result<Unit> = runCatching {
        val remote = firestoreDataSource.fetchGlobalIngredients()
        remote.forEach { dto -> ingredientDao.insertIngredient(dto.toEntity()) }
        log.d(TAG, "Synced ${remote.size} global ingredients")
    }

    // ── User ingredients ────────────────────────────────────────────────────

    suspend fun backupUserIngredients(): Result<Unit> = runCatching {
        val uid = requireUid()
        val userIngredients = (ingredientDao.getIngredients().firstOrNull() ?: emptyList()).filter { !it.isGlobal }
        if (userIngredients.isEmpty()) return@runCatching
        firestoreDataSource.uploadUserIngredients(uid, userIngredients.map { it.toDto() })
        log.d(TAG, "Backed up ${userIngredients.size} user ingredients")
    }

    suspend fun restoreUserIngredients(): Result<Unit> = runCatching {
        val uid = requireUid()
        val remote = firestoreDataSource.downloadUserIngredients(uid)
        remote.forEach { dto -> ingredientDao.insertIngredient(dto.toEntity()) }
        log.d(TAG, "Restored ${remote.size} user ingredients")
    }

    // ── Meals ───────────────────────────────────────────────────────────────

    suspend fun backupMeals(): Result<Unit> = runCatching {
        val uid = requireUid()
        val meals = mealDao.getMeals().firstOrNull() ?: emptyList()
        if (meals.isEmpty()) return@runCatching
        val documents = meals.map { buildMealDocument(it) }
        firestoreDataSource.uploadMeals(uid, documents)
        log.d(TAG, "Backed up ${documents.size} meals")
    }

    suspend fun restoreMeals(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadMeals(uid)
        documents.forEach { doc -> restoreMealDocument(doc) }
        log.d(TAG, "Restored ${documents.size} meals")
    }

    // ── Diets ───────────────────────────────────────────────────────────────

    suspend fun backupDiets(): Result<Unit> = runCatching {
        val uid = requireUid()
        val diets = dietDao.getDiets().firstOrNull() ?: emptyList()
        if (diets.isEmpty()) return@runCatching
        val documents = diets.map { diet ->
            val crossRefs = dietDao.getMealsForDiet(diet.id).firstOrNull() ?: emptyList()
            val mealDtos = crossRefs.map { ref ->
                val meal = mealDao.getMeal(ref.mealId).firstOrNull()
                DietMealDto(meal = if (meal != null) buildMealDocument(meal) else MealDocument(id = ref.mealId), amount = ref.amount)
            }
            DietDocument(id = diet.id, name = diet.name, description = diet.description, imageUrl = diet.imageUrl, meals = mealDtos, updatedAt = diet.updatedAt.toString())
        }
        firestoreDataSource.uploadDiets(uid, documents)
        log.d(TAG, "Backed up ${documents.size} diets")
    }

    suspend fun restoreDiets(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadDiets(uid)
        documents.forEach { doc ->
            dietDao.insertDiet(Diet(id = doc.id, name = doc.name, description = doc.description, imageUrl = doc.imageUrl, updatedAt = parseInstant(doc.updatedAt)))
            doc.meals.forEach { dmDto ->
                restoreMealDocument(dmDto.meal)
                dietDao.insertDietMealCrossRef(DietMealCrossRef(dietId = doc.id, mealId = dmDto.meal.id, amount = dmDto.amount))
            }
        }
        log.d(TAG, "Restored ${documents.size} diets")
    }

    // ── Meal Logs ───────────────────────────────────────────────────────────

    suspend fun backupMealLogs(): Result<Unit> = runCatching {
        val uid = requireUid()
        val logs = mealLogDao.getMealLogs().firstOrNull() ?: emptyList()
        if (logs.isEmpty()) return@runCatching
        val documents = logs.map { log ->
            val crossRefs = mealLogDao.getMealsForMealLog(log.id).firstOrNull() ?: emptyList()
            val mealDtos = crossRefs.map { ref ->
                val meal = mealDao.getMeal(ref.mealId).firstOrNull()
                MealLogMealDto(meal = if (meal != null) buildMealDocument(meal) else MealDocument(id = ref.mealId), amount = ref.amount)
            }
            MealLogDocument(id = log.id, createdAt = log.createdAt.toString(), meals = mealDtos, updatedAt = log.updatedAt.toString())
        }
        firestoreDataSource.uploadMealLogs(uid, documents)
        log.d(TAG, "Backed up ${documents.size} meal logs")
    }

    suspend fun restoreMealLogs(): Result<Unit> = runCatching {
        val uid = requireUid()
        val documents = firestoreDataSource.downloadMealLogs(uid)
        documents.forEach { doc ->
            mealLogDao.insertMealLog(MealLog(id = doc.id, createdAt = parseInstant(doc.createdAt), updatedAt = parseInstant(doc.updatedAt)))
            doc.meals.forEach { mlmDto ->
                restoreMealDocument(mlmDto.meal)
                mealLogDao.insertMealLogMealCrossRef(MealLogMealCrossRef(mealLogId = doc.id, mealId = mlmDto.meal.id, amount = mlmDto.amount))
            }
        }
        log.d(TAG, "Restored ${documents.size} meal logs")
    }

    // ── Single-entity upload / delete ───────────────────────────────────────

    suspend fun uploadSingleIngredient(uid: String, ingredientId: String) {
        val ingredient = ingredientDao.getIngredient(ingredientId).firstOrNull() ?: return
        if (ingredient.isGlobal) return
        firestoreDataSource.uploadUserIngredients(uid, listOf(ingredient.toDto()))
        log.d(TAG, "Uploaded single ingredient $ingredientId")
    }

    suspend fun deleteSingleIngredient(uid: String, ingredientId: String) {
        firestoreDataSource.deleteUserIngredient(uid, ingredientId)
        log.d(TAG, "Deleted single ingredient $ingredientId")
    }

    suspend fun uploadSingleMeal(uid: String, mealId: String) {
        val meal = mealDao.getMeal(mealId).firstOrNull() ?: return
        firestoreDataSource.uploadMeals(uid, listOf(buildMealDocument(meal)))
        log.d(TAG, "Uploaded single meal $mealId")
    }

    suspend fun deleteSingleMeal(uid: String, mealId: String) {
        firestoreDataSource.deleteMeal(uid, mealId)
        log.d(TAG, "Deleted single meal $mealId")
    }

    suspend fun uploadSingleDiet(uid: String, dietId: String) {
        val diet = dietDao.getDiet(dietId).firstOrNull() ?: return
        val crossRefs = dietDao.getMealsForDiet(dietId).firstOrNull() ?: emptyList()
        val mealDtos = crossRefs.map { ref ->
            val meal = mealDao.getMeal(ref.mealId).firstOrNull()
            DietMealDto(meal = if (meal != null) buildMealDocument(meal) else MealDocument(id = ref.mealId), amount = ref.amount)
        }
        val document = DietDocument(id = diet.id, name = diet.name, description = diet.description, imageUrl = diet.imageUrl, meals = mealDtos, updatedAt = diet.updatedAt.toString())
        firestoreDataSource.uploadDiets(uid, listOf(document))
        log.d(TAG, "Uploaded single diet $dietId")
    }

    suspend fun deleteSingleDiet(uid: String, dietId: String) {
        firestoreDataSource.deleteDiet(uid, dietId)
        log.d(TAG, "Deleted single diet $dietId")
    }

    suspend fun uploadSingleMealLog(uid: String, mealLogId: String) {
        val entry = mealLogDao.getMealLog(mealLogId).firstOrNull() ?: return
        val crossRefs = mealLogDao.getMealsForMealLog(mealLogId).firstOrNull() ?: emptyList()
        val mealDtos = crossRefs.map { ref ->
            val meal = mealDao.getMeal(ref.mealId).firstOrNull()
            MealLogMealDto(meal = if (meal != null) buildMealDocument(meal) else MealDocument(id = ref.mealId), amount = ref.amount)
        }
        firestoreDataSource.uploadMealLogs(uid, listOf(MealLogDocument(id = entry.id, createdAt = entry.createdAt.toString(), meals = mealDtos, updatedAt = entry.updatedAt.toString())))
        log.d(TAG, "Uploaded single meal log $mealLogId")
    }

    suspend fun deleteSingleMealLog(uid: String, mealLogId: String) {
        firestoreDataSource.deleteMealLog(uid, mealLogId)
        log.d(TAG, "Deleted single meal log $mealLogId")
    }

    // ── Full sync ───────────────────────────────────────────────────────────

    suspend fun fullSync(): Result<Unit> = runCatching {
        syncGlobalIngredients().getOrThrow()
        backupUserIngredients().getOrThrow()
        backupMeals().getOrThrow()
        backupDiets().getOrThrow()
        backupMealLogs().getOrThrow()
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    internal suspend fun buildMealDocument(meal: Meal): MealDocument {
        val crossRefs = mealDao.getIngredientsForMeal(meal.id).firstOrNull() ?: emptyList()
        val ingredientDtos = crossRefs.map { ref ->
            val ingredient = ingredientDao.getIngredient(ref.ingredientId).firstOrNull()
            MealIngredientDto(ingredient = ingredient?.toDto() ?: IngredientDto(id = ref.ingredientId), amount = ref.amount)
        }
        return MealDocument(id = meal.id, name = meal.name, description = meal.description, imageUrl = meal.imageUrl, measurementUnit = meal.measurementUnit.name, ingredients = ingredientDtos, updatedAt = meal.updatedAt.toString())
    }

    private suspend fun restoreMealDocument(doc: MealDocument) {
        mealDao.insertMeal(Meal(id = doc.id, name = doc.name, description = doc.description, imageUrl = doc.imageUrl, measurementUnit = parseMeasurementUnit(doc.measurementUnit), updatedAt = parseInstant(doc.updatedAt)))
        doc.ingredients.forEach { miDto ->
            ingredientDao.insertIngredient(miDto.ingredient.toEntity())
            mealDao.insertMealIngredientCrossRef(MealIngredientCrossRef(mealId = doc.id, ingredientId = miDto.ingredient.id, amount = miDto.amount))
        }
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private fun Ingredient.toDto() = IngredientDto(id = id, name = name, baseCalories = baseCalories, measurementUnit = measurementUnit.name, imageUrl = imageUrl, isGlobal = isGlobal, creatorId = creatorId, updatedAt = updatedAt.toString())
    private fun IngredientDto.toEntity() = Ingredient(id = id, name = name, baseCalories = baseCalories, measurementUnit = parseMeasurementUnit(measurementUnit), imageUrl = imageUrl, isGlobal = isGlobal, creatorId = creatorId, updatedAt = parseInstant(updatedAt))

    private fun requireUid(): String = authRepository.currentUser.value?.uid ?: throw IllegalStateException("User not authenticated")
    private fun parseInstant(value: String): Instant = if (value.isBlank()) nowInstant() else try { Instant.parse(value) } catch (_: Exception) { nowInstant() }
    private fun parseMeasurementUnit(value: String): MeasurementUnit = try { MeasurementUnit.valueOf(value) } catch (_: Exception) { MeasurementUnit.GRAMS }
}
