package matt.json.stream

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


abstract class JsonStreamer {
    protected abstract suspend fun writeText(text: String)


    suspend fun objectStart() = writeText("{")
    suspend fun objectEnd() = writeText("}")

    suspend fun arrayStart() = writeText("[")
    suspend fun arrayEnd() = writeText("]")

    suspend fun delimiter() = writeText(",")

    suspend fun objectKey(key: String) = writeText("\"$key\":")

    suspend inline fun <reified T> serializableValue(value: T) = serializableValue(value, serializer<T>())

    suspend fun <T> serializableValue(
        value: T,
        serializer: KSerializer<T>
    ) = writeText(Json.encodeToString(serializer, value))


    abstract suspend fun close()


    /*fun startArrayStreamer() = JsonArrayStreamer()*/


}


/*class JsonArrayStreamer(
    private val streamer: JsonStreamer
) {

}*/



