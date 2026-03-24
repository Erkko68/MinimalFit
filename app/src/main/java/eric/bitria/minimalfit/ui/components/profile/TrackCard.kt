package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .drawBehind {
                val pathTint = contentColor.copy(alpha = 0.08f)
                val strokeWidth = size.height * 0.25f

                // Draw a smooth, winding route across the entire card
                val trackPath = Path().apply {
                    // Start off-screen on the left
                    moveTo(-size.width * 0.1f, size.height * 0.7f)

                    // First curve (swoops down then up)
                    quadraticTo(
                        x1 = size.width * 0.3f,
                        y1 = size.height * 1.3f,
                        x2 = size.width * 0.6f,
                        y2 = size.height * 0.5f
                    )

                    // Second curve (swoops up then exits right)
                    quadraticTo(
                        x1 = size.width * 0.8f,
                        y1 = -size.height * 0.1f,
                        x2 = size.width * 1.1f,
                        y2 = size.height * 0.6f
                    )
                }

                drawPath(
                    path = trackPath,
                    color = pathTint,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Text Column (Left)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
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
                        color = contentColor,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = " km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.padding(top = Spacing.xs)
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

            // Foreground Icon (Right)
            StatCardIcon(
                icon = Icons.AutoMirrored.Outlined.DirectionsRun,
                contentColor = contentColor
            )
        }
    }
}