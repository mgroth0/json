package matt.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import matt.json.prim.PrettyJson
import matt.lang.NEVER
import matt.model.text.HasText
import matt.model.yes.YesIUse
import kotlin.reflect.KClass

object YesIUseJson: YesIUse

inline fun <reified T> JsonElement.decode() = Json.decodeFromJsonElement<T>(this)

fun String.parseJson() = Json.decodeFromString<JsonElement>(this)

inline fun <reified T> String.parse() = Json.decodeFromString<T>(this)

fun HasText.parseJson() = text.parseJson()


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

@Suppress("UNCHECKED_CAST") fun <T> SerializationStrategy<T>.withDeserializationStrategyHack(d: Decoder.()->Any) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return decoder.d() as T
	}
  })

inline fun <reified T: Any> SerializationStrategy<T>.withDeserializationStrategyInline(
  @Suppress("UNUSED_PARAMETER") cls: KClass<T>,
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


inline fun <reified T> T.toJson() = Json.encodeToJsonElement(this)

inline fun <reified T> T.toJsonString() = Json.encodeToString(this)
inline fun <reified T> T.toPrettyJsonString() = PrettyJson.encodeToString(this)

fun yesIUseJsonButAnInlineFunSoItDoesntShowInBytecode() = "yesIUseJsonButAnInlineFunSoItDoesntShowInBytecode"

