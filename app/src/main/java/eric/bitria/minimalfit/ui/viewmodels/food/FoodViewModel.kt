package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.DailyLog
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    val uiState: StateFlow<FoodUiState> = journal
        .getAllLogsFlow()
        .map { logs -> buildUiState(logs) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodUiState()
        )

    private fun buildUiState(logs: Map<java.time.LocalDate, DailyLog>): FoodUiState {
        val days = weekViewHelper.last7Days()
        return FoodUiState(
            weeklyProgress = days.map { date ->
                val log = logs[date] ?: DailyLog(date = date)
                DailyCalorieData(
                    dayLabel = weekViewHelper.dayLabel(date),
                    dayNumber = date.dayOfMonth,
                    currentCalories = log.totalCalories,
                    goalCalories = log.calorieGoal
                )
            }
        )
    }
}
