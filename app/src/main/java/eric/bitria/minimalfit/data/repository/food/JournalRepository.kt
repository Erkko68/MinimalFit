package eric.bitria.minimalfit.data.repository.food

import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Repository for managing daily food logs.
 */
interface JournalRepository {

    /** Returns the logs within a specific time range. */
    fun getMealLogsInRange(start: Instant, end: Instant): Flow<List<MealLog>>

    suspend fun addMealLog(mealLog: MealLog)
    suspend fun removeMealLog(id: String)

    // Relation methods
    /** Returns all meals associated with a specific log entry. */
    fun getMealsForLog(mealLogId: String): Flow<List<Meal>>
    
    /** Returns the logged amount of a specific meal in a log entry. */
    fun getMealAmountInLog(mealLogId: String, mealId: String): Flow<Float>

    /** Returns the calories for a specific meal in a log entry, scaled by its amount. */
    fun getMealCaloriesInLog(mealLogId: String, mealId: String): Flow<Int>

    /** Returns the total calories for a log entry. */
    fun getLogCalories(mealLogId: String): Flow<Int>

    suspend fun addMealToLog(mealLogId: String, mealId: String, amount: Float)
}
