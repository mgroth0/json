@file:OptIn(InternalSerializationApi::class)

package matt.json.fx

//import matt.json.custom.jsonArray
//import matt.json.custom.jsonObj
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import matt.json.custom.JsonWriter
import matt.json.custom.toJsonElement
import matt.json.ser.JsonObjectSerializer
import matt.json.ser.MySerializer
import matt.lang.err
import matt.obs.hold.NamedObsHolder
import matt.obs.prop.BindableProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Any?.toJsonElement(
  serializers: List<MySerializer<*>> = listOf()
): JsonElement {


  @Suppress("UNCHECKED_CAST")
  return when (this) {
	null                   -> JsonNull
	is String              -> JsonPrimitive(this)
	is Number              -> JsonPrimitive(this)
	is Boolean             -> JsonPrimitive(this)
	is Enum<*>             -> JsonPrimitive(this.name)
	is JsonElement         -> this
	is JsonWriter          -> this.toJsonElement()
	is Map<*, *>           -> jsonObj(this)
	is BooleanProperty     -> JsonPrimitive(this.value)
	is StringProperty      -> JsonPrimitive(this.value)
	is LongProperty        -> JsonPrimitive(this.value)
	is DoubleProperty      -> JsonPrimitive(this.value)
	is ObjectProperty<*>   -> this.value.toJsonElement(serializers = serializers)
	is BindableProperty<*> -> this.value.toJsonElement(serializers = serializers)
	is List<*>             -> jsonArray(this)
	else                   -> when {

	  this::class.serializerOrNull() != null -> Json.encodeToJsonElement(
		this::class.serializer() as KSerializer<Any>, this
	  )

	  this::class.isValue                    -> {
		this::class.memberProperties.first().run {
		  val oldAccessible = isAccessible
		  isAccessible = true
		  val r = getter.call(this).toJsonElement(serializers = serializers)
		  isAccessible = oldAccessible
		  r
		}
	  }


	  else                                   -> {
		serializers.firstOrNull {
		  it.canSerialize(this)
		}?.castAndSerialize(this) ?: err("making json object value with ${this::class} is not yet implemented")
	  }
	}


  }
}

fun jsonArray(vararg elements: Any?, serializeNulls: Boolean = false): kotlinx.serialization.json.JsonArray =
  buildJsonArray {
	elements.filter { serializeNulls || it != null }.forEach {
	  this.add(it?.toJsonElement() ?: JsonNull)
	}
  }

fun jsonArray(elements: Iterable<Any?>, serializeNulls: Boolean = false) =
  jsonArray(*elements.toList().toTypedArray(), serializeNulls = serializeNulls)


fun jsonObj(
  map: Map<*, *>, serializers: List<MySerializer<*>> = listOf()
): JsonObject = jsonObj(*map.map { it.key to it.value }.toTypedArray(), serializers = serializers)

fun jsonObj(
  vararg entries: Pair<*, *>,
  serializeNulls: Boolean = false,
  serializeEmptyLists: Boolean = true,
  serializers: List<MySerializer<*>> = listOf()
): JsonObject = buildJsonObject {
  entries.filter { serializeNulls || it.second != null }.forEach {
	val key = it.first
	val value = it.second
	require(key is String)
	val j = value?.toJsonElement(serializers = serializers) ?: JsonNull
	if ((serializeNulls || j !is JsonNull) && (serializeEmptyLists || j !is kotlinx.serialization.json.JsonArray || j.isNotEmpty())) put(
	  key, j
	)
  }
}


abstract class JsonObjectFXSerializer<T: NamedObsHolder<*>>(val cls: KClass<T>): JsonObjectSerializer<T>(cls) {
  open val miniSerializers: List<MySerializer<*>> = listOf()
  final override fun serialize(value: T) = jsonObj(
	value.namedObservables(),
	serializers = miniSerializers
  )
}
