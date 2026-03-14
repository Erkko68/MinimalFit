package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eric.bitria.minimalfit.ui.components.permission.RequireActivityRecognitionPermission
import eric.bitria.minimalfit.ui.components.permission.RequireLocationPermission
import eric.bitria.minimalfit.ui.components.track.map.LiveTrackingMap
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.track.TrackRecordingViewModel
import org.koin.androidx.compose.koinViewModel
import org.maplibre.android.geometry.LatLng

@Composable
fun TrackRecordingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackRecordingViewModel = koinViewModel()
) {
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var activityPermissionGranted by remember { mutableStateOf(false) }
    val permissionsGranted = locationPermissionGranted && activityPermissionGranted

    if (!permissionsGranted) {
        if (!locationPermissionGranted) {
            RequireLocationPermission(onPermissionResult = { isGranted ->
                if (isGranted) {
                    locationPermissionGranted = true
                } else {
                    onNavigateBack()
                }
            })
        } else {
            RequireActivityRecognitionPermission(onPermissionResult = { isGranted ->
                if (isGranted) {
                    activityPermissionGranted = true
                } else {
                    onNavigateBack()
                }
            })
        }
    } else {
        val routePoints by viewModel.routePoints.collectAsState()
        val currentLocation by viewModel.currentLocation.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            LiveTrackingMap(
                routePoints = routePoints,
                followLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
            )
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.m)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        }
    }
}
