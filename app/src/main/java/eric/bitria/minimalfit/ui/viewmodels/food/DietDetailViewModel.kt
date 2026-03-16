package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.model.food.Diet
import eric.bitria.minimalfit.data.repository.food.DietRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DietDetailUiState(
    val diet: Diet? = null
)

class DietDetailViewModel(
    private val dietId: String,
    private val dietRepository: DietRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DietDetailUiState(diet = dietRepository.getDietById(dietId)))
    val uiState: StateFlow<DietDetailUiState> = _uiState.asStateFlow()
}
