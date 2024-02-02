package matt.json.oldfx

import kotlinx.serialization.json.JsonElement
import matt.json.ser.MyJsonSerializer
import matt.lang.NOT_IMPLEMENTED

@Suppress("NoExtensionOfAny")
actual fun Any?.toJsonElement(
    serializers: List<MyJsonSerializer<*>>
): JsonElement = NOT_IMPLEMENTED
