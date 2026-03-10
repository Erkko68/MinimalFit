package eric.bitria.minimalfit.ui.components.track.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.theme.Spacing
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.material3.ScaleBar
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState

@Composable
fun TrackMap(){
    val cameraState = rememberCameraState()
    val styleState = rememberStyleState()

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            options = MapOptions(
                ornamentOptions = OrnamentOptions.AllDisabled
            ),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/dark"),
            cameraState = cameraState,
            styleState = styleState,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.l)
        ) {
            ScaleBar(
                cameraState.metersPerDpAtTarget,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}