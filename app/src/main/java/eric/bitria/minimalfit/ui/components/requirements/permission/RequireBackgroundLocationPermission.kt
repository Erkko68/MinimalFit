package eric.bitria.minimalfit.ui.components.requirements.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
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
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
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
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionResult(true)
        } else {
            showRationaleDialog = true
        }
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
                permissionLauncher.launch(permission)
            }
        )
    }
}
