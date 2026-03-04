package eric.bitria.minimalfit.ui.components.food.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TagPickerDialog(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    onCreateTag: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var localSelected by remember { mutableStateOf(selectedTags) }
    val keyboard = LocalSoftwareKeyboardController.current

    val filteredTags = remember(query, availableTags) {
        if (query.isBlank()) availableTags
        else availableTags.filter { it.contains(query, ignoreCase = true) }
    }

    val canCreateNew = query.isNotBlank() &&
            availableTags.none { it.equals(query.trim(), ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f)
        ) {
            val width = maxWidth
            val height = maxHeight

            Surface(
                shape = RoundedCornerShape(10),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
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
                            text = "Tags",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Search + create row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = width * 0.04f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(width * 0.02f)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search or create…") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier.weight(1f)
                        )
                        if (canCreateNew) {
                            FilledTonalIconButton(
                                onClick = {
                                    val newTag = query.trim()
                                    onCreateTag(newTag)
                                    localSelected = localSelected + newTag
                                    query = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create tag")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(height * 0.015f))

                    // Tag list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = width * 0.04f),
                        verticalArrangement = Arrangement.spacedBy(height * 0.005f)
                    ) {
                        items(filteredTags) { tag ->
                            val isSelected = tag in localSelected
                            Surface(
                                onClick = {
                                    localSelected = if (isSelected)
                                        localSelected - tag
                                    else
                                        localSelected + tag
                                },
                                shape = RoundedCornerShape(12),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = width * 0.04f,
                                            vertical = height * 0.015f
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(width * 0.05f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Confirm button
                    Button(
                        onClick = { onConfirm(localSelected) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(width * 0.04f),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

