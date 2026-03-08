package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.repository.JournalRepository
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
    val weeklyProgress: List<DailyCalorieData> = emptyList(),
    val savedMeals: List<Meal> = emptyList()
)

class FoodViewModel(
    private val journal: JournalRepository,
    private val foodDatabase: FoodDatabase
) : ViewModel() {

    val uiState: StateFlow<FoodUiState> = journal.logs
        .map { _ ->
            val days = journal.last7Days()
            FoodUiState(
                weeklyProgress = days.map { date ->
                    val log = journal.getLog(date)
                    DailyCalorieData(
                        dayLabel = journal.dayLabel(date),
                        dayNumber = date.dayOfMonth,
                        currentCalories = log.totalCalories,
                        goalCalories = log.calorieGoal
                    )
                },
                savedMeals = foodDatabase.meals
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FoodUiState(
                weeklyProgress = journal.last7Days().map { date ->
                    DailyCalorieData(
                        dayLabel = journal.dayLabel(date),
                        dayNumber = date.dayOfMonth,
                        currentCalories = 0,
                        goalCalories = 2500
                    )
                },
                savedMeals = foodDatabase.meals
            )
        )
}
