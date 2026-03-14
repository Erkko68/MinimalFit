package eric.bitria.minimalfit.ui.components.track.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.data.model.TrackPoint
import org.maplibre.android.geometry.LatLng
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@Composable
fun LiveTrackingMap(
    routePoints: List<TrackPoint>,
    followLocation: LatLng,
    modifier: Modifier = Modifier
) {
    // 1. Initialize Camera State (start zoomed out slightly if no location yet)
    val cameraState = rememberCameraState(
        CameraPosition(
            target = Position(followLocation.longitude, followLocation.latitude),
            zoom = if (followLocation.latitude != 0.0 || followLocation.longitude != 0.0) 16.0 else 1.0
        )
    )

    // 2. Move Camera to follow location changes during live recording
    LaunchedEffect(followLocation) {
        if (followLocation.latitude != 0.0 || followLocation.longitude != 0.0) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(followLocation.longitude, followLocation.latitude),
                    zoom = 16.0
                )
            )
        }
    }

    // 3. Render the map
    TrackMap(
        cameraState = cameraState,
        routePoints = routePoints,
        modifier = modifier
    )
}