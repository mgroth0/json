package matt.json.jser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.ObjectInputStream
import java.io.ObjectOutput

internal fun <T> ObjectOutput.writeJson(
    ser: KSerializer<T>,
    obj: T
) {
    val s = Json.encodeToString(ser, obj)
    writeInt(s.length)
    write(s.encodeToByteArray())
}


private inline fun <reified T> ObjectOutput.writeJson(obj: T) {
    val ser = serializer<T>()
    writeJson(ser, obj)
}


internal fun <T> ObjectInputStream.readJson(ser: KSerializer<T>): T {
    val length = readInt()
    val sIn = readNBytes(length).decodeToString()
    val o = Json.decodeFromString(ser, sIn)
    return o
}


private inline fun <reified T> ObjectInputStream.readJson(): T {
    val ser = serializer<T>()
    return readJson(ser)
}

