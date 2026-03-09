package eric.bitria.minimalfit.ui.components.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import eric.bitria.minimalfit.ui.theme.Spacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Delete background ────────────────────────────────────────────────────────

@Composable
private fun SwipeDeleteBackground(
    offsetX: Float,
    cardWidth: Int,
    modifier: Modifier = Modifier
) {
    val threshold = cardWidth * 0.5f
    val progress = (kotlin.math.abs(offsetX) / threshold.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val alignment = if (offsetX > 0) Alignment.CenterStart else Alignment.CenterEnd

    // Dynamic background color that intensifies as user drags closer to threshold
    val backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(
        alpha = 0.6f + (progress * 0.4f)
    )

    val iconScale by animateFloatAsState(
        targetValue = when {
            progress >= 1f -> 1.4f
            progress > 0f -> 0.8f + progress * 0.4f
            else -> 0.5f
        },
        animationSpec = spring(
            dampingRatio = if (progress >= 1f) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
            stiffness = if (progress >= 1f) Spring.StiffnessLow else Spring.StiffnessMedium
        ),
        label = "DeleteIconScale"
    )

    // Icon rotation animation for expressiveness
    val iconRotation by animateFloatAsState(
        targetValue = if (progress >= 1f) 0f else (if (offsetX > 0) -15f else 15f) * (1f - progress),
        label = "IconRotation"
    )

    Box(
        modifier = modifier
            .background(backgroundColor)
            .padding(horizontal = Spacing.l),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .scale(iconScale)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

// ─── Swipeable wrapper ────────────────────────────────────────────────────────

@Composable
fun SwipeToDeleteCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    var cardWidth by remember { mutableIntStateOf(0) }
    var isDismissed by remember { mutableStateOf(false) }

    val dragProgress by remember {
        derivedStateOf {
            (kotlin.math.abs(offsetX.value) / cardWidth.coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }

    // Background alpha animates to 0 when the card is fully dismissed
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isDismissed) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "BackgroundFadeOut"
    )

    // Fire onDismiss after the slide-off and fade-out animations finish
    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            delay(300)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { cardWidth = it.width }
            .clip(MaterialTheme.shapes.extraLarge) // Default shape, can be overridden
            .graphicsLayer { alpha = backgroundAlpha }
    ) {
        // Background fills the same space as the card (matchParentSize = size of the Box
        // as determined by the card content below)
        SwipeDeleteBackground(
            offsetX = offsetX.value,
            cardWidth = cardWidth,
            modifier = Modifier.matchParentSize()
        )

        // Foreground card slides over the background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                    alpha = 1f - dragProgress * 0.3f
                    scaleX = 1f - dragProgress * 0.04f
                    scaleY = 1f - dragProgress * 0.04f
                }
                .pointerInput(Unit) {
                    val velocityTracker = VelocityTracker()
                    coroutineScope {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity().x
                                val threshold = cardWidth * 0.5f
                                val shouldDismiss =
                                    kotlin.math.abs(offsetX.value) > threshold ||
                                    kotlin.math.abs(velocity) > 1200f

                                if (shouldDismiss) {
                                    val targetOffset = if (offsetX.value > 0) {
                                        cardWidth.toFloat() * 2
                                    } else {
                                        -cardWidth.toFloat() * 2
                                    }
                                    isDismissed = true
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = targetOffset,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                } else {
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(200)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                velocityTracker.addPosition(
                                    change.uptimeMillis,
                                    change.position
                                )
                                launch {
                                    offsetX.snapTo(offsetX.value + dragAmount)
                                }
                            }
                        )
                    }
                }
        ) {
            content()
        }
    }
}
