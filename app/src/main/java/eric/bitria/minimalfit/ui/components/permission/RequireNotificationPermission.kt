package eric.bitria.minimalfit.ui.components.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun RequireNotificationPermission(
    onPermissionResult: (Boolean) -> Unit
) {
    // Notification permission is only required for Android 13 (Tiramisu) and above
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
        return
    }

    val permission = Manifest.permission.POST_NOTIFICATIONS
    var showRationaleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    fun checkPermission() {
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            onPermissionResult(true)
            showRationaleDialog = false
        } else {
            showRationaleDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionResult(true)
                showRationaleDialog = false
            } else {
                showRationaleDialog = true
            }
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onPermissionResult(true)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    if (showRationaleDialog) {
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false

        PermissionDialog(
            title = "Notification Permission Required",
            text = "To show you the tracking progress in the background, we need permission to show notifications.",
            showSettingsButton = !shouldShowRationale,
            onDismiss = {
                showRationaleDialog = false
                onPermissionResult(false)
            },
            onConfirm = {
                showRationaleDialog = false
                if (shouldShowRationale) {
                    permissionLauncher.launch(permission)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                }
            }
        )
    }
}
