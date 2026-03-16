package eric.bitria.minimalfit.ui.viewmodels.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OutdoorActivitiesUiState(
    val activities: List<Track> = emptyList()
)

class TrackViewModel(
    private val repository: TrackRepository
) : ViewModel() {

    val uiState: StateFlow<OutdoorActivitiesUiState> = repository
        .getAllActivitiesFlow()
        .map { activities -> OutdoorActivitiesUiState(activities = activities) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OutdoorActivitiesUiState()
        )

    fun addActivity(activity: Track) {
        repository.addActivity(activity)
    }

    fun deleteActivity(id: String) {
        repository.deleteActivity(id)
    }
}
