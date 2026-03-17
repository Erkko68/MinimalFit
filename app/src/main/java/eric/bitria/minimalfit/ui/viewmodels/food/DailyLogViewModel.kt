package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import java.util.UUID

data class MealLogUiModel(
    val logId: String,
    val meal: Meal,
    val calories: Int,
    val amount: Float
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
            if (logs.isEmpty()) {
                fetchSavedMeals(showDialog, query, emptyList())
            } else {
                val mealLogsFlows: List<Flow<List<MealLogUiModel>>> = logs.map { log ->
                    journal.getMealsForLog(log.id).flatMapLatest { meals ->
                        if (meals.isEmpty()) flowOf(emptyList())
                        else combine(meals.map { meal ->
                            combine(
                                journal.getMealAmountInLog(log.id, meal.id),
                                journal.getMealCaloriesInLog(log.id, meal.id)
                            ) { amount, calories ->
                                MealLogUiModel(log.id, meal, calories, amount)
                            }
                        }) { it.toList() }
                    }
                }
                
                combine(mealLogsFlows) { arrays ->
                    arrays.flatMap { it }
                }.flatMapLatest { mealLogs ->
                    fetchSavedMeals(showDialog, query, mealLogs)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyLogUiState(date = date)
    )

    private fun fetchSavedMeals(showDialog: Boolean, query: String, mealLogs: List<MealLogUiModel>) =
        foodCatalog.getMeals(query).map { savedMeals ->
            DailyLogUiState(
                date = date,
                logs = mealLogs,
                savedMeals = savedMeals,
                totalCalories = mealLogs.sumOf { it.calories },
                showSearchDialog = showDialog,
                searchMealQuery = query
            )
        }

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
            
            val logId = UUID.randomUUID().toString()
            journal.addMealLog(MealLog(id = logId, createdAt = timestamp))
            journal.addMealToLog(logId, mealId, amount)
        }
    }

    fun removeMealLog(logId: String) {
        viewModelScope.launch {
            journal.removeMealLog(logId)
        }
    }
}
