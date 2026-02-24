package eric.bitria.minimalfit.ui.components.food

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.viewmodels.SavedMeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMealCard(
    meal: SavedMeal,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = maxWidth
        
        OutlinedCard(
            onClick = { isExpanded = !isExpanded },
            shape = RoundedCornerShape(percent = 15),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(width * 0.04f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(width * 0.02f))
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = width * 0.02f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = meal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Category: ${meal.category}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = width * 0.01f)
                    )
                }
            }
        }
    }
}
