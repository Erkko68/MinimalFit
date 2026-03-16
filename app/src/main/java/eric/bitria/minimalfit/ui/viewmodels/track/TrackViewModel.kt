package eric.bitria.minimalfit.ui.viewmodels.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OutdoorActivitiesUiState(
    val activities: List<Track> = emptyList(),
    val searchQuery: String = ""
)

class TrackViewModel(
    private val repository: TrackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OutdoorActivitiesUiState> = _searchQuery
        .flatMapLatest { query ->
            repository.getTracks(query).map { activities ->
                OutdoorActivitiesUiState(
                    activities = activities,
                    searchQuery = query
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OutdoorActivitiesUiState()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addActivity(activity: Track) {
        viewModelScope.launch {
            repository.addTrack(activity)
        }
    }

    fun deleteActivity(id: String) {
        viewModelScope.launch {
            repository.deleteTrack(id)
        }
    }
}
