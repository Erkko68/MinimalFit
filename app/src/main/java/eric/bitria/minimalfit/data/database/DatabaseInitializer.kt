package eric.bitria.minimalfit.data.database

import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.track.Track
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import eric.bitria.minimalfit.util.today
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlin.time.Duration.Companion.minutes

class DatabaseInitializer(private val db: AppDatabase) {

    suspend fun initializeMockData() = withContext(Dispatchers.IO) {
        val mealDao = db.mealDao()
        val dietDao = db.dietDao()
        val trackDao = db.trackDao()

        // 1. Initialize Meals
        val meals = listOf(
            Meal(
                id = "meal-oatmeal-berries",
                name = "Oatmeal with Berries",
                calories = 350,
                imageUrl = "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?q=80&w=500&auto=format&fit=crop",
                description = "Healthy breakfast bowl with fiber and antioxidants.",
                relatedMealIds = listOf("meal-greek-yogurt", "meal-protein-shake")
            ),
            Meal(
                id = "meal-grilled-chicken-salad",
                name = "Grilled Chicken Salad",
                calories = 450,
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=500&auto=format&fit=crop",
                description = "Balanced lunch with lean protein and greens.",
                relatedMealIds = listOf("meal-avocado-toast")
            ),
            Meal(
                id = "meal-protein-shake",
                name = "Protein Shake",
                calories = 200,
                description = "Post-workout recovery option.",
                relatedMealIds = listOf("meal-oatmeal-berries")
            ),
            Meal(
                id = "meal-salmon-asparagus",
                name = "Salmon and Asparagus",
                calories = 600,
                imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=500&auto=format&fit=crop",
                description = "Omega-3 rich dinner.",
                relatedMealIds = listOf("meal-beef-stir-fry")
            ),
            Meal(
                id = "meal-greek-yogurt",
                name = "Greek Yogurt",
                calories = 150,
                imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500&auto=format&fit=crop",
                description = "High-protein snack.",
                relatedMealIds = listOf("meal-oatmeal-berries")
            ),
            Meal(
                id = "meal-avocado-toast",
                name = "Avocado Toast",
                calories = 300,
                imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500&auto=format&fit=crop",
                description = "Simple breakfast with healthy fats.",
                relatedMealIds = listOf("meal-grilled-chicken-salad")
            ),
            Meal(
                id = "meal-beef-stir-fry",
                name = "Beef Stir Fry",
                calories = 550,
                imageUrl = "https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=500&auto=format&fit=crop",
                description = "Lunch option with veggies and protein.",
                relatedMealIds = listOf("meal-salmon-asparagus")
            )
        )
        meals.forEach { mealDao.insertMeal(it) }

        // 2. Initialize Diets
        val diets = listOf(
            Diet(
                id = "diet-keto",
                name = "Keto Diet",
                description = "High-fat, low-carb diet.",
                imageUrl = "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=2070&auto=format&fit=crop",
                relatedMealIds = listOf("meal-salmon-asparagus", "meal-beef-stir-fry")
            ),
            Diet(
                id = "diet-mediterranean",
                name = "Mediterranean",
                description = "Emphasis on plant-based foods and healthy fats.",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop",
                relatedMealIds = listOf("meal-grilled-chicken-salad", "meal-avocado-toast")
            ),
            Diet(
                id = "diet-vegan",
                name = "Vegan",
                description = "Excludes all animal products.",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=2070&auto=format&fit=crop",
                relatedMealIds = listOf("meal-oatmeal-berries", "meal-avocado-toast")
            ),
            Diet(
                id = "diet-paleo",
                name = "Paleo",
                description = "Focuses on foods similar to what might have been eaten during the Paleolithic era.",
                imageUrl = "https://images.unsplash.com/photo-1505576391880-b3f9d713dc4f?q=80&w=2070&auto=format&fit=crop",
                relatedMealIds = listOf("meal-beef-stir-fry")
            )
        )
        diets.forEach { dietDao.insertDiet(it) }

        // 3. Initialize Tracks
        val eveningRunPoints = listOf(
            TrackPoint(41.38879, 2.18992, Instant.parse("2026-03-09T16:30:00Z")),
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

        val morningWalkPoints = listOf(
            TrackPoint(41.40280, 2.15640, Instant.parse("2026-03-08T07:00:00Z")),
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
                date = today().minus(1, DateTimeUnit.DAY),
                time = LocalTime(18, 30),
                name = "Evening Run",
                distance = 5.2,
                duration = 30.minutes,
                pace = "5:46",
                routePoints = eveningRunPoints
            ),
            Track(
                id = "2",
                date = today().minus(2, DateTimeUnit.DAY),
                time = LocalTime(7, 0),
                name = "Morning Walk",
                distance = 3.1,
                duration = 45.minutes,
                pace = "14:31",
                routePoints = morningWalkPoints
            )
        )
        tracks.forEach { trackDao.insertTrack(it) }
    }
}
