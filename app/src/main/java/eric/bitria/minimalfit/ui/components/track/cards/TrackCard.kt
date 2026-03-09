package eric.bitria.minimalfit.ui.components.track.cards

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import eric.bitria.minimalfit.data.model.Track
import eric.bitria.minimalfit.ui.theme.Spacing
import java.time.format.DateTimeFormatter

@Composable
fun TrackCard(
    track: Track,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30),
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
                    .weight(0.35f) // Takes the remaining 35% of the width
                    .aspectRatio(1f), // Locks the height to equal the dynamic width
                contentAlignment = Alignment.Center
            ) {
                if (track.mapImageUrl != null) {
                    AsyncImage(
                        model = track.mapImageUrl,
                        contentDescription = "Map view of ${track.name}",
                        modifier = Modifier
                            .fillMaxSize() // Takes up the full relative bounding box
                            // 30% curve for a super organic, squircle-like shape
                            .clip(RoundedCornerShape(percent = 30)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback placeholder if the image is null so the layout doesn't collapse
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(percent = 30),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {}
                }
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