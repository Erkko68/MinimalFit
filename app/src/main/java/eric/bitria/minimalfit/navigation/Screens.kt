package eric.bitria.minimalfit.navigation

import kotlinx.serialization.Serializable

sealed class Screens {
    @Serializable
    data object Home
    
    @Serializable
    data object Settings
}
