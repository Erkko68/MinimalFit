package eric.bitria.minimalfit.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun LoginCard(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

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
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.large)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = colorScheme.onPrimaryContainer
                    )
                }

                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Account Sync (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Sign in to keep your data safe in the cloud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.weight(0.05f))
                Text(
                    text = "Sign In / Register",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
