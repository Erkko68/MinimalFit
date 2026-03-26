package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.permission.RequireActivityRecognitionPermission
import eric.bitria.minimalfit.ui.components.permission.RequireBackgroundLocationPermission
import eric.bitria.minimalfit.ui.components.permission.RequireLocationPermission
import eric.bitria.minimalfit.ui.components.permission.RequireNotificationPermission
import eric.bitria.minimalfit.ui.components.settings.RequireLocationEnabledSetting
import eric.bitria.minimalfit.ui.components.track.TrackingToolbar
import eric.bitria.minimalfit.ui.components.track.map.TrackMap
import eric.bitria.minimalfit.ui.components.track.map.TrackMapCameraAction
import eric.bitria.minimalfit.ui.components.track.map.centerOnUser
import eric.bitria.minimalfit.ui.components.track.map.fitRoute
import eric.bitria.minimalfit.ui.components.track.stats.FloatingStats
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.track.TrackRecordingViewModel
import org.koin.androidx.compose.koinViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackRecordingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackRecordingViewModel = koinViewModel()
) {
    ScreenConfiguration(
        bottomBar = false,
        quickActions = false
    )

    var locationPermissionGranted by remember { mutableStateOf(false) }
    var backgroundLocationPermissionGranted by remember { mutableStateOf(false) }
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var gpsSettingEnabled by remember { mutableStateOf(false) }

    // 1. Permission Logic Chain (Setup Phase)
    if (!locationPermissionGranted) {
        RequireLocationPermission(onPermissionResult = { isGranted ->
            if (isGranted) locationPermissionGranted = true else {
                onNavigateBack()
                viewModel.stop()
            }
        })
    } else if (!backgroundLocationPermissionGranted) {
        RequireBackgroundLocationPermission(onPermissionResult = { isGranted ->
            if (isGranted) backgroundLocationPermissionGranted = true else {
                onNavigateBack()
                viewModel.stop()
            }
        })
    } else if (!activityPermissionGranted) {
        RequireActivityRecognitionPermission(onPermissionResult = { isGranted ->
            if (isGranted) activityPermissionGranted = true else {
                onNavigateBack()
                viewModel.stop()
            }
        })
    } else if (!notificationPermissionGranted) {
        RequireNotificationPermission(onPermissionResult = { isGranted ->
            if (isGranted) notificationPermissionGranted = true else {
                onNavigateBack()
                viewModel.stop()
            }
        })
    } else {
        // We always want to monitor the GPS setting, even after it's initially enabled.
        // If it's disabled later, this component will show the dialog and notify onResult(false).
        RequireLocationEnabledSetting(onResult = { isEnabled ->
            gpsSettingEnabled = isEnabled
            if (!isEnabled) {
                viewModel.stop()
                onNavigateBack()
            }
        })

        if (gpsSettingEnabled) {
            val uiState by viewModel.uiState.collectAsState()

            val defaultLatLng = LatLng(0.0, 0.0)
            val currentLatLng = uiState.currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLatLng
            val cameraState = rememberCameraState(
                CameraPosition(
                    target = Position(currentLatLng.longitude, currentLatLng.latitude),
                    zoom = if (uiState.currentLocation != null) 16.0 else 1.0
                )
            )

            var isFollowingUser by remember { mutableStateOf(true) }
            var pendingCameraAction by remember { mutableStateOf<TrackMapCameraAction?>(null) }

            // Fetch the initial location map center once permissions are good
            LaunchedEffect(Unit) {
                viewModel.requestInitialLocation()
            }

            // --- Follow Mode Logic ---

            // 1. Center camera reactively when location updates or Follow Mode is toggled on
            LaunchedEffect(uiState.currentLocation, isFollowingUser) {
                if (isFollowingUser) {
                    uiState.currentLocation?.let { location ->
                        cameraState.centerOnUser(LatLng(location.latitude, location.longitude))
                    }
                }
            }

            // 2. Disable Follow Mode if the user manually drags the map
            LaunchedEffect(cameraState.isCameraMoving, cameraState.moveReason) {
                if (cameraState.isCameraMoving && cameraState.moveReason == CameraMoveReason.GESTURE) {
                    isFollowingUser = false
                }
            }

            // Handle Fit Route Action
            LaunchedEffect(pendingCameraAction, uiState.routePoints) {
                if (pendingCameraAction == TrackMapCameraAction.FitRoute) {
                    cameraState.fitRoute(uiState.routePoints)
                    pendingCameraAction = null
                }
            }

            // 3. Main UI Layout
            Box(modifier = Modifier.fillMaxSize()) {

                // Map Layer
                TrackMap(
                    routePoints = uiState.routePoints,
                    cameraState = cameraState,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Overlay: Floating Back Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(Spacing.m)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            onNavigateBack()
                            viewModel.stop() },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

                // Top Overlay: Floating Bold Stats
                FloatingStats(
                    distanceKm = uiState.distanceKm,
                    duration = uiState.duration,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = Spacing.m)
                )

                // Bottom Overlay: Floating Toolbar (Pill)
                TrackingToolbar(
                    state = uiState.recordingState,
                    onStartResume = {
                        isFollowingUser = true // Re-center map when they start
                        viewModel.startOrResume()
                    },
                    onPause = viewModel::pause,
                    onStop = viewModel::stop,
                    onCenterOnUser = {
                        isFollowingUser = true
                    },
                    onCenterOnRoute = {
                        isFollowingUser = false
                        pendingCameraAction = TrackMapCameraAction.FitRoute
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = Spacing.l)
                )
            }
        }
    }
}
