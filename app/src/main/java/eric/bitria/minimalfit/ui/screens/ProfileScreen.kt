package eric.bitria.minimalfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.components.profile.card.CalorieCard
import eric.bitria.minimalfit.ui.components.profile.card.GymCard
import eric.bitria.minimalfit.ui.components.profile.card.TrackCard
import eric.bitria.minimalfit.ui.components.profile.card.WaterCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onSectionClick: (section: String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
        contentPadding = PaddingValues(bottom = Spacing.m)
    ) {

        item {
            // Header Texts
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome\nBack!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = MaterialTheme.typography.displayMedium.letterSpacing
                )
                Text(
                    text = "Thursday, October 24",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item { CalorieCard() }

        item { GymCard() }

        item { WaterCard() }

        item { TrackCard() }
    }
}