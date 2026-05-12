package eric.bitria.minimalfit.ui.screens.settings

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import eric.bitria.minimalfit.R
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
    val syncLogs by viewModel.syncLogs.collectAsState()
    val isConnected = rememberInternetConnection(showDialogOnLost = false)
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var googleSignInRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(googleSignInRequest) {
        if (googleSignInRequest == 0) return@LaunchedEffect
        val googleIdOption = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        )
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                viewModel.onGoogleLoginSuccess(googleCredential.idToken)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("SettingsScreen", "Google Sign-In cancelled", e)
            viewModel.onGoogleLoginError("Google sign-in cancelled")
        } catch (e: GetCredentialException) {
            Log.e("SettingsScreen", "Google Sign-In failed", e)
            viewModel.onGoogleLoginError(e.message ?: "Google sign-in was cancelled or no Google account is available.")
        } catch (e: Exception) {
            Log.e("SettingsScreen", "An unexpected Google Sign-In error occurred", e)
            viewModel.onGoogleLoginError(e.message ?: "Google sign-in failed")
        }
    }

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
                onAutoSyncToggle = { viewModel.toggleAutoSync(it) },
                syncLogs = syncLogs,
                onClearLogs = { viewModel.clearSyncLogs() }
            )

            AccountManagementCard(
                onLogoutClick = { viewModel.logout() },
                onDeleteAccountClick = { viewModel.deleteAccount() },
                onResetPasswordClick = { viewModel.sendPasswordReset() }
            )
        } else {
            LoginCard(
                onLoginClick = onLoginClick,
                onGoogleLoginClick = { googleSignInRequest++ },
                isGoogleSignInLoading = uiState.isGoogleSignInLoading
            )

            uiState.authError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            SyncCard(
                isLoggedIn = uiState.isLoggedIn,
                isEmailVerified = uiState.userProfile?.isEmailVerified == true,
                isSyncing = uiState.isSyncing,
                isAutoSyncEnabled = uiState.isAutoSyncEnabled,
                onSyncClick = { viewModel.uploadData() },
                onAutoSyncToggle = { viewModel.toggleAutoSync(it) },
                syncLogs = syncLogs,
                onClearLogs = { viewModel.clearSyncLogs() }
            )
        }
    }
}
