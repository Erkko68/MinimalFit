package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.navigation.composables.ScreenTitle
import eric.bitria.minimalfit.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    ScreenTitle(title = "Gym Activities")
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = true,
        quickActions = true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.m)
    ){
        Text(
            text = "Indoor Activities Screen Content"
        )
    }
}
