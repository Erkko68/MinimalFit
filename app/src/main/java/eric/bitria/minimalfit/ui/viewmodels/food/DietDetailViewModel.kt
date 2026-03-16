package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.repository.food.DietRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DietDetailUiState(
    val diet: Diet? = null
)

class DietDetailViewModel(
    private val dietId: String,
    private val dietRepository: DietRepository
) : ViewModel() {

    val uiState: StateFlow<DietDetailUiState> = dietRepository.getDiet(dietId)
        .map { diet -> DietDetailUiState(diet = diet) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DietDetailUiState()
        )

    fun updateDiet(diet: Diet) {
        viewModelScope.launch {
            dietRepository.updateDiet(diet)
        }
    }

    fun deleteDiet() {
        viewModelScope.launch {
            dietRepository.deleteDiet(dietId)
        }
    }
}
