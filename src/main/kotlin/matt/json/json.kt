package matt.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File

fun String.parseJson() = Json.decodeFromString<JsonElement>(this)
fun File.parseJson() = readText().parseJson()
