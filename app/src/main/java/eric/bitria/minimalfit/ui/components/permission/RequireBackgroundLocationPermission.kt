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
fun RequireBackgroundLocationPermission(
    onPermissionResult: (Boolean) -> Unit
) {
    // Background location is only required for Android 10 (Q) and above
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
        return
    }

    val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    var showRationaleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    fun checkPermission() {
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            onPermissionResult(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionResult(true)
            } else {
                // If denied, we show the dialog explaining why it's needed
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
        checkPermission()
        // Note: For background location, we typically show a rationale first
        // as per Google Play policies, but here we trigger the request if not granted.
        showRationaleDialog = true 
    }

    if (showRationaleDialog) {
        PermissionDialog(
            title = "Background Location Required",
            text = "To track your activity while the screen is off or while using other apps, please select 'Allow all the time' in the location settings.",
            showSettingsButton = true,
            onDismiss = {
                showRationaleDialog = false
                onPermissionResult(false)
            },
            onConfirm = {
                showRationaleDialog = false
                // On Android 11+, background location must be requested after foreground location
                // and it often requires taking the user to the settings page.
                permissionLauncher.launch(permission)
            }
        )
    }
}
