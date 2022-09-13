@file:JvmName("SerJvmKt")

package matt.json.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/*cls used to be qname: String, but this is much less typesafe*/
actual abstract class MySerializer<T: Any> actual constructor(cls: KClass<*>): KSerializer<T> {

  private val cls = cls

  actual final override val descriptor: SerialDescriptor =
	buildClassSerialDescriptor(cls.qualifiedName!!) /*I don't understand why ths is necessary but I think it is.*/

  actual final override fun deserialize(decoder: Decoder): T {

	return deserialize(jsonElement = (decoder as JsonDecoder).decodeJsonElement())
  }

  actual final override fun serialize(encoder: Encoder, value: T) {
	(encoder as JsonEncoder).encodeJsonElement(serialize(value))
  }

  actual abstract fun deserialize(jsonElement: JsonElement): T
  actual abstract fun serialize(value: T): JsonElement


  actual fun canSerialize(value: Any) = value::class.isSubclassOf(cls)
  actual fun castAndSerialize(value: Any): JsonElement {
	@Suppress("UNCHECKED_CAST")
	return serialize(value as T)
  }


}