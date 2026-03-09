package eric.bitria.minimalfit.ui.viewmodels.track

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.model.Track
import eric.bitria.minimalfit.data.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OutdoorActivitiesUiState(
    val activities: List<Track> = emptyList()
)

class TrackScreen(
    private val repository: TrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutdoorActivitiesUiState())
    val uiState: StateFlow<OutdoorActivitiesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update {
            OutdoorActivitiesUiState(activities = repository.getAllActivities())
        }
    }

    fun addActivity(activity: Track) {
        repository.addActivity(activity)
        refresh()
    }

    fun deleteActivity(id: String) {
        repository.deleteActivity(id)
        refresh()
    }
}
