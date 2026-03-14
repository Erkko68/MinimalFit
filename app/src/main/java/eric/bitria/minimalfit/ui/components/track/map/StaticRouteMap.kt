package eric.bitria.minimalfit.ui.components.track.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.data.model.TrackPoint
import eric.bitria.minimalfit.ui.theme.Spacing
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.BoundingBox

@Composable
fun StaticRouteMap(
    routePoints: List<TrackPoint>,
    modifier: Modifier = Modifier
) {
    // 1. Calculate Bounding Box using West, South, East, North
    val routeBounds = remember(routePoints) {
        if (routePoints.isEmpty()) null
        else {
            val minLat = routePoints.minOf { it.latitude } // South
            val maxLat = routePoints.maxOf { it.latitude } // North
            val minLon = routePoints.minOf { it.longitude } // West
            val maxLon = routePoints.maxOf { it.longitude } // East

            BoundingBox(
                west = minLon,
                south = minLat,
                east = maxLon,
                north = maxLat
            )
        }
    }

    // 2. Initialize Camera State
    val cameraState = rememberCameraState()

    // 3. Move Camera to fit bounds using the dedicated animateTo overload
    LaunchedEffect(routeBounds) {
        routeBounds?.let { bounds ->
            cameraState.animateTo(
                boundingBox = bounds,
                padding = PaddingValues(Spacing.xxxl)
            )
        }
    }

    // 4. Render the base map
    TrackMap(
        cameraState = cameraState,
        routePoints = routePoints,
        modifier = modifier
    )
}