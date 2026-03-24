package eric.bitria.minimalfit.ui.viewmodels.profile.card

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalorieViewModel : ViewModel() {
    private val _eaten = MutableStateFlow(1850)
    val eaten: StateFlow<Int> = _eaten.asStateFlow()

    private val _eatenGoal = MutableStateFlow(2500)
    val eatenGoal: StateFlow<Int> = _eatenGoal.asStateFlow()

    private val _burned = MutableStateFlow(450)
    val burned: StateFlow<Int> = _burned.asStateFlow()

    private val _burnedGoal = MutableStateFlow(600)
    val burnedGoal: StateFlow<Int> = _burnedGoal.asStateFlow()
}

