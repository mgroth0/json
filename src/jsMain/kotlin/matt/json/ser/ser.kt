package matt.json.ser

//
///*cls used to be qname: String, but this is much less typesafe*/
//actual abstract class MyJsonSerializer<T : Any> actual constructor(private val cls: KClass<*>) : KSerializer<T>,
//    SerIdea {
//    actual final override val descriptor: SerialDescriptor get() = TODO()
//    actual final override fun deserialize(decoder: Decoder): T {
//        TODO()
//    }
//
//
//    actual final override fun serialize(
//        encoder: Encoder,
//        value: T
//    ) {
//        TODO()
//    }
//
//    actual abstract fun deserialize(jsonElement: JsonElement): T
//    actual abstract fun serialize(value: T): JsonElement
//
//    actual fun canSerialize(value: Any): Boolean {
//        return cls.isInstance(value)
//    }
//
//    actual fun castAndSerialize(value: Any): JsonElement {
//        @Suppress("UNCHECKED_CAST")
//        return serialize(value as T)
//    }
//}