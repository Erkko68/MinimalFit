package eric.bitria.minimalfit.data.remote.firestore

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Converts a [JsonObject] produced by kotlinx.serialization into a
 * `Map<String, Any?>` that Firestore can store directly.
 *
 * Handles nested objects, arrays, primitives, and null values.
 */
fun JsonObject.toFirestoreMap(): Map<String, Any?> = entries.associate { (key, value) ->
    key to value.toFirestoreValue()
}

private fun JsonElement.toFirestoreValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> {
        // Try to preserve the most specific numeric type
        booleanOrNull
            ?: intOrNull
            ?: longOrNull
            ?: floatOrNull
            ?: doubleOrNull
            ?: content // fallback to String
    }
    is JsonObject -> toFirestoreMap()
    is JsonArray -> map { it.toFirestoreValue() }
}
