package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.repository.JournalRepository
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DailyCalorieData(
    val dayLabel: String,
    val dayNumber: Int,
    val currentCalories: Int,
    val goalCalories: Int
)

data class FoodUiState(
    val weeklyProgress: List<DailyCalorieData> = emptyList()
)

class FoodViewModel(
    private val journal: JournalRepository,
    private val weekViewHelper: WeekViewHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    private fun buildInitialState(): FoodUiState {
        val days = weekViewHelper.last7Days()
        return FoodUiState(
            weeklyProgress = days.map { date ->
                val log = journal.getLog(date)
                DailyCalorieData(
                    dayLabel = weekViewHelper.dayLabel(date),
                    dayNumber = date.dayOfMonth,
                    currentCalories = log.totalCalories,
                    goalCalories = log.calorieGoal
                )
            }
        )
    }

    /** Call this to refresh the weekly data after mutations. */
    fun refresh() {
        _uiState.update { buildInitialState() }
    }
}
