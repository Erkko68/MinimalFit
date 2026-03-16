package eric.bitria.minimalfit.ui.components.food.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun MealCard(
    meal: Meal,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val hasImage = !meal.imageUrl.isNullOrEmpty()
    val cardShape = MaterialTheme.shapes.extraLarge
    val cardModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Card(
        modifier = cardModifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor =
                if (hasImage) MaterialTheme.colorScheme.surfaceContainerHigh
                else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (hasImage) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = meal.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color =
                        if (hasImage) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color =
                        if (hasImage) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(
                            horizontal = Spacing.m,
                            vertical = Spacing.xs
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
