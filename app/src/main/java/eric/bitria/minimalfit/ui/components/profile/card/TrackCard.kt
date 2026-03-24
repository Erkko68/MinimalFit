package eric.bitria.minimalfit.ui.components.profile.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eric.bitria.minimalfit.ui.components.profile.StatCardChip
import eric.bitria.minimalfit.ui.components.profile.StatCardLayout
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.profile.card.TrackViewModel

@Composable
fun TrackCard(
    modifier: Modifier = Modifier,
    viewModel: TrackViewModel = viewModel()
) {
    val distance by viewModel.distance.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val pace by viewModel.pace.collectAsState()

    val containerColor = MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = MaterialTheme.colorScheme.onTertiaryContainer

    StatCardLayout(
        title = "Morning Run",
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        backgroundContent = {
            Canvas(modifier = Modifier.matchParentSize()) {
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
        }
    ) {
        // Text Column (Left)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
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
    }
}
