package eric.bitria.minimalfit.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eric.bitria.minimalfit.data.database.dao.DietDao
import eric.bitria.minimalfit.data.database.dao.GymDao
import eric.bitria.minimalfit.data.database.dao.IngredientDao
import eric.bitria.minimalfit.data.database.dao.MealDao
import eric.bitria.minimalfit.data.database.dao.MealLogDao
import eric.bitria.minimalfit.data.database.dao.TrackDao
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.entity.food.relations.DietMealCrossRef
import eric.bitria.minimalfit.data.entity.food.relations.MealIngredientCrossRef
import eric.bitria.minimalfit.data.entity.food.relations.MealLogMealCrossRef
import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionEntity
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import eric.bitria.minimalfit.data.entity.track.Track

@Database(
    entities = [
        Track::class,
        Ingredient::class,
        Meal::class,
        Diet::class,
        MealLog::class,
        MealIngredientCrossRef::class,
        DietMealCrossRef::class,
        MealLogMealCrossRef::class,
        GymExerciseEntity::class,
        GymSessionEntity::class,
        GymSetEntity::class
    ],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealDao(): MealDao
    abstract fun dietDao(): DietDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun gymDao(): GymDao
}
