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
 */
fun JsonObject.toFirestoreMap(): Map<String, Any?> = entries.associate { (key, value) ->
    key to value.toFirestoreValue()
}

/**
 * Converts a Firestore `Map<String, Any?>` into a [JsonObject] so it can be
 * decoded via kotlinx.serialization.
 */
fun Map<String, Any?>.toJsonObject(): JsonObject =
    JsonObject(entries.associate { (key, value) -> key to value.toJsonElement() })

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(
        @Suppress("UNCHECKED_CAST")
        (this as Map<String, Any?>).entries.associate { (k, v) -> k to v.toJsonElement() }
    )
    is List<*> -> JsonArray(map { it.toJsonElement() })
    else -> JsonPrimitive(toString())
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
