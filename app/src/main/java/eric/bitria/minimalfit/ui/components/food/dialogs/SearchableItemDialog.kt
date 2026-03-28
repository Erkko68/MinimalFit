package eric.bitria.minimalfit.ui.components.food.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableItemDialog(
    title: String,
    placeholder: String,
    items: List<T>,
    itemKey: (T) -> Any,
    filter: (T, String) -> Boolean,
    onDismiss: () -> Unit,
    onCreateNew: ((String) -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, items) {
        items.filter { filter(it, searchQuery) }.take(50)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.m)
                .padding(bottom = Spacing.m)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.m)
            )
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(Spacing.m))
            
            if (onCreateNew != null && searchQuery.isNotBlank() && filtered.isEmpty()) {
                Button(
                    onClick = {
                        onCreateNew(searchQuery)
                        searchQuery = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.m)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = Spacing.xs))
                    Text("Create new '$searchQuery'")
                }
            }

            BoxWithConstraints {
                // Limit the maximum height of the list to 70% of the screen height
                val maxHeightConstraint = maxHeight * 0.7f
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeightConstraint),
                    contentPadding = PaddingValues(bottom = Spacing.xl)
                ) {
                    items(
                        items = filtered,
                        key = itemKey
                    ) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}
