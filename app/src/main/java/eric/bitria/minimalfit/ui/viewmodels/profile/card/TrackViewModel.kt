package eric.bitria.minimalfit.ui.viewmodels.profile.card

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackViewModel : ViewModel() {
    private val _distance = MutableStateFlow("8.42")
    val distance: StateFlow<String> = _distance.asStateFlow()

    private val _duration = MutableStateFlow("45m 12s")
    val duration: StateFlow<String> = _duration.asStateFlow()

    private val _pace = MutableStateFlow("5'20\" /km")
    val pace: StateFlow<String> = _pace.asStateFlow()
}

