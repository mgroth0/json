package matt.json.oldfx.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import matt.json.toJson

/*fun jsonArray(
    vararg elements: Any?,
    serializeNulls: Boolean = false
): JsonArray =
    buildJsonArray {
        addAll(elements.filter { serializeNulls || it != null }.map { it?.toJsonElement() ?: JsonNull })
    }

fun jsonArray(
    elements: Iterable<Any?>,
    serializeNulls: Boolean = false
) =
    jsonArray(*elements.toList().toTypedArray(), serializeNulls = serializeNulls)

fun jsonObj(
    map: Map<*, *>,
    serializers: List<MyJsonSerializer<*>> = listOf()
): JsonObject = jsonObj(*map.map { it.key to it.value }.toTypedArray(), serializers = serializers)*/

fun JsonObjectBuilder.putIfValueNotNull(
    key: String,
    value: JsonElement
) {
    if (value != JsonNull) {
        put(key, value)
    }
}
fun JsonObjectBuilder.putIfValueNotNullOrEmptyArray(
    key: String,
    value: JsonElement
) {
    if (value != JsonNull) {
        if (value !is JsonArray || !value.isEmpty()) {
            put(key, value)
        }
    }
}
fun JsonObjectBuilder.putIfValueNotNull(
    key: String,
    value: Number?
) {
    if (value != null) {
        put(key, value)
    }
}
inline fun <reified T> JsonObjectBuilder.putIfValueNotNull(
    key: String,
    value: T?
) {
    if (value != null) {
        val jsonValue = value.toJson()
        check(jsonValue!is JsonNull)
        put(key, jsonValue)
    }
}
inline fun <reified T> JsonObjectBuilder.putIfValueNotNullOrEmptyArray(
    key: String,
    value: T?
) {
    if (value != null) {
        val jsonValue = value.toJson()
        check(jsonValue!is JsonNull)
        if (jsonValue !is JsonArray || !jsonValue.isEmpty()) {
            put(key, jsonValue)
        }
    }
}



fun JsonObjectBuilder.putIfValueNotNull(
    key: String,
    value: String?
) {
    if (value != null) {
        put(key, value)
    }
}
fun JsonObjectBuilder.putIfValueNotEmpty(
    key: String,
    value: JsonArray
) {
    if (value.isNotEmpty()) {
        put(key, value)
    }
}

/*
fun jsonObj(
    vararg entries: Pair<*, *>,
    serializeNulls: Boolean = false,
    serializeEmptyLists: Boolean = true,
    serializers: List<MyJsonSerializer<*>> = listOf()
): JsonObject =
    buildJsonObject {
        entries.filter { serializeNulls || it.second != null }.forEach {
            val key = it.first
            val value = it.second
            key as String
            val j = value?.toJsonElement(serializers = serializers) ?: JsonNull
            if ((serializeNulls || j !is JsonNull) && (serializeEmptyLists || j !is JsonArray || j.isNotEmpty())) put(
                key, j
            )
        }
    }


fun jsonObjString(
    vararg entries: Pair<*, *>
) = jsonObj(*entries).toJsonString()

fun prettyJsonObjString(
    vararg entries: Pair<*, *>
) = jsonObj(*entries).toPrettyJsonString()*/
