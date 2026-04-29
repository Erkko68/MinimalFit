package eric.bitria.minimalfit.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.requirements.settings.rememberInternetConnection
import eric.bitria.minimalfit.ui.components.settings.AccountManagementCard
import eric.bitria.minimalfit.ui.components.settings.LoginCard
import eric.bitria.minimalfit.ui.components.settings.NoInternetCard
import eric.bitria.minimalfit.ui.components.settings.ProfileCard
import eric.bitria.minimalfit.ui.components.settings.SyncCard
import eric.bitria.minimalfit.ui.components.settings.VerificationAlertCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected = rememberInternetConnection(showDialogOnLost = false)

    ScreenConfiguration(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = false,
        quickActions = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        if (!isConnected) {
            NoInternetCard()
        } else if (uiState.isLoggedIn && uiState.userProfile != null) {
            ProfileCard(userProfile = uiState.userProfile!!)

            if (!uiState.userProfile!!.isEmailVerified) {
                VerificationAlertCard(
                    onVerifyClick = { viewModel.sendVerificationEmail() },
                    onReloadClick = { viewModel.reloadUser() },
                    verificationCooldown = uiState.verificationCooldown,
                    showVerificationMessage = uiState.showVerificationMessage
                )
            }

            SyncCard(
                isLoggedIn = uiState.isLoggedIn,
                isEmailVerified = uiState.userProfile?.isEmailVerified == true,
                isSyncing = uiState.isSyncing,
                isAutoSyncEnabled = uiState.isAutoSyncEnabled,
                onSyncClick = { viewModel.uploadData() },
                onAutoSyncToggle = { viewModel.toggleAutoSync(it) }
            )

            AccountManagementCard(
                onLogoutClick = { viewModel.logout() },
                onDeleteAccountClick = { viewModel.deleteAccount() },
                onResetPasswordClick = { viewModel.sendPasswordReset() }
            )
        } else {
            LoginCard(onLoginClick = onLoginClick)

            SyncCard(
                isLoggedIn = uiState.isLoggedIn,
                isEmailVerified = uiState.userProfile?.isEmailVerified == true,
                isSyncing = uiState.isSyncing,
                isAutoSyncEnabled = uiState.isAutoSyncEnabled,
                onSyncClick = { viewModel.uploadData() },
                onAutoSyncToggle = { viewModel.toggleAutoSync(it) }
            )
        }
    }
}
