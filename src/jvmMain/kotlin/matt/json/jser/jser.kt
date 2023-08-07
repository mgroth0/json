package matt.json.jser

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
internal fun <T : Any> ObjectOutputStream.writeJson(
    cls: KClass<T>,
    obj: T
) {
    val s = Json.encodeToString(cls.serializer(), obj)
    writeInt(s.length)
    write(s.encodeToByteArray())
}


private inline fun <reified T : Any> ObjectOutputStream.writeJson(obj: T) {
    writeJson(T::class, obj)
}

//@OptIn(InternalSerializationApi::class)
//internal fun ObjectOutputStream.writeJsonWithReflection(obj: Any) {
//    @Suppress("UNCHECKED_CAST") val s = Json.encodeToString(obj::class.serializer() as KSerializer<Any>, obj)
//    writeInt(s.length)
//    write(s.encodeToByteArray())
//}


@OptIn(InternalSerializationApi::class)
internal fun <T : Any> ObjectInputStream.readJson(cls: KClass<T>): T {
    val length = readInt()
    val sIn = readNBytes(length).decodeToString()
    return Json.decodeFromString(cls.serializer(), sIn)
}


private inline fun <reified T : Any> ObjectInputStream.readJson(): T {
    return readJson(T::class)
}

