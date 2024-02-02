package matt.json.lang

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


inline fun <reified T> JsonElement.getOrNull(s: String): T? = try {
    this[s]
} catch (e: JsonObjectDoesNotContainKey) {
    null
}


sealed class JsonException: Exception()
class IsNotJsonObjectException: JsonException()
class JsonObjectDoesNotContainKey(val key: String): JsonException() {
    override val message: String
        get() = super.message + " (key: $key)"
}


inline operator fun <reified T> JsonElement.get(s: String): T? {
    if (this !is JsonObject) {
        throw IsNotJsonObjectException()
    }
    val o = this.jsonObject
    if (s !in o) {
        throw JsonObjectDoesNotContainKey(s)
    }
    val thing = o[s]
    return if (thing is JsonNull) {
        null
    } else {
        Json.decodeFromString<T>(thing.toString())
    }
}
