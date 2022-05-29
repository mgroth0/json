package matt.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import matt.klib.lang.NEVER
import java.io.File

fun String.parseJson() = Json.decodeFromString<JsonElement>(this)
fun File.parseJson() = readText().parseJson()


inline fun <reified T> SerializationStrategy<T>.withDeserializationStrategy(d: DeserializationStrategy<T>) =
  object: KSerializer<T> {
	override val descriptor = this@withDeserializationStrategy.descriptor
	override fun deserialize(decoder: Decoder) = d.deserialize(decoder)
	override fun serialize(encoder: Encoder, value: T) = this@withDeserializationStrategy.serialize(encoder, value)
  }

inline fun <reified T> DeserializationStrategy<T>.withSerializationStrategy(s: SerializationStrategy<T>) =
  object: KSerializer<T> {
	override val descriptor = this@withSerializationStrategy.descriptor
	override fun deserialize(decoder: Decoder) = this@withSerializationStrategy.deserialize(decoder)
	override fun serialize(encoder: Encoder, value: T) = s.serialize(encoder, value)
  }

inline fun <reified T> SerializationStrategy<T>.withDeserializationStrategy(crossinline d: Decoder.()->T) =
  withDeserializationStrategy(object: DeserializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun deserialize(decoder: Decoder): T {
	  return decoder.d()
	}
  })

inline fun <reified T> DeserializationStrategy<T>.withSerializationStrategy(crossinline s: Encoder.(T)->Unit) =
  withSerializationStrategy(object: SerializationStrategy<T> {
	override val descriptor get() = NEVER
	override fun serialize(encoder: Encoder, value: T) {
	  return encoder.s(value)
	}
  })

