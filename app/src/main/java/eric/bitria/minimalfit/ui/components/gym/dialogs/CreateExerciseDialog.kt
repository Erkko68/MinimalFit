package eric.bitria.minimalfit.ui.components.gym.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun CreateExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, muscleGroup: String?, isBodyweight: Boolean, restSeconds: Int) -> Unit,
    initialName: String = ""
) {
    var name by remember { mutableStateOf(initialName) }
    var muscleGroup by remember { mutableStateOf("") }
    var isBodyweight by remember { mutableStateOf(false) }
    var restSecondsText by remember { mutableStateOf("120") }
    val restSeconds = restSecondsText.toIntOrNull() ?: 120

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = { muscleGroup = it },
                    label = { Text("Muscle group") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { value ->
                        restSecondsText = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("Rest seconds") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Checkbox(
                        checked = isBodyweight,
                        onCheckedChange = { isBodyweight = it }
                    )
                    Text("Bodyweight exercise")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onCreate(
                        name,
                        muscleGroup.trim().takeIf { it.isNotBlank() },
                        isBodyweight,
                        restSeconds
                    )
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
