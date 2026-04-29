package eric.bitria.minimalfit.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun VerificationAlertCard(
    onVerifyClick: () -> Unit,
    onReloadClick: () -> Unit,
    verificationCooldown: Int,
    showVerificationMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.errorContainer.copy(alpha = 0.3f),
            contentColor = colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.padding(end = Spacing.s)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Email not verified",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (showVerificationMessage) "Check your email or spam please." else "Verify to secure your account.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(
                    onClick = onVerifyClick,
                    enabled = verificationCooldown == 0
                ) {
                    Text(if (verificationCooldown > 0) "${verificationCooldown}s" else "Verify")
                }
            }
            TextButton(
                onClick = onReloadClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = Spacing.s, bottom = Spacing.xs)
            ) {
                Text("Refresh status", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
