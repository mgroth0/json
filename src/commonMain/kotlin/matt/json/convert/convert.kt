package matt.json.convert

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import matt.prim.converters.StringConverter


class JsonConverter<T>(
    private val ser: KSerializer<T>,
    private val json: Json = Json
) : StringConverter<T> {
    override fun toString(t: T): String = json.encodeToString(ser, t)

    override fun fromString(s: String): T = json.decodeFromString(ser, s)
}
