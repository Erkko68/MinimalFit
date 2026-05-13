package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.repository.food.DietRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.food.JournalRepository
import eric.bitria.minimalfit.util.endOfDayInstant
import eric.bitria.minimalfit.util.last7DaysEndingToday
import eric.bitria.minimalfit.util.shortWeekdayLabel
import eric.bitria.minimalfit.util.startOfDayInstant
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyCalorieData(
    val date: LocalDate,
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

sealed class FoodNavigationEvent {
    data class ToDiet(val diet: Diet) : FoodNavigationEvent()
    data class ToMeal(val meal: Meal) : FoodNavigationEvent()
}

class FoodViewModel(
    private val journal: JournalRepository,
    private val dietRepository: DietRepository,
    private val foodCatalog: FoodCatalogRepository,
) : ViewModel() {

    private val _searchDietQuery = MutableStateFlow("")
    private val _searchMealQuery = MutableStateFlow("")

    private val _navigationEvents = Channel<FoodNavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FoodUiState> = combine(
        _searchDietQuery,
        _searchMealQuery
    ) { dietQuery, mealQuery ->
        dietQuery to mealQuery
    }.flatMapLatest { (dietQuery, mealQuery) ->
        val days = last7DaysEndingToday()

        val weeklyProgressFlow = combine(days.map { date ->
            val start = date.startOfDayInstant()
            val end = date.endOfDayInstant()
            journal.getMealLogs(start = start, end = end).flatMapLatest { logs ->
                if (logs.isEmpty()) flowOf(0)
                else combine(logs.map { journal.getLogCalories(it.id) }) { calories ->
                    calories.sum()
                }
            }.map { calories ->
                DailyCalorieData(
                    date = date,
                    dayLabel = date.shortWeekdayLabel(),
                    dayNumber = date.day,
                    currentCalories = calories,
                    goalCalories = 2500 // TODO Hardcoded for now
                )
            }
        }) { it.toList() }

        combine(
            weeklyProgressFlow,
            dietRepository.getDiets(dietQuery),
            foodCatalog.getMeals(mealQuery)
        ) { progress, diets, meals ->
            FoodUiState(
                weeklyProgress = progress,
                diets = diets,
                meals = meals,
                searchDietQuery = dietQuery,
                searchMealQuery = mealQuery
            )
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

    fun createDiet(name: String) {
        viewModelScope.launch {
            val diet = Diet(name = name.trim())
            dietRepository.addDiet(diet)
            _navigationEvents.send(FoodNavigationEvent.ToDiet(diet))
        }
    }

    fun createMeal(name: String) {
        viewModelScope.launch {
            val meal = Meal(name = name.trim())
            foodCatalog.addMeal(meal)
            _navigationEvents.send(FoodNavigationEvent.ToMeal(meal))
        }
    }
}
