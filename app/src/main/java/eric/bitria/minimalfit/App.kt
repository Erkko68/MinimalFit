package eric.bitria.minimalfit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.AppNavHost
import eric.bitria.minimalfit.navigation.LocalScreenConfig
import eric.bitria.minimalfit.navigation.QuickAction
import eric.bitria.minimalfit.navigation.ScreenConfigState
import eric.bitria.minimalfit.navigation.composables.BottomNavigationBar
import eric.bitria.minimalfit.navigation.composables.QuickActionButton

@Composable
fun App() {
    val navController = rememberNavController()
    
    val screenConfigState = remember { ScreenConfigState() }

    CompositionLocalProvider(LocalScreenConfig provides screenConfigState) {
        val config = screenConfigState.config
        val quickActionEntries = remember { QuickAction.entries }

        Scaffold(
            modifier = config.modifier.fillMaxSize(),
            topBar = config.topBar,
            snackbarHost = config.snackbarHost,
            floatingActionButton = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Floating Action Button centered
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        config.floatingActionButton()
                    }

                    // Quick Actions Button aligned to the right
                    if (config.quickActions) {
                        QuickActionButton(
                            actions = quickActionEntries,
                            onActionClick = { action ->
                                action.createRoute()?.let { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateEndPadding(LayoutDirection.Ltr))
                        )
                    }
                }
            },
            floatingActionButtonPosition = config.floatingActionButtonPosition,
            bottomBar = {
                if (config.bottomBar) {
                    BottomNavigationBar(navController)
                }
            },
            contentWindowInsets = config.contentWindowInsets ?: ScaffoldDefaults.contentWindowInsets
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .then(
                        if (config.fullScreen) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.padding(contentPadding)
                        }
                    ),
            ) {
                AppNavHost(
                    navController = navController
                )
            }
        }
    }
}