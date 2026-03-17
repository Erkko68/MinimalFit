package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.util.nowInstant
import eric.bitria.minimalfit.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

data class MealLogUiModel(
    val log: MealLog,
    val meal: Meal
)

data class DailyLogUiState(
    val date: LocalDate,
    val logs: List<MealLogUiModel> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val calorieGoal: Int = 2500,
    val totalCalories: Int = 0,
    val showSearchDialog: Boolean = false,
    val searchMealQuery: String = ""
)

class DailyLogViewModel(
    private val date: LocalDate,
    private val journal: JournalRepository,
    private val foodCatalog: FoodCatalogRepository
) : ViewModel() {

    private val _showSearchDialog = MutableStateFlow(false)
    private val _searchMealQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DailyLogUiState> = combine(
        _showSearchDialog,
        _searchMealQuery
    ) { showDialog, query ->
        showDialog to query
    }.flatMapLatest { (showDialog, query) ->
        val timeZone = TimeZone.currentSystemDefault()
        val startOfDay = date.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val endOfDay = date.atTime(23, 59, 59, 999_999_999)
            .toInstant(timeZone).toEpochMilliseconds()

        journal.getMealLogsInRange(startOfDay, endOfDay).flatMapLatest { logs ->
            val logsFlow = if (logs.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(logs.map { log ->
                    foodCatalog.getMeal(log.mealId).map { meal ->
                        meal?.let { MealLogUiModel(log, it) }
                    }
                }) { it.filterNotNull() }
            }

            combine(
                logsFlow,
                foodCatalog.getMeals(query)
            ) { mealLogs, savedMeals ->
                DailyLogUiState(
                    date = date,
                    logs = mealLogs,
                    savedMeals = savedMeals,
                    totalCalories = mealLogs.sumOf { it.meal.totalCalories },
                    showSearchDialog = showDialog,
                    searchMealQuery = query
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyLogUiState(date = date)
    )

    fun openSearchDialog() {
        _showSearchDialog.value = true
    }

    fun dismissSearchDialog() {
        _showSearchDialog.value = false
    }

    fun onSearchMealQueryChange(query: String) {
        _searchMealQuery.value = query
    }

    fun addMeal(mealId: String, amount: Float) {
        viewModelScope.launch {
            val timestamp = if (date == today()) {
                System.currentTimeMillis()
            } else {
                date.atTime(12, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            }
            
            journal.addMealLog(
                MealLog(
                    mealId = mealId,
                    amount = amount,
                    createdAt = timestamp
                )
            )
        }
    }

    fun removeMealLog(logId: String) {
        viewModelScope.launch {
            journal.removeMealLog(logId)
        }
    }
}
