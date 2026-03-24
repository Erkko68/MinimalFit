package eric.bitria.minimalfit.ui.viewmodels.profile.card

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GymViewModel : ViewModel() {
    private val _weight = MutableStateFlow("12,450")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _comparison = MutableStateFlow("+12% vs last week")
    val comparison: StateFlow<String> = _comparison.asStateFlow()
}

