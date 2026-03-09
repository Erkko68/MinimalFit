package eric.bitria.minimalfit.ui.viewmodels.track

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.model.Track
import eric.bitria.minimalfit.data.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackDetailUiState(
    val track: Track? = null
)

class TrackDetailViewModel(
    private val trackId: String,
    private val repository: TrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackDetailUiState())
    val uiState: StateFlow<TrackDetailUiState> = _uiState.asStateFlow()

    init {
        loadTrack()
    }

    private fun loadTrack() {
        val track = repository.getTrackById(trackId)
        _uiState.update { it.copy(track = track) }
    }

    fun deleteTrack() {
        _uiState.value.track?.let { track ->
            repository.deleteActivity(track.id)
        }
    }

    fun updateTrackName(newName: String) {
        _uiState.value.track?.let { track ->
            val updatedTrack = track.copy(name = newName)
            repository.updateActivity(updatedTrack)
            _uiState.update { it.copy(track = updatedTrack) }
        }
    }
}

