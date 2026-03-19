package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun TrackCard(
    modifier: Modifier = Modifier,
    distance: String = "8.42",
    duration: String = "45m 12s",
    pace: String = "5'20\" /km"
) {
    val containerColor = MaterialTheme.colorScheme.secondary
    val contentColor = MaterialTheme.colorScheme.onSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .padding(Spacing.m)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            StatCardIcon(
                icon = Icons.AutoMirrored.Outlined.DirectionsRun,
                contentColor = contentColor
            )

            Column {
                Text(
                    text = "Morning Run",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.9f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = distance,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = " km",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.padding(top = Spacing.s)
                ) {
                    StatCardChip(
                        text = duration,
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                    StatCardChip(
                        text = pace,
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                }
            }
        }
    }
}
