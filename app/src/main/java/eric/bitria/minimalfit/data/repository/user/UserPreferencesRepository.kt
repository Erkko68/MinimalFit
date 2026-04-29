package eric.bitria.minimalfit.data.repository.user

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val onboardingCompleted: Flow<Boolean>
    val userName: Flow<String?>
    val themeMode: Flow<String>
    val isAutoSyncEnabled: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateUserName(name: String)
    suspend fun updateThemeMode(mode: String)
    suspend fun setAutoSyncEnabled(enabled: Boolean)
}
