@file:OptIn(InternalSerializationApi::class)

package matt.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import matt.klib.lang.NEVER
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KClass

fun String.parseJson() = Json.decodeFromString<JsonElement>(this)
fun File.parseJson() = readText().parseJson()


/*class JITSerializer<T>(baseDescriptor: SerialDescriptor): SerializationStrategy<T> {
  override val descriptor = baseDescriptor
  override fun serialize(encoder: Encoder, value: T)  {
	require(value != null)
	val fixed = value as Any
	fixed::class.serializer().serialize(encoder,value)
//	Class.forName(T::class.qualifiedName)
  }
}*/


fun <T> SerializationStrategy<T>.withDeserializationStrategy(d: DeserializationStrategy<T>) = object: KSerializer<T> {
  override val descriptor = this@withDeserializationStrategy.descriptor
  override fun deserialize(decoder: Decoder) = d.deserialize(decoder)
  override fun serialize(encoder: Encoder, value: T) = this@withDeserializationStrategy.serialize(encoder, value)
}

fun <T> DeserializationStrategy<T>.withSerializationStrategy(s: SerializationStrategy<T>) = object: KSerializer<T> {
  override val descriptor = this@withSerializationStrategy.descriptor
  override fun deserialize(decoder: Decoder) = this@withSerializationStrategy.deserialize(decoder)
  override fun serialize(encoder: Encoder, value: T) = s.serialize(encoder, value)
}

fun <T> SerializationStrategy<T>.withDeserializationStrategy(d: Decoder.()->T) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return decoder.d()
	}
  })

fun <T> SerializationStrategy<T>.withJsonDeserializationStrategy(d: (JsonElement)->T) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return d((decoder as JsonDecoder).decodeJsonElement())
	}
  })

fun <T> SerializationStrategy<T>.withDeserializationStrategyHack(d: Decoder.()->Any) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return decoder.d() as T
	}
  })

inline fun <reified T: Any> SerializationStrategy<T>.withDeserializationStrategyInline(
  cls: KClass<T>,
  crossinline d: Decoder.()->T
) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return decoder.d()
	}
  })

fun <T> DeserializationStrategy<T>.withSerializationStrategy(s: Encoder.(T)->Unit) =
  withSerializationStrategy(object: SerializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun serialize(encoder: Encoder, value: T) {
	  return encoder.s(value)
	}
  })


fun Any.toJson() = Json.encodeToJsonElement(this)
fun Any.toJsonString() = Json.encodeToString(this)


fun Any.loadProperties(obj: JsonElement) {
  require(obj is JsonObject)
  val a = this
  println(
	"it is actually ambiguous what to do here. like if a default is set in the Any but absent from the json, does the prop here get reset or ignored?"
  )
  obj.forEach { key, v ->
	require(v is JsonPrimitive)
	if (key != "type") {
	  a::class.memberProperties.first { it.name == key }.call(a)
	}

  }
}