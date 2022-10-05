@file:OptIn(InternalSerializationApi::class)

package matt.json.oldfx

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import matt.json.custom.JsonWriter
import matt.json.custom.toJsonElement
import matt.json.ser.MySerializer
import matt.lang.err
import matt.model.value.ValueWrapper
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

actual fun Any?.toJsonElement(
  serializers: List<MySerializer<*>>
): JsonElement {


  @Suppress("UNCHECKED_CAST")
  return when (this) {
	null               -> JsonNull
	is String          -> JsonPrimitive(this)
	is Number          -> JsonPrimitive(this)
	is Boolean         -> JsonPrimitive(this)
	is Enum<*>         -> JsonPrimitive(this.name)
	is JsonElement     -> this
	is JsonWriter      -> this.toJsonElement()
	is Map<*, *>       -> jsonObj(this)
	is ValueWrapper<*> -> this.value.toJsonElement(serializers = serializers)
	is List<*>         -> jsonArray(this)
	else               -> when {

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