package eric.bitria.minimalfit.ui.components.profile.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eric.bitria.minimalfit.ui.components.profile.StatCardChip
import eric.bitria.minimalfit.ui.components.profile.StatCardLayout
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.profile.card.GymViewModel

@Composable
fun GymCard(
    modifier: Modifier = Modifier,
    viewModel: GymViewModel = viewModel()
) {
    val weight by viewModel.weight.collectAsState()
    val comparison by viewModel.comparison.collectAsState()

    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    // 1. Prepare the vector and color outside the layout phase
    val watermarkPainter = rememberVectorPainter(Icons.Outlined.FitnessCenter)
    val watermarkTint = contentColor.copy(alpha = 0.08f)

    StatCardLayout(
        title = "Gym Performance",
        icon = Icons.Filled.FitnessCenter,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        backgroundContent = {
            Canvas(modifier = Modifier.matchParentSize()) {
                val iconSize = size.height * 1.6f

                // Calculate position: Push it off the right edge and center vertically
                val xOffset = size.width - (iconSize * 0.8f)
                val yOffset = (size.height - iconSize) / 4f

                translate(left = xOffset, top = yOffset) {
                    with(watermarkPainter) {
                        draw(
                            size = Size(iconSize, iconSize),
                            colorFilter = ColorFilter.tint(watermarkTint)
                        )
                    }
                }
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
                    text = weight,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = " kg",
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
                    text = "This Week",
                    containerColor = contentColor.copy(alpha = 0.2f),
                    contentColor = contentColor
                )
                Text(
                    text = comparison,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
