package matt.json.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import matt.lang.reflect.isSubTypeOf
import kotlin.reflect.KClass

/*cls used to be qname: String, but this is much less typesafe*/
actual abstract class MySerializer<T: Any> actual constructor(cls: KClass<*>): KSerializer<T> {

  val cls = cls

  actual final override val descriptor: SerialDescriptor
	get() = TODO("Not yet implemented")

  actual final override fun deserialize(decoder: Decoder): T {
	TODO("Not yet implemented")
  }

  actual final override fun serialize(encoder: Encoder, value: T) {
  }

  actual abstract fun deserialize(jsonElement: JsonElement): T
  actual abstract fun serialize(value: T): JsonElement
  actual fun canSerialize(value: Any): Boolean {
	return value::class.isSubTypeOf(cls)
  }

}