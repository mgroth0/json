package matt.json.stream

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import matt.json.stream.JsonObjectStreamer.ObjectValue


abstract class JsonStreamer {

    protected abstract suspend fun writeText(text: String)
    internal suspend fun writeTextInternal(text: String) = writeText(text)


    suspend inline fun <reified T> serializableValue(value: T) = serializableValue(value, serializer<T>())

    suspend fun <T> serializableValue(
        value: T,
        serializer: KSerializer<T>
    ) {

        writeText(Json.encodeToString(serializer, value))

    }


    abstract suspend fun close()


    suspend fun startArrayStreamer() = JsonArrayStreamer(this).apply {
        start()
    }

    suspend fun startObjectStreamer() = JsonObjectStreamer(this).apply {
        start()
    }


}

abstract class JsonCollectionStreamer(
    protected val streamer: JsonStreamer
) {
    internal sealed interface Status
    internal data object NotStarted : Status
    internal class Started(val lastAppended: Piece?) : Status
    internal data object Ended : Status

    internal sealed interface Piece
    internal data object Delimiter : Piece
    internal interface NonDelimiter : Piece {
        val followedByDelimiter: Boolean
    }


    internal var status: Status = NotStarted

    suspend fun delimiter() {
        check(((status as Started).lastAppended as NonDelimiter).followedByDelimiter)
        streamer.writeTextInternal(",")
        status = Started(lastAppended = Delimiter)
    }

    internal abstract suspend fun start()
    abstract suspend fun end()
    internal abstract fun setStatusToValueAdded()


    suspend inline fun <reified T> serializableValue(value: T) = serializableValue(value, serializer<T>())


    private suspend fun preValue() {
        val startedStatus = status as Started
        val lastAppended = startedStatus.lastAppended
        when (lastAppended) {
            Delimiter       -> Unit
            is NonDelimiter -> {
                check(lastAppended !is ObjectValue)
                if (lastAppended.followedByDelimiter) delimiter()
            }

            null            -> Unit
        }
    }

    suspend fun <T> serializableValue(
        value: T,
        serializer: KSerializer<T>
    ) {
        preValue()
        streamer.serializableValue(value, serializer)
        setStatusToValueAdded()
    }


    suspend fun startArrayStreamer(): JsonArrayStreamer {
        preValue()
        val r =streamer.startArrayStreamer()
        setStatusToValueAdded() /*ideally would have an intermediate state for when a nested value is still being written*/
        return r
    }

    suspend fun startObjectStreamer(): JsonObjectStreamer {
        preValue()
        val r = streamer.startObjectStreamer()
        setStatusToValueAdded() /*ideally would have an intermediate state for when a nested value is still being written*/
        return r
    }

}


class JsonArrayStreamer(
    streamer: JsonStreamer
) : JsonCollectionStreamer(streamer) {


    internal data object ArrayElement : NonDelimiter {
        override val followedByDelimiter = true
    }

    override suspend fun start() {
        check(status is NotStarted)
        streamer.writeTextInternal("[")
        status = Started(null)
    }

    override suspend fun end() {
        check(((status as Started).lastAppended as NonDelimiter?)?.followedByDelimiter != false)
        streamer.writeTextInternal("]")
        status = Ended
    }

    override fun setStatusToValueAdded() {
        status = Started(ArrayElement)
    }


}


class JsonObjectStreamer(
    streamer: JsonStreamer
) : JsonCollectionStreamer(streamer) {

    internal data object ObjectKey : NonDelimiter {
        override val followedByDelimiter = false
    }

    internal data object ObjectValue : NonDelimiter {
        override val followedByDelimiter = true
    }

    override suspend fun start() {
        check(status is NotStarted)
        streamer.writeTextInternal("{")
        status = Started(null)
    }

    override suspend fun end() {
        check(((status as Started).lastAppended as NonDelimiter?)?.followedByDelimiter != false)
        streamer.writeTextInternal("}")
        status = Ended
    }


    suspend fun objectKey(key: String) {

        val startedStatus = status as Started
        val lastAppended = startedStatus.lastAppended
        when (lastAppended) {
            Delimiter       -> Unit
            is NonDelimiter -> {
                check(lastAppended is ObjectValue)
                delimiter()
            }

            null            -> Unit
        }

        check(((status as Started).lastAppended is Delimiter?))
        streamer.writeTextInternal("\"$key\":")
        status = Started(ObjectKey)
    }

    override fun setStatusToValueAdded() {
        status = Started(ObjectValue)
    }
}
