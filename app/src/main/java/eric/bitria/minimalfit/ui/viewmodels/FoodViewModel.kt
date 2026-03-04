package eric.bitria.minimalfit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.FoodJournal
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.Nutrient
import eric.bitria.minimalfit.data.model.UnitType
import eric.bitria.minimalfit.data.repository.journal.FoodJournalRepository
import eric.bitria.minimalfit.data.repository.meal.MealRepository
import eric.bitria.minimalfit.data.repository.tag.TagRepository
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class FoodViewModel(
    private val mealRepository: MealRepository,
    private val journalRepository: FoodJournalRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    // ── Meal library ──────────────────────────────────────────────────────────

    val savedMeals: StateFlow<List<Meal>> = mealRepository
        .getMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMeal(meal: Meal) = viewModelScope.launch { mealRepository.addMeal(meal) }
    fun updateMeal(meal: Meal) = viewModelScope.launch { mealRepository.updateMeal(meal) }
    fun deleteMeal(id: String) = viewModelScope.launch { mealRepository.deleteMeal(id) }

    fun createMeal(
        name: String,
        description: String,
        tags: List<String>,
        color: Color,
        icon: ImageVector,
        unitType: UnitType,
        servingSize: Float = 100f,
        nutrition: Map<Nutrient, Float> = emptyMap()
    ) = viewModelScope.launch {
        mealRepository.addMeal(
            Meal(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                tags = tags,
                color = color,
                icon = icon,
                unitType = unitType,
                servingSize = servingSize,
                nutrition = nutrition
            )
        )
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    val availableTags: StateFlow<List<String>> = tagRepository
        .getTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTag(tag: String) = viewModelScope.launch { tagRepository.addTag(tag) }
    fun removeTag(tag: String) = viewModelScope.launch { tagRepository.removeTag(tag) }

    // ── Food journal ──────────────────────────────────────────────────────────

    val diaryJournals: StateFlow<List<FoodJournal>> = journalRepository
        .getAllJournals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getJournalForDate(date: LocalDate): StateFlow<FoodJournal> = journalRepository
        .getJournalForDate(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodJournal(date))

    fun addMealToDiary(date: LocalDate, meal: Meal, servings: Float = 1f) =
        viewModelScope.launch { journalRepository.addEntry(date, meal, servings) }

    fun removeMealFromDiary(date: LocalDate, entryId: String) =
        viewModelScope.launch { journalRepository.removeEntry(date, entryId) }

    fun clearDiaryForDate(date: LocalDate) =
        viewModelScope.launch { journalRepository.clearJournal(date) }

    // ── Dates — last 7 days, computed fresh each time ─────────────────────────

    /** Returns the last [n] days as [LocalDate]s, oldest first. */
    fun recentDays(n: Int = 7): List<LocalDate> {
        val today = LocalDate.now()
        return (n - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }
}
