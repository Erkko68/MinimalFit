package eric.bitria.minimalfit.ui.components.profile.card

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eric.bitria.minimalfit.ui.components.profile.StatCardLayout
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.theme.toVerticalGradient
import eric.bitria.minimalfit.ui.viewmodels.profile.card.CalorieViewModel

@Composable
fun CalorieCard(
    modifier: Modifier = Modifier,
    viewModel: CalorieViewModel = viewModel()
) {
    val eaten by viewModel.eaten.collectAsState()
    val eatenGoal by viewModel.eatenGoal.collectAsState()
    val burned by viewModel.burned.collectAsState()
    val burnedGoal by viewModel.burnedGoal.collectAsState()

    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    val eatenProgress =
        (eaten.toFloat() / eatenGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val burnedProgress =
        (burned.toFloat() / burnedGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val eatenColor = MaterialTheme.colorScheme.primary
    val burnedColor = MaterialTheme.colorScheme.secondary

    // Gradients generated from base colors
    val eatenBrush = eatenColor.toVerticalGradient()
    val burnedBrush = burnedColor.toVerticalGradient()

    StatCardLayout(
        title = "Calorie Intake",
        icon = Icons.Filled.LocalFireDepartment,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        backgroundContent = {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = size.minDimension * 0.22f
                val spacing = strokeWidth * 1.25f

                val eatenRadius = size.minDimension * 0.9f
                val burnedRadius = eatenRadius - spacing

                // Paths
                val eatenPath = Path().apply {
                    moveTo(-strokeWidth, size.height - eatenRadius)
                    quadraticTo(
                        x1 = eatenRadius,
                        y1 = size.height - eatenRadius,
                        x2 = eatenRadius,
                        y2 = size.height + strokeWidth
                    )
                }

                val burnedPath = Path().apply {
                    moveTo(-strokeWidth, size.height - burnedRadius)
                    quadraticTo(
                        x1 = burnedRadius,
                        y1 = size.height - burnedRadius,
                        x2 = burnedRadius,
                        y2 = size.height + strokeWidth
                    )
                }

                // --- TRACKS (background arcs)
                drawPath(
                    path = eatenPath,
                    color = eatenColor.copy(alpha = 0.18f),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                drawPath(
                    path = burnedPath,
                    color = burnedColor.copy(alpha = 0.18f),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                val pathMeasure = PathMeasure()

                // --- EATEN PROGRESS
                pathMeasure.setPath(eatenPath, false)
                val eatenProgressPath = Path()
                pathMeasure.getSegment(
                    0f,
                    pathMeasure.length * eatenProgress,
                    eatenProgressPath
                )

                // Main gradient stroke
                drawPath(
                    path = eatenProgressPath,
                    brush = eatenBrush,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // --- BURNED PROGRESS
                pathMeasure.setPath(burnedPath, false)
                val burnedProgressPath = Path()
                pathMeasure.getSegment(
                    0f,
                    pathMeasure.length * burnedProgress,
                    burnedProgressPath
                )

                // Main gradient stroke
                drawPath(
                    path = burnedProgressPath,
                    brush = burnedBrush,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            horizontalAlignment = Alignment.End
        ) {
            // ===== EATEN =====
            Column(horizontalAlignment = Alignment.End) {
                val formattedEaten = remember(eaten) { "%,d".format(eaten) }
                val formattedGoal = remember(eatenGoal) { "%,d".format(eatenGoal) }

                Row {
                    Text(
                        text = formattedEaten,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = contentColor,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = " / $formattedGoal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        ) { append("Eaten") }
                        append(" kcal")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }

            // ===== BURNED =====
            Column(horizontalAlignment = Alignment.End) {
                val formattedBurned = remember(burned) { "%,d".format(burned) }
                val formattedBurnedGoal = remember(burnedGoal) { "%,d".format(burnedGoal) }

                Row {
                    Text(
                        text = formattedBurned,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor.copy(alpha = 0.9f),
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = " / $formattedBurnedGoal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        ) { append("Burned") }
                        append(" kcal")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
