package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import eric.bitria.minimalfit.util.today

enum class QuickAction(
    val label: String,
    val icon: ImageVector,
    private val routeFactory: () -> Route?,
) {
    START_TRACK(
        "Start Track",
        Icons.AutoMirrored.Filled.DirectionsRun,
        routeFactory = { Route.TrackRecording }
    ),
    ADD_MEAL(
        "Register Today's Meal",
        Icons.Default.Restaurant,
        routeFactory = { Route.DailyLog(date = today().toString(), openSearch = true) }
    ),
    REGISTER_WORKOUT(
        "Register Workout",
        Icons.Default.FitnessCenter,
        routeFactory = { Route.IndoorActivities }
    );

    fun createRoute(): Route? = routeFactory()
}