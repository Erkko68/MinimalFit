package eric.bitria.minimalfit.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.profile.card.CalorieCard
import eric.bitria.minimalfit.ui.components.profile.card.GymCard
import eric.bitria.minimalfit.ui.components.profile.card.TrackCard
import eric.bitria.minimalfit.ui.components.profile.card.WaterCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onSectionClick: (section: String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome\nBack!",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Thursday, October 24",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = true,
        quickActions = true
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
        contentPadding = PaddingValues(
            top = Spacing.m,
            bottom = Spacing.m
        )
    ) {
        item { CalorieCard() }
        item { GymCard() }
        item { WaterCard() }
        item { TrackCard() }
    }
}
