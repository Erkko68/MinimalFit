package eric.bitria.minimalfit.ui.components.food.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eric.bitria.minimalfit.ui.components.food.search.FoodSearchContent
import eric.bitria.minimalfit.ui.viewmodels.SavedMeal

@Composable
fun AddFoodDialog(
    savedMeals: List<SavedMeal>,
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onMealSelected: (SavedMeal) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            val width = maxWidth
            val height = maxHeight

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Dialog header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = width * 0.05f,
                                end = width * 0.02f,
                                top = height * 0.03f,
                                bottom = height * 0.01f
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add a meal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    FoodSearchContent(
                        savedMeals = savedMeals,
                        availableTags = availableTags,
                        onMealClick = onMealSelected,
                        autoFocus = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

