package matt.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import matt.klib.lang.NEVER
import java.io.File

fun String.parseJson() = Json.decodeFromString<JsonElement>(this)
fun File.parseJson() = readText().parseJson()


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

fun <T> DeserializationStrategy<T>.withSerializationStrategy(s: Encoder.(T)->Unit) =
  withSerializationStrategy(object: SerializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun serialize(encoder: Encoder, value: T) {
	  return encoder.s(value)
	}
  })


fun Any.toJson() = Json.encodeToJsonElement(this)
fun Any.toJsonString() = Json.encodeToString(this)