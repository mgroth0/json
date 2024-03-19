
package matt.json.oldfx

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializerOrNull
import matt.lang.classname.common.JvmQualifiedClassName
import matt.lang.classname.common.SimpleClassName
import matt.model.ser.JvmQualifiedClassNameSerializer
import matt.model.ser.SimpleClassNameSerializer

/*
 * I will have to find another way to register these, whether as contextual, marked at use site, or whatever... once they are settled get rid of this. Until then, this serves as a reminder that these serializers need sorting out...
*/
@OptIn(InternalSerializationApi::class)
@Suppress("NoExtensionOfAny")
private fun Any.findSerializer() =
    when (this) {
        is JvmQualifiedClassName -> JvmQualifiedClassNameSerializer
        is SimpleClassName       -> SimpleClassNameSerializer
        else                     -> this::class.serializerOrNull()
    }


/*
@Suppress("NoExtensionOfAny")
actual fun Any?.toJsonElement(
    serializers: List<MyJsonSerializer<*>>
): JsonElement =
    when (this) {
        null               -> JsonNull
        is String          -> JsonPrimitive(this)
        is Number          -> JsonPrimitive(this)
        is Boolean         -> JsonPrimitive(this)
        is Enum<*>         -> JsonPrimitive(name)
        is JsonElement     -> this
        is JsonWriter      -> toJsonElement()
        is Map<*, *>       -> jsonObj(this)
        is ValueWrapper<*> -> value.toJsonElement(serializers = serializers)
        is List<*>         -> jsonArray(this)
        is Array<*>        -> jsonArray(*this)
        else               ->

            findSerializer().let {

                when (it) {

                    is Any ->
                        Json.encodeToJsonElement(
                            it as KSerializer<*>, this
                        )






                    JUST MAKE A SERIALIZER FOR VALUE CLASSES

                    this::class.isValue                    -> err("just make a serializer for ${this::class}!")

                    		this::class.memberProperties.first().run {
                              val oldAccessible = isAccessible
                              isAccessible = true
                              val r = getter.call(this).toJsonElement(serializers = serializers)
                              isAccessible = oldAccessible
                              r
                            }
                    }






                    else                                   -> {
                        serializers.firstOrNull {
                            it.canSerialize(this)
                        }?.castAndSerialize(this)
                            ?: err("making json object value with ${this::class} is not yet implemented")
                    }
                }
            }
    }
*/
