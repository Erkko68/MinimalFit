package eric.bitria.minimalfit.ui.viewmodels.profile.card

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WaterViewModel : ViewModel() {
    private val _waterIntake = MutableStateFlow(0) // in ml
    val waterIntake: StateFlow<Int> = _waterIntake.asStateFlow()

    private val _waterGoal = MutableStateFlow(2500) // in ml (2.5L)
    val waterGoal: StateFlow<Int> = _waterGoal.asStateFlow()

    fun addWater(amountMl: Int = 250) {
        _waterIntake.value += amountMl
    }
}