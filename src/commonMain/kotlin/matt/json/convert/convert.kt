package matt.json.convert

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import matt.model.op.convert.StringConverter


class JsonConverter<T>(
    private val ser: KSerializer<T>,
    private val json: Json = Json,
) : StringConverter<T> {
    override fun toString(t: T): String {
        return json.encodeToString(ser, t)
    }

    override fun fromString(s: String): T {
        println("DESERIALIZING 1: $s")
        println("JSON=$json")
        println("ser=$ser")
        val r = json.decodeFromString(ser, s)
        println("DESERIALIZING 2: $r")
        return r
    }
}