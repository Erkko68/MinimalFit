package eric.bitria.minimalfit.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

data class ScreenConfig(
    val modifier: Modifier = Modifier,
    val topBar: @Composable () -> Unit = {},
    val snackbarHost: @Composable () -> Unit = {},
    val floatingActionButton: @Composable () -> Unit = {},
    val floatingActionButtonPosition: FabPosition = FabPosition.Center,
    val contentWindowInsets: WindowInsets? = null,
    val bottomBar: Boolean = true,
    val quickActions: Boolean = true
)

class ScreenConfigState {
    var config by mutableStateOf(ScreenConfig())
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
    quickActions: Boolean = true
) {
    val state = LocalScreenConfig.current
    DisposableEffect(
        modifier, topBar, snackbarHost, floatingActionButton,
        floatingActionButtonPosition, contentWindowInsets, bottomBar, quickActions
    ) {
        state.config = ScreenConfig(
            modifier = modifier,
            topBar = topBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            contentWindowInsets = contentWindowInsets,
            bottomBar = bottomBar,
            quickActions = quickActions
        )
        onDispose { }
    }
}
