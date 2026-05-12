package eric.bitria.minimalfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eric.bitria.minimalfit.ui.theme.MinimalFitTheme

class MainActivity : ComponentActivity() {

    private var navigationIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        navigationIntent = intent
        setContent {
            MinimalFitTheme {
                App(
                    navigationIntent = navigationIntent,
                    onNavigationIntentConsumed = { navigationIntent = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navigationIntent = intent
    }

    companion object {
        const val EXTRA_OPEN_GYM_SESSION = "extra_open_gym_session"
    }
}
