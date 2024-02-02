package matt.json.sysprop

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import matt.lang.sysprop.JavaSystemProp
import matt.lang.sysprop.SystemProp

class JsonSystemProp<T : Any>(
    override val key: String,
    private val serializer: KSerializer<T>,
    val default: T? = null
) : SystemProp<T>, JavaSystemProp {

    constructor(
        key: String,
        serializer: KSerializer<T>
    ) : this(key, serializer, null)

    override fun get(): T = System.getProperty(key)?.let { value ->
        Json.decodeFromString(serializer, value)
    } ?: default ?: error("no default set for enum property ser=$serializer $key")

    override fun set(t: T) {
        System.setProperty(key, Json.encodeToString(serializer, t))
    }


}
