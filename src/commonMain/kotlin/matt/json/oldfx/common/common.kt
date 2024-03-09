package matt.json.oldfx.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import matt.json.oldfx.toJsonElement
import matt.json.ser.MyJsonSerializer
import matt.json.toJsonString
import matt.json.toPrettyJsonString

fun jsonArray(
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
): JsonObject = jsonObj(*map.map { it.key to it.value }.toTypedArray(), serializers = serializers)

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
) = jsonObj(*entries).toPrettyJsonString()
