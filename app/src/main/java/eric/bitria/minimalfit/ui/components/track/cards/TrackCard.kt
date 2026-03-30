package eric.bitria.minimalfit.ui.components.track.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.track.Track
import eric.bitria.minimalfit.ui.components.track.route.drawTrackRoute
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.hourMinute
import eric.bitria.minimalfit.util.shortMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TrackCard(
    track: Track,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val spacingM = with(LocalDensity.current) { Spacing.m.toPx() }
    val widerStroke = with(LocalDensity.current) { 8.dp.toPx() }

    val startDateTime = track.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val duration = track.endTime - track.startTime

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawTrackRoute(
                        points = track.routePoints,
                        color = primaryColor.copy(alpha = 0.12f),
                        strokeWidth = widerStroke,
                        padding = spacingM
                    )
                }
                .padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            val dateTime = "${startDateTime.date.shortMonthDay()} • ${startDateTime.time.hourMinute()}"
            Text(
                text = dateTime,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = track.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.Start
            ) {
                val distanceText = "%.2f".format(track.distance)

                StatItem(
                    label = "Distance",
                    value = "$distanceText km",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Time",
                    value = duration.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Pace",
                    value = track.pace,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
