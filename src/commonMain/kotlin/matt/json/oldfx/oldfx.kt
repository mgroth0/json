package matt.json.oldfx

import kotlinx.serialization.json.JsonElement
import matt.json.ser.MyJsonSerializer

@Suppress("NoExtensionOfAny")
expect fun Any?.toJsonElement(
    serializers: List<MyJsonSerializer<*>> = listOf()
): JsonElement


