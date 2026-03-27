package eric.bitria.minimalfit.ui.components.track.route

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import kotlin.math.cos


fun DrawScope.drawTrackRoute(
    points: List<TrackPoint>,
    color: Color,
    strokeWidth: Float,
    padding: Float
) {
    if (points.size < 2) return

    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }

    val avgLat = Math.toRadians((minLat + maxLat) / 2.0)
    val cosLat = cos(avgLat).toFloat()

    val latRange = (maxLat - minLat).toFloat().coerceAtLeast(0.00001f)
    val lonRange = ((maxLon - minLon) * cosLat).toFloat().coerceAtLeast(0.00001f)

    val targetWidth = size.width * 0.6f
    val targetHeight = size.height

    val usableWidth = targetWidth - (padding * 2)
    val usableHeight = targetHeight - (padding * 2)

    val scale = minOf(usableWidth / lonRange, usableHeight / latRange)

    val drawnWidth = lonRange * scale
    val drawnHeight = latRange * scale

    // Aligned to the right
    val offsetX = size.width - drawnWidth - padding
    val offsetY = padding + (usableHeight - drawnHeight) / 2f

    val path = Path().apply {
        points.forEachIndexed { index, point ->
            val x = ((point.longitude - minLon) * cosLat).toFloat() * scale + offsetX
            val y = (maxLat - point.latitude).toFloat() * scale + offsetY
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
