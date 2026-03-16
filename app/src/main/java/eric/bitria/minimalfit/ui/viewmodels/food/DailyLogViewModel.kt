package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MealLog
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class DailyLogUiState(
    val date: LocalDate,
    val meals: List<MealLog> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val calorieGoal: Int = 2500,
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
        combine(
            journal.getLog(date),
            foodCatalog.getMeals(query)
        ) { log, savedMeals ->
            DailyLogUiState(
                date = date,
                meals = log.meals,
                savedMeals = savedMeals,
                calorieGoal = log.calorieGoal,
                showSearchDialog = showDialog,
                searchMealQuery = query
            )
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

    fun addMeal(meal: Meal) {
        viewModelScope.launch {
            journal.addMeal(date, meal)
        }
    }

    fun removeMeal(mealLog: MealLog) {
        viewModelScope.launch {
            journal.deleteMeal(date, mealLog)
        }
    }

    fun updateMeal(mealLog: MealLog) {
        viewModelScope.launch {
            journal.updateMeal(date, mealLog)
        }
    }
}
