package eric.bitria.minimalfit.ui.components.track.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import eric.bitria.minimalfit.ui.theme.Spacing
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.material3.ScaleBar
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

@Composable
fun TrackMap(
    cameraState: CameraState,
    routePoints: List<TrackPoint>,
    modifier: Modifier = Modifier
) {
    val styleState = rememberStyleState()

    Box(modifier = modifier.fillMaxSize()) {
        MaplibreMap(
            options = MapOptions(
                ornamentOptions = OrnamentOptions.AllDisabled
            ),
            baseStyle = BaseStyle.Uri(
                if (isSystemInDarkTheme()) "https://tiles.openfreemap.org/styles/dark"
                else "https://tiles.openfreemap.org/styles/liberty"
            ),
            cameraState = cameraState,
            styleState = styleState,
        ) {
            if (routePoints.size >= 2) {
                val lineCoordinates = routePoints.map {
                    Position(it.longitude, it.latitude)
                }

                val lineFeature = Feature(
                    geometry = LineString(lineCoordinates),
                    properties = JsonObject(emptyMap())
                )

                val routeSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(FeatureCollection(features = listOf(lineFeature)))
                )

                LineLayer(
                    id = "route-layer",
                    source = routeSource,
                    color = const(MaterialTheme.colorScheme.primary),
                    width = const(8.dp)
                )
            }
        }

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