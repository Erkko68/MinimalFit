package eric.bitria.minimalfit.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.relations.SetSessionCrossRef
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
        Exercise::class,
        Session::class,
        Set::class,
        SetSessionCrossRef::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealDao(): MealDao
    abstract fun dietDao(): DietDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun gymDao(): GymDao

    companion object {
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `set_session_pairs_tmp` (
                        `setId` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        PRIMARY KEY(`setId`, `sessionId`)
                    )
                    """.trimIndent()
                )
                db.execSQL("INSERT OR REPLACE INTO `set_session_pairs_tmp` (`setId`, `sessionId`) SELECT `id`, `sessionId` FROM `sets`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sets_new` (
                        `id` TEXT NOT NULL,
                        `exerciseId` TEXT NOT NULL,
                        `orderInSession` INTEGER NOT NULL,
                        `weight` REAL NOT NULL,
                        `reps` INTEGER NOT NULL,
                        `rpe` REAL,
                        `isCompleted` INTEGER NOT NULL,
                        `isWarmup` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `sets_new` (`id`, `exerciseId`, `orderInSession`, `weight`, `reps`, `rpe`, `isCompleted`, `isWarmup`, `notes`)
                    SELECT `id`, `exerciseId`, `orderInSession`, `weight`, `reps`, `rpe`, `isCompleted`, `isWarmup`, `notes`
                    FROM `sets`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `sets`")
                db.execSQL("ALTER TABLE `sets_new` RENAME TO `sets`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `set_session_cross_ref` (
                        `setId` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        PRIMARY KEY(`setId`, `sessionId`),
                        FOREIGN KEY(`setId`) REFERENCES `sets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_set_session_cross_ref_setId` ON `set_session_cross_ref` (`setId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_session_cross_ref_sessionId` ON `set_session_cross_ref` (`sessionId`)")
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO `set_session_cross_ref` (`setId`, `sessionId`)
                    SELECT `setId`, `sessionId` FROM `set_session_pairs_tmp`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `set_session_pairs_tmp`")
            }
        }
    }
}
