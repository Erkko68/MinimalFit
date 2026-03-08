package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class DailyLogUiState(
    val date: LocalDate,
    val meals: List<Meal> = emptyList(),
    val calorieGoal: Int = 2500,
    val showSearchDialog: Boolean = false
)

class DailyLogViewModel(
    private val date: LocalDate,
    private val journal: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DailyLogUiState(
            date = date,
            meals = journal.getLog(date).meals,
            calorieGoal = journal.getLog(date).calorieGoal
        )
    )
    val uiState: StateFlow<DailyLogUiState> = _uiState.asStateFlow()

    private fun refresh() {
        val log = journal.getLog(date)
        _uiState.update { it.copy(meals = log.meals, calorieGoal = log.calorieGoal) }
    }

    fun openSearchDialog() {
        _uiState.update { it.copy(showSearchDialog = true) }
    }

    fun dismissSearchDialog() {
        _uiState.update { it.copy(showSearchDialog = false) }
    }

    fun addMeal(meal: Meal) {
        journal.addMeal(date, meal)
        refresh()
    }

    fun removeMeal(mealId: Int) {
        journal.removeMeal(date, mealId)
        refresh()
    }

    fun updateMeal(meal: Meal) {
        journal.updateMeal(date, meal)
        refresh()
    }

    fun updateCalorieGoal(goal: Int) {
        journal.updateCalorieGoal(date, goal)
        refresh()
    }
}
