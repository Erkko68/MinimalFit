package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.GymSetWithSession
import eric.bitria.minimalfit.data.repository.gym.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ExerciseProgressionUiState(
    val exercise: Exercise? = null,
    val dates: List<String> = emptyList(),
    val maxWeights: List<Float> = emptyList(),
    val setsHistory: List<Set> = emptyList(),
    val groupedHistory: Map<String, List<GymSetWithSession>> = emptyMap()
)

class ExerciseProgressionViewModel(
    private val exerciseId: String,
    private val repository: GymRepository
) : ViewModel() {

    val uiState: StateFlow<ExerciseProgressionUiState> = combine(
        repository.getExercises().map { list -> list.find { it.id == exerciseId } },
        repository.getSetsWithSessionForExercise(exerciseId)
    ) { exercise, setsWithSession ->
        
        val validSets = setsWithSession.filter { it.session != null }
        val completedSets = validSets.filter { it.set.isCompleted || it.set.weight > 0 }
        
        val timeZone = TimeZone.currentSystemDefault()
        
        // Group sets by date for history (descending)
        val grouped = completedSets
            .sortedByDescending { it.session!!.startTime }
            .groupBy { it.session!!.startTime.toLocalDateTime(timeZone).date.toString() }
        
        // Calculate progression data (chronological)
        val chronologicalGroups = completedSets
            .groupBy { it.session!!.startTime.toLocalDateTime(timeZone).date }
            .toSortedMap()
            
        val dates = chronologicalGroups.keys.map { it.toString() }
        val maxWeights = chronologicalGroups.values.map { sets ->
            sets.maxOfOrNull { it.set.weight } ?: 0f
        }
        
        ExerciseProgressionUiState(
            exercise = exercise,
            dates = dates,
            maxWeights = maxWeights,
            setsHistory = completedSets.map { it.set },
            groupedHistory = grouped
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseProgressionUiState()
    )
}
