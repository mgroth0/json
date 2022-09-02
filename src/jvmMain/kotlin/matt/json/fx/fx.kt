package matt.json.fx

//import matt.json.custom.jsonArray
//import matt.json.custom.jsonObj
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import matt.json.custom.JsonWriter
import matt.json.custom.toJsonElement
import matt.json.ser.MiniSerializer
import matt.klib.lang.err
import kotlin.reflect.full.memberProperties

fun Any?.toJsonElement(
  serializers: List<MiniSerializer> = listOf()
): JsonElement {


  return when (this) {
	null               -> JsonNull
	is String          -> JsonPrimitive(this)
	is Number          -> JsonPrimitive(this)
	is Boolean         -> JsonPrimitive(this)
	is Enum<*>         -> JsonPrimitive(this.name)
	is JsonElement     -> this
	is JsonWriter      -> this.toJsonElement()
	is Map<*, *>       -> jsonObj(this)
	is BooleanProperty -> JsonPrimitive(this.value)
	is StringProperty    -> JsonPrimitive(this.value)
	is LongProperty      -> JsonPrimitive(this.value)
	is DoubleProperty    -> JsonPrimitive(this.value)
	is ObjectProperty<*> -> this.value.toJsonElement(serializers = serializers)
	is List<*>           -> jsonArray(this)
	else                 -> when {
	  this::class.isValue -> {
		this::class.memberProperties.first().getter.call(this).toJsonElement(serializers = serializers)
	  }

	  else                -> {
		serializers.firstOrNull {
		  it.canSerialize(this)
		}?.serialize(this) ?: err("making json object value with ${this::class} is not yet implemented")
	  }
	}


  }
}

fun jsonArray(vararg elements: Any?, serializeNulls: Boolean = false): kotlinx.serialization.json.JsonArray =
  buildJsonArray {
	elements
	  .filter { serializeNulls || it != null }
	  .forEach {
		this.add(it?.toJsonElement() ?: JsonNull)
	  }
  }

fun jsonArray(elements: Iterable<Any?>, serializeNulls: Boolean = false) =
  jsonArray(*elements.toList().toTypedArray(), serializeNulls = serializeNulls)


fun jsonObj(
  map: Map<*, *>,
  serializers: List<MiniSerializer> = listOf()
): JsonObject = jsonObj(*map.map { it.key to it.value }.toTypedArray(), serializers = serializers)

fun jsonObj(
  vararg entries: Pair<*, *>,
  serializeNulls: Boolean = false,
  serializers: List<MiniSerializer> = listOf()
): JsonObject = buildJsonObject {
  entries
	.filter { serializeNulls || it.second != null }
	.forEach {
	  val sec = it.second
	  val key = it.first
	  require(key is String)
	  put(key, sec?.toJsonElement(serializers = serializers) ?: JsonNull)
	}
}