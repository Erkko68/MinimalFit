package eric.bitria.minimalfit.data.repository.journal

import eric.bitria.minimalfit.data.model.FoodJournal
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.data.model.MealEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Manages the user's food journal — one [FoodJournal] per [LocalDate].
 */
interface FoodJournalRepository {

    /** Emits all journals that have at least one entry, reactively. */
    fun getAllJournals(): Flow<List<FoodJournal>>

    /** Emits the journal for [date], or an empty journal if none exists yet. */
    fun getJournalForDate(date: LocalDate): Flow<FoodJournal>

    /** Logs [meal] on [date] with optional [servings]. Returns the created [MealEntry]. */
    suspend fun addEntry(date: LocalDate, meal: Meal, servings: Float = 1f): MealEntry

    /** Removes a single entry by [entryId] from [date]. */
    suspend fun removeEntry(date: LocalDate, entryId: String)

    /** Replaces an existing entry (matched by [MealEntry.id]) in [date]'s journal. */
    suspend fun updateEntry(date: LocalDate, entry: MealEntry)

    /** Removes all entries for [date]. */
    suspend fun clearJournal(date: LocalDate)
}
