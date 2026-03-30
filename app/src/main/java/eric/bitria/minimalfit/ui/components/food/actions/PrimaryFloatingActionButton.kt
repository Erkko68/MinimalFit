package eric.bitria.minimalfit.ui.components.food.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrimaryFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = ""
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(text) },
        modifier = modifier
    )
}