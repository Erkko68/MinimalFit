package eric.bitria.minimalfit.ui.components.food

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoMeals
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.data.model.Meal

@Composable
fun DailyMealsSection(meals: List<Meal>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Today's Meals",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
            modifier = Modifier.padding(vertical = Spacing.s)
        )
        if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NoMeals,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Spacing.xl + Spacing.m)
                    )
                    Text(
                        text = "No meals added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap + to log a meal for this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            MealsStaggeredGrid(
                meals = meals,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

