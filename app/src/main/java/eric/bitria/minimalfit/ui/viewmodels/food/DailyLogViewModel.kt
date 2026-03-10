package eric.bitria.minimalfit.ui.viewmodels.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.MealLog
import eric.bitria.minimalfit.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DailyLogUiState(
    val date: LocalDate,
    val meals: List<MealLog> = emptyList(),
    val calorieGoal: Int = 2500,
    val showSearchDialog: Boolean = false
)

class DailyLogViewModel(
    private val date: LocalDate,
    private val journal: JournalRepository
) : ViewModel() {

    private val _showSearchDialog = MutableStateFlow(false)

    val uiState: StateFlow<DailyLogUiState> = combine(
        journal.getLogFlow(date),
        _showSearchDialog
    ) { log, showDialog ->
        DailyLogUiState(
            date = date,
            meals = log.meals,
            calorieGoal = log.calorieGoal,
            showSearchDialog = showDialog
        )
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

    fun addMeal(meal: Meal) {
        journal.addMeal(date, meal)
    }

    fun removeMeal(mealLog: MealLog) {
        journal.removeMeal(date, mealLog)
    }

    fun updateMeal(mealLog: MealLog) {
        journal.updateMeal(date, mealLog)
    }
}
