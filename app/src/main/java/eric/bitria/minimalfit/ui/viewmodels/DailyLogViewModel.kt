package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.datasource.FoodDatabase
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DailyLogUiState(
    val meals: List<Meal> = emptyList(),
    val savedMeals: List<Meal> = emptyList(),
    val calorieGoal: Int = 2500,
    val showSearchDialog: Boolean = false
)

class DailyLogViewModel(
    private val journal: JournalRepository,
    private val foodDatabase: FoodDatabase
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)

    fun buildUiState(dayIndex: Int): StateFlow<DailyLogUiState> {
        val date = dateForIndex(dayIndex)
        return combine(journal.logs, _showDialog) { _, showDialog ->
            val log = journal.getLog(date)
            DailyLogUiState(
                meals = log.meals,
                savedMeals = foodDatabase.meals,
                calorieGoal = log.calorieGoal,
                showSearchDialog = showDialog
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyLogUiState(
                meals = journal.getLog(date).meals,
                savedMeals = foodDatabase.meals,
                calorieGoal = journal.getLog(date).calorieGoal
            )
        )
    }

    fun openSearchDialog() {
        _showDialog.value = true
    }

    fun dismissSearchDialog() {
        _showDialog.value = false
    }

    fun addMeal(dayIndex: Int, meal: Meal) {
        journal.addMeal(dateForIndex(dayIndex), meal)
    }

    private fun dateForIndex(dayIndex: Int): LocalDate =
        journal.last7Days().getOrElse(dayIndex) { LocalDate.now() }
}
