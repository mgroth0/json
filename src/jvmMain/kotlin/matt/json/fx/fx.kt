package matt.json.fx

import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import matt.json.custom.JsonWriter
import matt.json.custom.jsonArray
import matt.json.custom.jsonObj
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