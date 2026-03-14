package eric.bitria.minimalfit.ui.components.track.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.data.model.Track
import eric.bitria.minimalfit.ui.theme.Spacing
import java.time.format.DateTimeFormatter

@Composable
fun TrackCard(
    track: Track,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Content Column
            Column(
                modifier = Modifier.weight(0.65f), // Takes 65% of the row width
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // Date and Time
                val dateTime = "${track.date.format(DateTimeFormatter.ofPattern("MMM dd"))} • ${track.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary, // Using primary color for emphasis
                    fontWeight = FontWeight.Bold
                )

                // Name
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black, // Heavier weight for expressive hierarchy
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.s))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Distance", value = "${track.distance} km")
                    StatItem(label = "Time", value = track.duration.toString())
                    StatItem(label = "Pace", value = track.pace)
                }
            }

            Spacer(modifier = Modifier.width(Spacing.m))

            // Map Image Container
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
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