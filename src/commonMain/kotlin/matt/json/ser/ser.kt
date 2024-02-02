package matt.json.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
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
import matt.lang.classname.theQualifiedClassName
import matt.model.code.idea.SerIdea
import kotlin.reflect.KClass


///*cls used to be qname: String, but this is much less typesafe*/
//expect abstract class MyJsonSerializer<T : Any>(cls: KClass<*>) : KSerializer<T>, SerIdea {
//
//    fun canSerialize(value: Any): Boolean
//    final override val descriptor: SerialDescriptor
//    final override fun deserialize(decoder: Decoder): T
//    final override fun serialize(
//        encoder: Encoder,
//        value: T
//    )
//
//    abstract fun deserialize(jsonElement: JsonElement): T
//    abstract fun serialize(value: T): JsonElement
//    fun castAndSerialize(value: Any): JsonElement
//
//
//}


/*cls used to be qname: String, but this is much less typesafe*/
abstract class MyJsonSerializer<T : Any>(private val cls: KClass<*>) : KSerializer<T>, SerIdea {

    /*private val cls = cls*/

    final override val descriptor: SerialDescriptor by lazy {
        buildClassSerialDescriptor(cls.theQualifiedClassName) /*I don't understand why ths is necessary but I think it is.*/
    }


    final override fun deserialize(decoder: Decoder): T = deserialize(jsonElement = (decoder as JsonDecoder).decodeJsonElement())

    final override fun serialize(
        encoder: Encoder,
        value: T
    ) {
        (encoder as JsonEncoder).encodeJsonElement(serialize(value))
    }

    abstract fun deserialize(jsonElement: JsonElement): T
    abstract fun serialize(value: T): JsonElement


    fun canSerialize(value: Any) = cls.isInstance(value)

    /*value::class.isSubclassOf(cls)*/
    fun castAndSerialize(value: Any): JsonElement {
        @Suppress("UNCHECKED_CAST")
        return serialize(value as T)
    }


}


abstract class JsonObjectSerializer<T : Any>(cls: KClass<*>) : MyJsonSerializer<T>(cls) {
    final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonObject)
    abstract fun deserialize(jsonObject: JsonObject): T
    abstract override fun serialize(value: T): JsonObject
}

abstract class JsonArraySerializer<T : Any>(cls: KClass<*>) : MyJsonSerializer<T>(cls) {
    final override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonArray)
    abstract fun deserialize(jsonArray: JsonArray): T
    abstract override fun serialize(value: T): JsonArray
}
