package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
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

class ExerciseProgressionViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    val exercise: StateFlow<Exercise?> = exerciseRepository.getExercise(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val setsWithSessions = setRepository
        .getSetsForExercise(exerciseId)
        .flatMapLatest { sets ->
            if (sets.isEmpty()) flowOf(emptyList())
            else combine(
                sets.map { set ->
                    sessionRepository.getSession(set.sessionId).map { session -> set to session }
                }
            ) { it.toList() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressionData = setsWithSessions.map { pairs ->
        val validPairs = pairs.filter { it.second != null }
        val timeZone = TimeZone.currentSystemDefault()
        
        val chronologicalGroups = validPairs
            .groupBy { (_, session) -> session!!.startTime.toLocalDateTime(timeZone).date }
            .toSortedMap()

        val dates = chronologicalGroups.keys.map { it.toString() }
        val maxWeights = chronologicalGroups.values.map { sets ->
            sets.maxOfOrNull { (set, _) -> set.weight } ?: 0f
        }
        
        dates to maxWeights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<String>() to emptyList<Float>())

    val groupedHistory = setsWithSessions.map { pairs ->
        val timeZone = TimeZone.currentSystemDefault()
        pairs.filter { it.second != null }
            .sortedByDescending { it.second!!.startTime }
            .groupBy(
                keySelector = { it.second!!.startTime.toLocalDateTime(timeZone).date.toString() },
                valueTransform = { it.first }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
