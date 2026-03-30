package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ExerciseProgressionUiState(
    val exercise: Exercise? = null,
    val dates: List<String> = emptyList(),
    val maxWeights: List<Float> = emptyList(),
    val setsHistory: List<Set> = emptyList(),
    val groupedHistory: Map<String, List<Set>> = emptyMap()
)

class ExerciseProgressionViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val setsWithSessionFlow = setRepository
        .getSetsForExercise(exerciseId)
        .flatMapLatest { sets ->
            if (sets.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    sets.map { set ->
                        setRepository.getSessionForSet(set.id).map { session -> set to session }
                    }
                ) { pairs ->
                    pairs.toList()
                }
            }
        }

    val uiState: StateFlow<ExerciseProgressionUiState> = combine(
        exerciseRepository.getExercises().map { list -> list.find { it.id == exerciseId } },
        setsWithSessionFlow
    ) { exercise, setsWithSession ->

        val validSets = setsWithSession.filter { it.second != null }
        val completedSets = validSets.filter { (set, _) -> set.isCompleted || set.weight > 0 }
        
        val timeZone = TimeZone.currentSystemDefault()

        // Group sets by date for history (descending)
        val grouped = completedSets
            .sortedByDescending { (_, session) -> session!!.startTime }
            .groupBy(
                keySelector = { (_, session) -> session!!.startTime.toLocalDateTime(timeZone).date.toString() },
                valueTransform = { (set, _) -> set }
            )

        // Calculate progression data (chronological)
        val chronologicalGroups = completedSets
            .groupBy { (_, session) -> session!!.startTime.toLocalDateTime(timeZone).date }
            .toSortedMap()

        val dates = chronologicalGroups.keys.map { it.toString() }
        val maxWeights = chronologicalGroups.values.map { sets ->
            sets.maxOfOrNull { (set, _) -> set.weight } ?: 0f
        }

        ExerciseProgressionUiState(
            exercise = exercise,
            dates = dates,
            maxWeights = maxWeights,
            setsHistory = completedSets.map { (set, _) -> set },
            groupedHistory = grouped
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseProgressionUiState()
    )
}
