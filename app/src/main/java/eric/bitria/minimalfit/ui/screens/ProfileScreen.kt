package eric.bitria.minimalfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.components.profile.CalorieTrackerCard
import eric.bitria.minimalfit.ui.components.profile.GymPerformanceCard
import eric.bitria.minimalfit.ui.components.profile.MorningRunCard
import eric.bitria.minimalfit.ui.components.profile.ProfileHeader
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onSectionClick: (section: String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m)
            .padding(bottom = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        ProfileHeader(
            modifier = Modifier.fillMaxWidth()
        )

        CalorieTrackerCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        )

        GymPerformanceCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        )

        MorningRunCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        )
    }
}
