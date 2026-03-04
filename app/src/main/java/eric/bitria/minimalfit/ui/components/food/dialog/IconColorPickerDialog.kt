package eric.bitria.minimalfit.ui.components.food.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eric.bitria.minimalfit.ui.components.food.mealIcons
import eric.bitria.minimalfit.ui.theme.vividColors

@Composable
fun IconColorPickerDialog(
    currentIcon: ImageVector,
    currentColor: Color,
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (icon: ImageVector, color: Color, name: String) -> Unit
) {
    var selectedIcon  by remember { mutableStateOf(currentIcon) }
    var selectedColor by remember { mutableStateOf(currentColor) }
    var name          by remember { mutableStateOf(currentName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            val width = maxWidth

            Surface(
                shape = RoundedCornerShape(10),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(width * 0.05f)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(width * 0.04f))

                    // Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(width * 0.22f)
                            .clip(RoundedCornerShape(16))
                            .background(selectedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = selectedIcon,
                                contentDescription = null,
                                tint = selectedColor,
                                modifier = Modifier.size(width * 0.11f)
                            )
                            if (name.isNotBlank()) {
                                Spacer(Modifier.height(width * 0.015f))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = selectedColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(width * 0.04f))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(width * 0.01f))

                    // Color section
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(width * 0.025f))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(width * 0.025f)) {
                        items(vividColors) { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(width * 0.09f)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            width * 0.007f,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(width * 0.05f))

                    // Icon section
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(width * 0.025f))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(width * 0.02f),
                        verticalArrangement = Arrangement.spacedBy(width * 0.02f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = width * 0.65f)
                    ) {
                        items(mealIcons) { namedIcon ->
                            val isSelected = namedIcon.icon == selectedIcon
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected)
                                    selectedColor.copy(alpha = 0.18f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { selectedIcon = namedIcon.icon }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = namedIcon.icon,
                                        contentDescription = namedIcon.label,
                                        tint = if (isSelected)
                                            selectedColor
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(width * 0.06f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(width * 0.05f))

                    // Confirm button
                    Button(
                        onClick = { onConfirm(selectedIcon, selectedColor, name) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

