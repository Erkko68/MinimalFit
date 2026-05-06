package eric.bitria.minimalfit.data.remote.sync

import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.gym.Exercise

interface SyncRepository {
    // Global data
    suspend fun getGlobalIngredients(): Result<List<Ingredient>>
    suspend fun getGlobalExercises(): Result<List<Exercise>>

    // User data backup/restore
    suspend fun uploadUserIngredients(userId: String, ingredients: List<Ingredient>): Result<Unit>
    suspend fun downloadUserIngredients(userId: String): Result<List<Ingredient>>

    suspend fun uploadUserMeals(userId: String, meals: List<Meal>): Result<Unit>
    suspend fun downloadUserMeals(userId: String): Result<List<Meal>>

    suspend fun uploadUserExercises(userId: String, exercises: List<Exercise>): Result<Unit>
    suspend fun downloadUserExercises(userId: String): Result<List<Exercise>>
}
