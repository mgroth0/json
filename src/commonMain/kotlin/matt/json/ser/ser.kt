package matt.json.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import matt.model.code.idea.SerIdea
import kotlin.reflect.KClass


/*cls used to be qname: String, but this is much less typesafe*/
expect abstract class MyJsonSerializer<T: Any>(cls: KClass<*>): KSerializer<T>, SerIdea {

  fun canSerialize(value: Any): Boolean
  final override val descriptor: SerialDescriptor
  final override fun deserialize(decoder: Decoder): T
  final override fun serialize(encoder: Encoder, value: T)
  abstract fun deserialize(jsonElement: JsonElement): T
  abstract fun serialize(value: T): JsonElement
  fun castAndSerialize(value: Any): JsonElement


}

abstract class JsonObjectSerializer<T: Any>(cls: KClass<*>): MyJsonSerializer<T>(cls) {
  final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonObject)
  abstract fun deserialize(jsonObject: JsonObject): T
  abstract override fun serialize(value: T): JsonObject
}

abstract class JsonArraySerializer<T: Any>(cls: KClass<*>): MyJsonSerializer<T>(cls) {
  final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonArray)
  abstract fun deserialize(jsonArray: JsonArray): T
  abstract override fun serialize(value: T): JsonArray
}

abstract class JsonPrimitiveSerializer<T: Any>(cls: KClass<*>): MyJsonSerializer<T>(cls) {
  final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonPrimitive)
  abstract fun deserialize(jsonPrimitive: JsonPrimitive): T
  abstract override fun serialize(value: T): JsonPrimitive
}

//
//interface MiniSerializer {
//  fun canSerialize(a: Any): Boolean
//  fun serialize(a: Any): JsonElement
//}