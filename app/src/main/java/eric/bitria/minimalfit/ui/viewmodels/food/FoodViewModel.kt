package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.food.DailyMealLog
import eric.bitria.minimalfit.data.model.food.Diet
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.util.last7DaysEndingToday
import eric.bitria.minimalfit.util.shortWeekdayLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

data class DailyCalorieData(
    val dayLabel: String,
    val dayNumber: Int,
    val currentCalories: Int,
    val goalCalories: Int
)

data class FoodUiState(
    val weeklyProgress: List<DailyCalorieData> = emptyList(),
    val diets: List<Diet> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val searchDietQuery: String = "",
    val searchMealQuery: String = ""
)

class FoodViewModel(
    private val journal: JournalRepository,
    private val dietRepository: DietRepository,
    private val foodCatalog: FoodCatalogRepository,
) : ViewModel() {

    private val _searchDietQuery = MutableStateFlow("")
    private val _searchMealQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FoodUiState> = combine(
        _searchDietQuery,
        _searchMealQuery
    ) { dietQuery, mealQuery ->
        dietQuery to mealQuery
    }.flatMapLatest { (dietQuery, mealQuery) ->
        val days = last7DaysEndingToday()
        val start = days.first()
        val end = days.last()

        combine(
            journal.getLogs(start, end),
            dietRepository.getDiets(dietQuery),
            foodCatalog.getMeals(mealQuery)
        ) { logsList, diets, meals ->
            val logsMap = logsList.associateBy { it.date }
            buildUiState(logsMap, diets, meals, dietQuery, mealQuery, days)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodUiState()
    )

    fun onSearchDietQueryChange(query: String) {
        _searchDietQuery.value = query
    }

    fun onSearchMealQueryChange(query: String) {
        _searchMealQuery.value = query
    }

    private fun buildUiState(
        logs: Map<LocalDate, DailyMealLog>,
        diets: List<Diet>,
        meals: List<Meal>,
        dietQuery: String,
        mealQuery: String,
        days: List<LocalDate>
    ): FoodUiState {
        return FoodUiState(
            weeklyProgress = days.map { date ->
                val log = logs[date] ?: DailyMealLog(date = date)
                DailyCalorieData(
                    dayLabel = date.shortWeekdayLabel(),
                    dayNumber = date.day,
                    currentCalories = log.totalCalories,
                    goalCalories = log.calorieGoal
                )
            },
            diets = diets,
            meals = meals,
            searchDietQuery = dietQuery,
            searchMealQuery = mealQuery
        )
    }
}
