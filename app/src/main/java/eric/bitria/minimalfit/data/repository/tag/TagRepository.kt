package eric.bitria.minimalfit.data.repository.tag

import kotlinx.coroutines.flow.Flow

/**
 * Manages the user's meal tags.
 * Default tags are seeded on first use; users can add and remove their own.
 */
interface TagRepository {

    /** Emits the full sorted list of tags, reactively. */
    fun getTags(): Flow<List<String>>

    /** Adds a new tag. Does nothing if it already exists (case-insensitive). */
    suspend fun addTag(tag: String)

    /** Removes a tag by exact name. */
    suspend fun removeTag(tag: String)
}

