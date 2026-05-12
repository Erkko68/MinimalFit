package eric.bitria.minimalfit.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun AccountManagementCard(
    onLogoutClick: () -> Unit,
    onReauthenticateAndDelete: (password: String) -> Unit,
    onResetPasswordClick: () -> Unit,
    deleteError: String? = null,
    onClearDeleteError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog) {
            password = ""
            onClearDeleteError()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogoutClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = {
                Column {
                    Text("Enter your password to permanently delete your account and all data. This cannot be undone.")
                    Spacer(Modifier.height(Spacing.m))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; onClearDeleteError() },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = deleteError != null,
                        supportingText = deleteError?.let { { Text(it, color = colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onReauthenticateAndDelete(password) },
                    enabled = password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error,
                        contentColor = colorScheme.onError
                    )
                ) { Text("Delete Account") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Close dialog on successful deletion (user becomes null → logged out)
    // The dialog will dismiss naturally since the screen rerenders

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow,
            contentColor = colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Button(
                    onClick = onResetPasswordClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Reset Password", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = Spacing.xs)
                    )
                    Text("Log Out", style = MaterialTheme.typography.labelMedium)
                }
            }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.errorContainer,
                    contentColor = colorScheme.onErrorContainer
                )
            ) {
                Text("Delete Account", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
