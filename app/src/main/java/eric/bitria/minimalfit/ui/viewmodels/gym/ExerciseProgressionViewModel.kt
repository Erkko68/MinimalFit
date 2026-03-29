package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import eric.bitria.minimalfit.data.entity.gym.GymSetWithSession
import eric.bitria.minimalfit.data.repository.gym.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ExerciseProgressionUiState(
    val exercise: GymExerciseEntity? = null,
    val dates: List<String> = emptyList(),
    val maxWeights: List<Float> = emptyList(),
    val setsHistory: List<GymSetEntity> = emptyList(),
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
        
        // Group sets by date directly
        val grouped = completedSets
            .sortedByDescending { it.session!!.date }
            .groupBy { it.session!!.date.toString() }
        
        ExerciseProgressionUiState(
            exercise = exercise,
            setsHistory = completedSets.map { it.set },
            groupedHistory = grouped
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseProgressionUiState()
    )
}
