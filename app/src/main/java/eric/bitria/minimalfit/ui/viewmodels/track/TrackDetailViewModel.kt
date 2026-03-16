package eric.bitria.minimalfit.ui.viewmodels.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrackDetailUiState(
    val track: Track? = null
)

class TrackDetailViewModel(
    private val trackId: String,
    private val repository: TrackRepository
) : ViewModel() {

    val uiState: StateFlow<TrackDetailUiState> = repository
        .getTrackByIdFlow(trackId)
        .map { track -> TrackDetailUiState(track = track) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrackDetailUiState()
        )

    fun deleteTrack() {
        repository.deleteActivity(trackId)
    }

    fun updateTrackName(newName: String) {
        uiState.value.track?.let { track ->
            val updatedTrack = track.copy(name = newName)
            repository.updateActivity(updatedTrack)
        }
    }
}

