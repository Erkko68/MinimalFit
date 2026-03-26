package eric.bitria.minimalfit.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

data class ScreenConfig(
    val modifier: Modifier = Modifier,
    val topBar: @Composable () -> Unit = {},
    val snackbarHost: @Composable () -> Unit = {},
    val floatingActionButton: @Composable () -> Unit = {},
    val floatingActionButtonPosition: FabPosition = FabPosition.Center,
    val contentWindowInsets: WindowInsets? = null,
    val bottomBar: Boolean = true,
    val quickActions: Boolean = true,
    val fullScreen: Boolean = false
)

@Stable
class ScreenConfigState {
    var config by mutableStateOf(ScreenConfig())
        private set

    fun updateConfig(newConfig: ScreenConfig) {
        if (config != newConfig) {
            config = newConfig
        }
    }
}

val LocalScreenConfig = staticCompositionLocalOf<ScreenConfigState> {
    error("No ScreenConfigState provided")
}

@Composable
fun ScreenConfiguration(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.Center,
    contentWindowInsets: WindowInsets? = null,
    bottomBar: Boolean = true,
    quickActions: Boolean = true,
    fullScreen: Boolean = false
) {
    val stateHolder = LocalScreenConfig.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Simply instantiate the config (let the state holder handle diffing)
    val currentConfig = ScreenConfig(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets,
        bottomBar = bottomBar,
        quickActions = quickActions,
        fullScreen = fullScreen
    )

    // 2. Keep a reference to the latest config without re-triggering DisposableEffect
    val latestConfig by rememberUpdatedState(currentConfig)

    // 3. Push updates during active recompositions
    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            stateHolder.updateConfig(currentConfig)
        }
    }

    // 4. Push updates when popping back to this screen from the backstack
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                stateHolder.updateConfig(latestConfig)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}