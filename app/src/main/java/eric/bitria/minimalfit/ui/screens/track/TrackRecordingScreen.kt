package eric.bitria.minimalfit.ui.screens.track

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.requirements.permission.RequireActivityRecognitionPermission
import eric.bitria.minimalfit.ui.components.requirements.permission.RequireLocationPermission
import eric.bitria.minimalfit.ui.components.requirements.permission.RequireNotificationPermission
import eric.bitria.minimalfit.ui.components.requirements.settings.RequireLocationEnabledSetting
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
    onNavigateToDetail: (String) -> Unit,
    viewModel: TrackRecordingViewModel = koinViewModel()
) {
    ScreenConfiguration(
        bottomBar = false,
        quickActions = false,
        fullScreen = true
    )

    var locationPermissionGranted by remember { mutableStateOf(false) }
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var gpsSettingEnabled by remember { mutableStateOf(false) }

    if (!locationPermissionGranted) {
        RequireLocationPermission(onPermissionResult = { isGranted ->
            if (isGranted) locationPermissionGranted = true else {
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
        RequireLocationEnabledSetting(onResult = { isEnabled ->
            gpsSettingEnabled = isEnabled
            if (!isEnabled) {
                viewModel.stop()
                onNavigateBack()
            }
        })

        if (gpsSettingEnabled) {
            val uiState by viewModel.uiState.collectAsState()
            val isActive = uiState.recordingState != RecordingState.IDLE

            var showFinishDialog by remember { mutableStateOf(false) }

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

            LaunchedEffect(Unit) {
                viewModel.requestInitialLocation()
            }

            LaunchedEffect(uiState.currentLocation, isFollowingUser) {
                if (isFollowingUser) {
                    uiState.currentLocation?.let { location ->
                        cameraState.centerOnUser(LatLng(location.latitude, location.longitude))
                    }
                }
            }

            LaunchedEffect(cameraState.isCameraMoving, cameraState.moveReason) {
                if (cameraState.isCameraMoving && cameraState.moveReason == CameraMoveReason.GESTURE) {
                    isFollowingUser = false
                }
            }

            LaunchedEffect(pendingCameraAction, uiState.routePoints) {
                if (pendingCameraAction == TrackMapCameraAction.FitRoute) {
                    cameraState.fitRoute(uiState.routePoints)
                    pendingCameraAction = null
                }
            }

            LaunchedEffect(uiState.savedTrackId) {
                uiState.savedTrackId?.let { trackId ->
                    onNavigateToDetail(trackId)
                }
            }

            BackHandler(enabled = isActive) {
                showFinishDialog = true
            }

            Box(modifier = Modifier.fillMaxSize()) {

                TrackMap(
                    routePoints = uiState.routePoints,
                    currentLocation = uiState.currentLocation?.let {
                        Position(it.longitude, it.latitude)
                    },
                    cameraState = cameraState,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(Spacing.m)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (isActive) showFinishDialog = true else onNavigateBack()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

                FloatingStats(
                    distanceKm = uiState.distanceKm,
                    duration = uiState.duration,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = Spacing.m)
                )

                TrackingToolbar(
                    state = uiState.recordingState,
                    onStartResume = {
                        isFollowingUser = true
                        viewModel.startOrResume()
                    },
                    onPause = viewModel::pause,
                    onStop = { showFinishDialog = true },
                    onCenterOnUser = { isFollowingUser = true },
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

            if (showFinishDialog) {
                AlertDialog(
                    onDismissRequest = { showFinishDialog = false },
                    title = { Text("Finish track?") },
                    text = { Text("Do you want to finish and save this track?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showFinishDialog = false
                            viewModel.stop()
                        }) { Text("Finish") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFinishDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
