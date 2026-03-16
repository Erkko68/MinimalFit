package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.food.DailyMealLog
import eric.bitria.minimalfit.data.model.food.Diet
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DailyCalorieData(
    val dayLabel: String,
    val dayNumber: Int,
    val currentCalories: Int,
    val goalCalories: Int
)

data class FoodUiState(
    val weeklyProgress: List<DailyCalorieData> = emptyList(),
    val diets: List<Diet> = emptyList(),
    val searchDietQuery: String = ""
)

class FoodViewModel(
    private val journal: JournalRepository,
    private val dietRepository: DietRepository,
    private val weekViewHelper: WeekViewHelper
) : ViewModel() {

    private val _searchDietQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FoodUiState> = _searchDietQuery
        .flatMapLatest { query ->
            val days = weekViewHelper.last7Days()
            val start = days.first()
            val end = days.last()
            
            combine(
                journal.getLogs(start, end),
                dietRepository.getDiets(query)
            ) { logsList, diets ->
                val logsMap = logsList.associateBy { it.date }
                buildUiState(logsMap, diets, query, days)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodUiState()
        )

    fun onSearchDietQueryChange(query: String) {
        _searchDietQuery.value = query
    }

    private fun buildUiState(
        logs: Map<LocalDate, DailyMealLog>,
        diets: List<Diet>,
        query: String,
        days: List<LocalDate>
    ): FoodUiState {
        return FoodUiState(
            weeklyProgress = days.map { date ->
                val log = logs[date] ?: DailyMealLog(date = date)
                DailyCalorieData(
                    dayLabel = weekViewHelper.dayLabel(date),
                    dayNumber = date.dayOfMonth,
                    currentCalories = log.totalCalories,
                    goalCalories = log.calorieGoal
                )
            },
            diets = diets,
            searchDietQuery = query
        )
    }
}
