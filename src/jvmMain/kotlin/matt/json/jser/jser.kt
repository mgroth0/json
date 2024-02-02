package matt.json.jser

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.ObjectInputStream
import java.io.ObjectOutput
import kotlin.reflect.KClass
import kotlin.reflect.cast

internal fun <T : Any> ObjectOutput.writeJson(
    cls: KClass<T>,
    obj: T
) {
    check(cls.java.typeParameters.isEmpty())
    val s = Json.encodeToString(serializer(cls,listOf(/*safe because I check type params are empty*/),false /*ensured not null in kotlin non-null param above*/), obj)
    writeInt(s.length)
    write(s.encodeToByteArray())
}


private inline fun <reified T : Any> ObjectOutput.writeJson(obj: T) {
    writeJson(T::class, obj)
}

//@OptIn(InternalSerializationApi::class)
//internal fun ObjectOutputStream.writeJsonWithReflection(obj: Any) {
//    @Suppress("UNCHECKED_CAST") val s = Json.encodeToString(obj::class.serializer() as KSerializer<Any>, obj)
//    writeInt(s.length)
//    write(s.encodeToByteArray())
//}


@OptIn(InternalSerializationApi::class)
internal fun <T : Any> ObjectInputStream.readJsonAssumingNeverNull(cls: KClass<T>): T {
    val length = readInt()
    val sIn = readNBytes(length).decodeToString()
    check(cls.java.typeParameters.isEmpty())
    val o = Json.decodeFromString(serializer(cls,listOf(/*safe because I check type params are empty*/),isNullable=false), sIn)
    return cls.cast(o)
}


private inline fun <reified T : Any> ObjectInputStream.readJsonAssumingNeverNull(): T = readJsonAssumingNeverNull(T::class)

