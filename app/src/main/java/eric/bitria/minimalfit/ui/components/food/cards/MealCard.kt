package eric.bitria.minimalfit.ui.components.food.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun MealCard(
    meal: Meal,
    modifier: Modifier = Modifier
) {
    val hasImage = !meal.imageUrl.isNullOrEmpty()
    val cardShape = if (hasImage) MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large

    Card(
        modifier = modifier,
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
