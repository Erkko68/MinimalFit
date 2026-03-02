package eric.bitria.minimalfit.data.repository.tag

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryTagRepository : TagRepository {

    private val _tags = MutableStateFlow(defaultTags())

    override fun getTags(): Flow<List<String>> =
        _tags.map { it.sorted() }

    override suspend fun addTag(tag: String) {
        val normalized = tag.trim()
        if (normalized.isBlank()) return
        _tags.update { tags ->
            if (tags.any { it.equals(normalized, ignoreCase = true) }) tags
            else tags + normalized
        }
    }

    override suspend fun removeTag(tag: String) {
        _tags.update { it.filter { t -> t != tag } }
    }

    private fun defaultTags() = listOf(
        "Breakfast", "Lunch", "Dinner", "Snack", "Ingredient"
    )
}

