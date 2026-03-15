package eric.bitria.minimalfit.ui.components.track.route

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.model.Track
import kotlin.math.cos

@Composable
fun TrackRouteCanvas(
    track: Track,
    modifier: Modifier = Modifier,
    routeColor: Color = Color.Blue,
    strokeWidth: Dp = 4.dp,
    padding: Dp = 24.dp
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val points = track.routePoints

        // We need at least two points to draw a line
        if (points.size < 2) return@Canvas

        // 1. Find the geographic bounding box
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLon = points.minOf { it.longitude }
        val maxLon = points.maxOf { it.longitude }

        // 2. Calculate local equirectangular projection adjustment
        // This ensures the track doesn't look stretched horizontally
        val avgLat = Math.toRadians((minLat + maxLat) / 2.0)
        val cosLat = cos(avgLat).toFloat()

        val latRange = (maxLat - minLat).toFloat()
        val lonRange = ((maxLon - minLon) * cosLat).toFloat()

        // Fallback for straight vertical or horizontal lines to prevent division by zero
        val safeLatRange = latRange.coerceAtLeast(0.00001f)
        val safeLonRange = lonRange.coerceAtLeast(0.00001f)

        // 3. Calculate the responsive scale
        val paddingPx = padding.toPx()
        val usableWidth = size.width - (paddingPx * 2)
        val usableHeight = size.height - (paddingPx * 2)

        val scaleX = usableWidth / safeLonRange
        val scaleY = usableHeight / safeLatRange
        val scale = minOf(scaleX, scaleY) // minOf guarantees the aspect ratio is maintained

        // 4. Calculate centering offsets so the route sits exactly in the middle
        val drawnWidth = safeLonRange * scale
        val drawnHeight = safeLatRange * scale
        val offsetX = paddingPx + (usableWidth - drawnWidth) / 2f
        val offsetY = paddingPx + (usableHeight - drawnHeight) / 2f

        // 5. Build the visual path
        val path = Path()
        points.forEachIndexed { index, point ->
            // X: Apply the latitude-adjusted longitude, scale it, and center it
            val x = ((point.longitude - minLon) * cosLat).toFloat() * scale + offsetX

            // Y: Invert latitude (so North is up), scale it, and center it
            val y = (maxLat - point.latitude).toFloat() * scale + offsetY

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // 6. Draw the completed path
        drawPath(
            path = path,
            color = routeColor,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}