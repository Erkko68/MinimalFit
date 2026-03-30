package eric.bitria.minimalfit.data.database

import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.data.entity.food.relations.DietMealCrossRef
import eric.bitria.minimalfit.data.entity.food.relations.MealIngredientCrossRef
import eric.bitria.minimalfit.data.entity.track.Track
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes

class DatabaseInitializer(private val db: AppDatabase) {

    suspend fun initializeMockData() = withContext(Dispatchers.IO) {
        val ingredientDao = db.ingredientDao()
        val mealDao = db.mealDao()
        val dietDao = db.dietDao()
        val trackDao = db.trackDao()

        // 1. Initialize Ingredients
        val ingredients = listOf(
            Ingredient(
                id = "ing-oats",
                name = "Oats",
                baseCalories = 389, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-blueberries",
                name = "Blueberries",
                baseCalories = 57, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-chicken",
                name = "Chicken Breast",
                baseCalories = 165, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-avocado",
                name = "Avocado",
                baseCalories = 160, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-bread",
                name = "Whole Wheat Bread",
                baseCalories = 250, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-protein-powder",
                name = "Whey Protein",
                baseCalories = 120, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-salmon",
                name = "Salmon Fillet",
                baseCalories = 208, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-asparagus",
                name = "Asparagus",
                baseCalories = 20, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-beef",
                name = "Lean Beef",
                baseCalories = 250, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            ),
            Ingredient(
                id = "ing-greek-yogurt",
                name = "Greek Yogurt",
                baseCalories = 59, // per 100g
                measurementUnit = MeasurementUnit.GRAMS
            )
        )
        ingredients.forEach { ingredientDao.insertIngredient(it) }

        // 2. Initialize Meals
        val meals = listOf(
            Meal(
                id = "meal-oatmeal-berries",
                name = "Oatmeal with Berries",
                description = "Healthy breakfast bowl with fiber and antioxidants.",
                imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop"
            ),
            Meal(
                id = "meal-grilled-chicken-salad",
                name = "Grilled Chicken Salad",
                description = "Balanced lunch with lean protein and greens.",
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop"
            ),
            Meal(
                id = "meal-avocado-toast",
                name = "Avocado Toast",
                description = "Simple breakfast with healthy fats.",
                imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop"
            ),
            Meal(
                id = "meal-protein-shake",
                name = "Post-Workout Shake",
                description = "Quick recovery protein shake."
            ),
            Meal(
                id = "meal-salmon-asparagus",
                name = "Salmon and Asparagus",
                description = "Omega-3 rich dinner.",
                imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop"
            )
        )
        meals.forEach { mealDao.insertMeal(it) }

        // Initialize Meal Ingredients
        val mealIngredients = listOf(
            MealIngredientCrossRef("meal-oatmeal-berries", "ing-oats", 50f),
            MealIngredientCrossRef("meal-oatmeal-berries", "ing-blueberries", 30f),
            MealIngredientCrossRef("meal-grilled-chicken-salad", "ing-chicken", 150f),
            MealIngredientCrossRef("meal-grilled-chicken-salad", "ing-avocado", 50f),
            MealIngredientCrossRef("meal-avocado-toast", "ing-bread", 60f),
            MealIngredientCrossRef("meal-avocado-toast", "ing-avocado", 40f),
            MealIngredientCrossRef("meal-protein-shake", "ing-protein-powder", 1f),
            MealIngredientCrossRef("meal-salmon-asparagus", "ing-salmon", 200f),
            MealIngredientCrossRef("meal-salmon-asparagus", "ing-asparagus", 100f)
        )
        mealIngredients.forEach { mealDao.insertMealIngredientCrossRef(it) }

        // 3. Initialize Diets
        val diets = listOf(
            Diet(
                id = "diet-keto",
                name = "Keto Diet",
                description = "High-fat, low-carb diet.",
                imageUrl = "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=2070&auto=format&fit=crop"
            ),
            Diet(
                id = "diet-vegan",
                name = "Vegan",
                description = "Excludes all animal products.",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop"
            )
        )
        diets.forEach { dietDao.insertDiet(it) }

        // 4. Initialize Tracks
        val eveningRunStartTime = Instant.parse("2026-03-09T16:30:00Z")
        val eveningRunPoints = listOf(
            TrackPoint(41.38879, 2.18992, eveningRunStartTime),
            TrackPoint(41.38950, 2.19120, Instant.parse("2026-03-09T16:31:00Z")),
            TrackPoint(41.39040, 2.19280, Instant.parse("2026-03-09T16:32:00Z")),
            TrackPoint(41.39150, 2.19450, Instant.parse("2026-03-09T16:33:00Z")),
            TrackPoint(41.39220, 2.19620, Instant.parse("2026-03-09T16:34:00Z")),
            TrackPoint(41.39180, 2.19800, Instant.parse("2026-03-09T16:35:00Z")),
            TrackPoint(41.39090, 2.19950, Instant.parse("2026-03-09T16:36:00Z")),
            TrackPoint(41.38970, 2.20050, Instant.parse("2026-03-09T16:37:00Z")),
            TrackPoint(41.38840, 2.19970, Instant.parse("2026-03-09T16:38:00Z")),
            TrackPoint(41.38720, 2.19820, Instant.parse("2026-03-09T16:39:00Z")),
            TrackPoint(41.38640, 2.19640, Instant.parse("2026-03-09T16:40:00Z")),
            TrackPoint(41.38590, 2.19450, Instant.parse("2026-03-09T16:41:00Z")),
            TrackPoint(41.38610, 2.19260, Instant.parse("2026-03-09T16:42:00Z")),
            TrackPoint(41.38700, 2.19090, Instant.parse("2026-03-09T16:43:00Z")),
            TrackPoint(41.38820, 2.18980, Instant.parse("2026-03-09T16:44:00Z")),
            TrackPoint(41.38879, 2.18992, Instant.parse("2026-03-09T16:45:00Z"))
        )

        val morningWalkStartTime = Instant.parse("2026-03-08T07:00:00Z")
        val morningWalkPoints = listOf(
            TrackPoint(41.40280, 2.15640, morningWalkStartTime),
            TrackPoint(41.40350, 2.15720, Instant.parse("2026-03-08T07:03:00Z")),
            TrackPoint(41.40440, 2.15810, Instant.parse("2026-03-08T07:06:00Z")),
            TrackPoint(41.40530, 2.15900, Instant.parse("2026-03-08T07:09:00Z")),
            TrackPoint(41.40600, 2.16020, Instant.parse("2026-03-08T07:12:00Z")),
            TrackPoint(41.40660, 2.16180, Instant.parse("2026-03-08T07:15:00Z")),
            TrackPoint(41.40600, 2.16310, Instant.parse("2026-03-08T07:18:00Z")),
            TrackPoint(41.40510, 2.16380, Instant.parse("2026-03-08T07:21:00Z")),
            TrackPoint(41.40410, 2.16300, Instant.parse("2026-03-08T07:24:00Z")),
            TrackPoint(41.40320, 2.16180, Instant.parse("2026-03-08T07:27:00Z")),
            TrackPoint(41.40280, 2.16050, Instant.parse("2026-03-08T07:30:00Z")),
            TrackPoint(41.40280, 2.15640, Instant.parse("2026-03-08T07:45:00Z"))
        )

        val tracks = listOf(
            Track(
                id = "1",
                startTime = eveningRunStartTime,
                endTime = eveningRunStartTime + 30.minutes,
                name = "Evening Run",
                distance = 5.2,
                pace = "5:46",
                routePoints = eveningRunPoints
            ),
            Track(
                id = "2",
                startTime = morningWalkStartTime,
                endTime = morningWalkStartTime + 45.minutes,
                name = "Morning Walk",
                distance = 3.1,
                pace = "14:31",
                routePoints = morningWalkPoints
            )
        )
        tracks.forEach { trackDao.insertTrack(it) }
    }
}
