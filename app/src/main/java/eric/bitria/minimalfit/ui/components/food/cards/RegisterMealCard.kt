package eric.bitria.minimalfit.ui.components.food.cards

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.RegisterMealCard(
    date: LocalDate,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
    }
    // Use the ISO key as the stable shared-element key
    val dateKey = date.toString()
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth

        ElevatedCard(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "meal_container_$dateKey"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "meal_image_$dateKey"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clip(RoundedCornerShape(10))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(width * 0.15f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = width * 0.05f, vertical = width * 0.04f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to track nutrition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Meal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(width * 0.08f)
                    )
                }
            }
        }
    }
}

