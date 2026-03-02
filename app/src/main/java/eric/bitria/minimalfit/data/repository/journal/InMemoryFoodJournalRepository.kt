package eric.bitria.minimalfit.data.repository.journal

import eric.bitria.minimalfit.data.model.FoodJournal
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.MealEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID

class InMemoryFoodJournalRepository : FoodJournalRepository {

    private val _journals = MutableStateFlow<Map<LocalDate, FoodJournal>>(emptyMap())

    override fun getAllJournals(): Flow<List<FoodJournal>> =
        _journals.map { it.values.sortedByDescending { j -> j.date } }

    override fun getJournalForDate(date: LocalDate): Flow<FoodJournal> =
        _journals.map { it[date] ?: FoodJournal(date) }

    override suspend fun addEntry(date: LocalDate, meal: Meal, servings: Float): MealEntry {
        val entry = MealEntry(id = UUID.randomUUID().toString(), meal = meal, servings = servings)
        _journals.update { journals ->
            val existing = journals[date] ?: FoodJournal(date)
            journals + (date to existing.copy(entries = existing.entries + entry))
        }
        return entry
    }

    override suspend fun removeEntry(date: LocalDate, entryId: String) {
        _journals.update { journals ->
            val existing = journals[date] ?: return
            val updated = existing.copy(entries = existing.entries.filter { it.id != entryId })
            if (updated.entries.isEmpty()) journals - date else journals + (date to updated)
        }
    }

    override suspend fun updateEntry(date: LocalDate, entry: MealEntry) {
        _journals.update { journals ->
            val existing = journals[date] ?: return
            val updated = existing.copy(
                entries = existing.entries.map { if (it.id == entry.id) entry else it }
            )
            journals + (date to updated)
        }
    }

    override suspend fun clearJournal(date: LocalDate) {
        _journals.update { it - date }
    }
}
