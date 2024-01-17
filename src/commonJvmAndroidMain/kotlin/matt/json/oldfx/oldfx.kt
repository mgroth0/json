@file:JvmName("OldfxJvmAndroidKt")

package matt.json.oldfx

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializerOrNull
import matt.json.custom.JsonWriter
import matt.json.custom.toJsonElement
import matt.json.ser.MyJsonSerializer
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.SimpleClassName
import matt.lang.err
import matt.lang.model.value.ValueWrapper
import matt.model.ser.JvmQualifiedClassNameSerializer
import matt.model.ser.SimpleClassNameSerializer


private fun Any.findSerializer() = when (this) {
    is JvmQualifiedClassName -> JvmQualifiedClassNameSerializer
    is SimpleClassName       -> SimpleClassNameSerializer
    else                     -> this::class.serializerOrNull()
}



actual fun Any?.toJsonElement(
    serializers: List<MyJsonSerializer<*>>
): JsonElement {


    @Suppress("UNCHECKED_CAST")
    return when (this) {
        null               -> JsonNull
        is String          -> JsonPrimitive(this)
        is Number          -> JsonPrimitive(this)
        is Boolean         -> JsonPrimitive(this)
        is Enum<*>         -> JsonPrimitive(this.name)
        is JsonElement     -> this
        is JsonWriter      -> this.toJsonElement()
        is Map<*, *>       -> jsonObj(this)
        is ValueWrapper<*> -> this.value.toJsonElement(serializers = serializers)
        is List<*>         -> jsonArray(this)
        is Array<*>        -> jsonArray(*this)
        else               ->

            findSerializer().let {

                when(it) {

                    is Any -> Json.encodeToJsonElement(
                        it as KSerializer<Any>, this
                    )


                    /*JUST MAKE A SERIALIZER FOR VALUE CLASSES*/

                    /*this::class.isValue                    -> err("just make a serializer for ${this::class}!")*/

                    /*		this::class.memberProperties.first().run {
                              val oldAccessible = isAccessible
                              isAccessible = true
                              val r = getter.call(this).toJsonElement(serializers = serializers)
                              isAccessible = oldAccessible
                              r
                            }*/
                    /*}*/


                    else                                   -> {
                        serializers.firstOrNull {
                            it.canSerialize(this)
                        }?.castAndSerialize(this)
                            ?: err("making json object value with ${this::class} is not yet implemented")
                    }


                }
            }



    }
}