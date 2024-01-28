@file:JvmName("SerJvmAndroidKt")

package matt.json.ser


///*cls used to be qname: String, but this is much less typesafe*/
//actual abstract class MyJsonSerializer<T: Any> actual constructor(private val cls: KClass<*>): KSerializer<T>, SerIdea {
//
//    /*private val cls = cls*/
//
//    actual final override val descriptor: SerialDescriptor =
//        buildClassSerialDescriptor(cls.qualifiedName!!) /*I don't understand why ths is necessary but I think it is.*/
//
//    actual final override fun deserialize(decoder: Decoder): T {
//
//        return deserialize(jsonElement = (decoder as JsonDecoder).decodeJsonElement())
//    }
//
//    actual final override fun serialize(encoder: Encoder, value: T) {
//        (encoder as JsonEncoder).encodeJsonElement(serialize(value))
//    }
//
//    actual abstract fun deserialize(jsonElement: JsonElement): T
//    actual abstract fun serialize(value: T): JsonElement
//
//
//
//    actual fun canSerialize(value: Any) = cls.isInstance(value)
//    /*value::class.isSubclassOf(cls)*/
//    actual fun castAndSerialize(value: Any): JsonElement {
//        @Suppress("UNCHECKED_CAST")
//        return serialize(value as T)
//    }
//
//
//}