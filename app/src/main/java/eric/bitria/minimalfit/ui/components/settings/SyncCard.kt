package eric.bitria.minimalfit.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.remote.sync.SyncLogEntry
import eric.bitria.minimalfit.ui.theme.Spacing
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SyncCard(
    isLoggedIn: Boolean,
    isEmailVerified: Boolean,
    isSyncing: Boolean,
    isAutoSyncEnabled: Boolean,
    onSyncClick: () -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    syncLogs: List<SyncLogEntry> = emptyList(),
    onClearLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val canSync = isLoggedIn && isEmailVerified
    var showLogs by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Icon(
                    imageVector = if (canSync) Icons.Default.CloudSync else Icons.Default.SyncDisabled,
                    contentDescription = null,
                    tint = if (canSync) colorScheme.primary else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Cloud Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = when {
                            canSync -> "Keep your data synced across devices."
                            isLoggedIn -> "Verify your email to enable cloud backup."
                            else -> "Sign in to enable cloud backup."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Auto-sync", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(text = "Automatically sync data in the background", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAutoSyncEnabled, onCheckedChange = onAutoSyncToggle, enabled = canSync)
            }

            Button(
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSync && !isSyncing,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSync) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                    contentColor = if (canSync) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimaryContainer, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = if (canSync) Icons.Default.CloudUpload else Icons.Default.SyncDisabled, contentDescription = null)
                }
                Spacer(modifier = Modifier.size(Spacing.s))
                Text(
                    text = when {
                        isSyncing -> "Syncing..."
                        canSync -> "Sync Now"
                        isLoggedIn -> "Verify Account first"
                        else -> "Log in first"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = colorScheme.outlineVariant)

            // ── Log console toggle ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showLogs = !showLogs }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                    Text(text = "Sync Logs", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
                    if (syncLogs.isNotEmpty()) {
                        Text(text = "(${syncLogs.size})", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
                Icon(
                    imageVector = if (showLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }

            if (showLogs) {
                val context = LocalContext.current
                SyncLogConsole(entries = syncLogs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = syncLogs.joinToString("\n") { entry ->
                                val local = entry.time.toLocalDateTime(TimeZone.currentSystemDefault())
                                val t = "%02d:%02d:%02d".format(local.hour, local.minute, local.second)
                                "$t  ${entry.tag}: ${entry.message}"
                            }
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Sync Logs", text))
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Copy Logs", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onClearLogs,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Clear Logs", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncLogConsole(entries: List<SyncLogEntry>) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.scrollToItem(entries.size - 1)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp, max = 240.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF0D1117))
            .padding(8.dp)
    ) {
        if (entries.isEmpty()) {
            Text(
                text = "No sync activity yet",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF8B949E)
            )
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(entries) { entry -> SyncLogRow(entry) }
            }
        }
    }
}

@Composable
private fun SyncLogRow(entry: SyncLogEntry) {
    val local = entry.time.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeStr = "%02d:%02d:%02d".format(local.hour, local.minute, local.second)
    val color = when (entry.level) {
        SyncLogEntry.Level.DEBUG -> Color(0xFF8B949E)
        SyncLogEntry.Level.WARN  -> Color(0xFFD29922)
        SyncLogEntry.Level.ERROR -> Color(0xFFF85149)
    }
    Text(
        text = "$timeStr  ${entry.tag}: ${entry.message}",
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2
    )
}
