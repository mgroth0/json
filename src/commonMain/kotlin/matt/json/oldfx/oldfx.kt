

package matt.json.oldfx

//import matt.json.custom.jsonArray
//import matt.json.custom.matt.json.oldfx.jsonObj
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import matt.json.ser.MyJsonSerializer

expect fun Any?.toJsonElement(
  serializers: List<MyJsonSerializer<*>> = listOf()
): JsonElement

fun jsonArray(vararg elements: Any?, serializeNulls: Boolean = false): kotlinx.serialization.json.JsonArray =
  buildJsonArray {
	elements.filter { serializeNulls || it != null }.forEach {
	  this.add(it?.toJsonElement() ?: JsonNull)
	}
  }

fun jsonArray(elements: Iterable<Any?>, serializeNulls: Boolean = false) =
  jsonArray(*elements.toList().toTypedArray(), serializeNulls = serializeNulls)


fun jsonObj(
  map: Map<*, *>, serializers: List<MyJsonSerializer<*>> = listOf()
): JsonObject = jsonObj(*map.map { it.key to it.value }.toTypedArray(), serializers = serializers)





fun jsonObj(
  vararg entries: Pair<*, *>,
  serializeNulls: Boolean = false,
  serializeEmptyLists: Boolean = true,
  serializers: List<MyJsonSerializer<*>> = listOf()
): JsonObject = buildJsonObject {
  entries.filter { serializeNulls || it.second != null }.forEach {
	val key = it.first
	val value = it.second
	require(key is String)
	val j = value?.toJsonElement(serializers = serializers) ?: JsonNull
	if ((serializeNulls || j !is JsonNull) && (serializeEmptyLists || j !is JsonArray || j.isNotEmpty())) put(
	  key, j
	)
  }
}
