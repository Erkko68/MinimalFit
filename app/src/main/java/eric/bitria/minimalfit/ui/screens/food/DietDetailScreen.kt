package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DietDetailScreen(
    dietId: Int,
    onBackClick: () -> Unit,
    viewModel: DietDetailViewModel = koinViewModel { parametersOf(dietId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val diet = uiState.diet

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            // Optional FAB placeholder
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = Spacing.m)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.s, bottom = Spacing.m)
            ) {
                // Back button pinned to the top left
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }

                // Top-center placeholder for diet name
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(text = diet?.name ?: "Diet Detail")
                }
            }
        }
    }
}