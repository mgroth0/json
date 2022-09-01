package matt.json.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KClass


/*cls used to be qname: String, but this is much less typesafe*/
abstract class JsonSerializer<T: Any>(cls: KClass<T>): KSerializer<T> {

  final override val descriptor =
	buildClassSerialDescriptor(cls.qualifiedName!!) /*I don't understand why ths is necessary but I think it is.*/

  final override fun deserialize(decoder: Decoder): T {

	return deserialize(jsonElement = (decoder as JsonDecoder).decodeJsonElement())
  }

  final override fun serialize(encoder: Encoder, value: T) {
	(encoder as JsonEncoder).encodeJsonElement(serialize(value))
  }

  abstract fun deserialize(jsonElement: JsonElement): T
  abstract fun serialize(value: T): JsonElement
}


abstract class JsonObjectSerializer<T: Any>(cls: KClass<T>): JsonSerializer<T>(cls) {
  final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonObject)
  abstract fun deserialize(jsonObject: JsonObject): T
  abstract override fun serialize(value: T): JsonObject
}

abstract class JsonArraySerializer<T: Any>(cls: KClass<T>): JsonSerializer<T>(cls) {
  final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonArray)
  abstract fun deserialize(jsonArray: JsonArray): T
  abstract override fun serialize(value: T): JsonArray
}


interface MiniSerializer {
  fun canSerialize(a: Any): Boolean
  fun serialize(a: Any): JsonElement
}