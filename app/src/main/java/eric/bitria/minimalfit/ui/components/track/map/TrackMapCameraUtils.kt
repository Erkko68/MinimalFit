package eric.bitria.minimalfit.ui.components.track.map

import androidx.compose.foundation.layout.PaddingValues
import eric.bitria.minimalfit.data.model.TrackPoint
import eric.bitria.minimalfit.ui.theme.Spacing
import org.maplibre.android.geometry.LatLng
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

sealed interface TrackMapCameraAction {
    data object CenterOnUser : TrackMapCameraAction
    data object FitRoute : TrackMapCameraAction
}

private fun LatLng.isValid(): Boolean = latitude != 0.0 || longitude != 0.0

fun routeBounds(routePoints: List<TrackPoint>): BoundingBox? {
    if (routePoints.isEmpty()) return null
    return BoundingBox(
        west = routePoints.minOf { it.longitude },
        south = routePoints.minOf { it.latitude },
        east = routePoints.maxOf { it.longitude },
        north = routePoints.maxOf { it.latitude }
    )
}

suspend fun CameraState.centerOnUser(location: LatLng, zoomLevel: Double = 20.0) {
    if (!location.isValid()) return
    animateTo(
        CameraPosition(
            target = Position(location.longitude, location.latitude),
            zoom = zoomLevel
        )
    )
}

suspend fun CameraState.fitRoute(
    routePoints: List<TrackPoint>,
    padding: PaddingValues = PaddingValues(Spacing.xxxl)
) {
    val bounds = routeBounds(routePoints) ?: return
    animateTo(boundingBox = bounds, padding = padding)
}

