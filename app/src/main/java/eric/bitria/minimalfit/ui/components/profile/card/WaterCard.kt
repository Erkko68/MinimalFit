package eric.bitria.minimalfit.ui.components.profile.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import eric.bitria.minimalfit.ui.components.profile.StatCardChip
import eric.bitria.minimalfit.ui.components.profile.StatCardLayout
import eric.bitria.minimalfit.ui.theme.Quaternary
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.profile.card.WaterViewModel

@Composable
fun WaterCard(
    modifier: Modifier = Modifier,
    viewModel: WaterViewModel = viewModel()
) {
    val intakeMl by viewModel.waterIntake.collectAsState()
    val goalMl by viewModel.waterGoal.collectAsState()
    
    val containerColor = Quaternary
    val contentColor = Color.White

    // Watermark Icon
    val watermarkPainter = rememberVectorPainter(Icons.Outlined.WaterDrop)
    val watermarkTint = contentColor.copy(alpha = 0.1f)

    StatCardLayout(
        title = "Water Intake",
        icon = Icons.Filled.WaterDrop,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        backgroundContent = {
            Canvas(modifier = Modifier.matchParentSize()) {
                val iconSize = size.height * 1.4f
                val xOffset = size.width - (iconSize * 0.75f)
                val yOffset = (size.height - iconSize) / 2f

                translate(left = xOffset, top = yOffset) {
                    with(watermarkPainter) {
                        draw(
                            size = Size(iconSize, iconSize),
                            colorFilter = tint(watermarkTint)
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
                val formattedIntake = remember(intakeMl) { "%,d".format(intakeMl) }
                Text(
                    text = formattedIntake,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = " ml",
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
                    text = "Goal: %,d ml".format(goalMl),
                    containerColor = contentColor.copy(alpha = 0.2f),
                    contentColor = contentColor
                )

                // The Add Button as a Chip
                StatCardChip(
                    text = "+250 ml",
                    containerColor = contentColor,
                    contentColor = containerColor,
                    modifier = Modifier.clickable { viewModel.addWater(250) }
                )
            }
        }
    }
}
