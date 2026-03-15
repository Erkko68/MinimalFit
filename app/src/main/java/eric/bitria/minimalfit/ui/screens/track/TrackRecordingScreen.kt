package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import eric.bitria.minimalfit.ui.components.permission.RequireActivityRecognitionPermission
import eric.bitria.minimalfit.ui.components.permission.RequireBackgroundLocationPermission
import eric.bitria.minimalfit.ui.components.permission.RequireLocationPermission
import eric.bitria.minimalfit.ui.components.permission.RequireNotificationPermission
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

@Composable
fun TrackRecordingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackRecordingViewModel = koinViewModel()
) {
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var backgroundLocationPermissionGranted by remember { mutableStateOf(false) }
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }

    // 1. Permission Logic Chain
    if (!locationPermissionGranted) {
        RequireLocationPermission(onPermissionResult = { isGranted ->
            if (isGranted) locationPermissionGranted = true else onNavigateBack()
        })
    } else if (!backgroundLocationPermissionGranted) {
        RequireBackgroundLocationPermission(onPermissionResult = { isGranted ->
            if (isGranted) backgroundLocationPermissionGranted = true else onNavigateBack()
        })
    } else if (!activityPermissionGranted) {
        RequireActivityRecognitionPermission(onPermissionResult = { isGranted ->
            if (isGranted) activityPermissionGranted = true else onNavigateBack()
        })
    } else if (!notificationPermissionGranted) {
        RequireNotificationPermission(onPermissionResult = { isGranted ->
            if (isGranted) notificationPermissionGranted = true else onNavigateBack()
        })
    } else {
        // 2. Main View States
        val routePoints by viewModel.routePoints.collectAsState()
        val currentLocation by viewModel.currentLocation.collectAsState()
        val recordingState by viewModel.recordingState.collectAsState()
        val distanceKm by viewModel.distanceKm.collectAsState()
        val duration by viewModel.duration.collectAsState()

        val defaultLatLng = LatLng(0.0, 0.0)
        val currentLatLng = currentLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLatLng
        val cameraState = rememberCameraState(
            CameraPosition(
                target = Position(currentLatLng.longitude, currentLatLng.latitude),
                zoom = if (currentLocation != null) 16.0 else 1.0
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
        LaunchedEffect(currentLocation, isFollowingUser) {
            if (isFollowingUser) {
                currentLocation?.let { location ->
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
        LaunchedEffect(pendingCameraAction, routePoints) {
            if (pendingCameraAction == TrackMapCameraAction.FitRoute) {
                cameraState.fitRoute(routePoints)
                pendingCameraAction = null
            }
        }

        // 3. Main UI Layout
        Box(modifier = Modifier.fillMaxSize()) {

            // Map Layer
            TrackMap(
                routePoints = routePoints,
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
                    onClick = onNavigateBack,
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
                distanceKm = distanceKm,
                duration = duration,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = Spacing.m)
            )

            // Bottom Overlay: Floating Toolbar (Pill)
            TrackingToolbar(
                state = recordingState,
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
