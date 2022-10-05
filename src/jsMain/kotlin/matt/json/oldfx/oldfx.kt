package matt.json.oldfx

import kotlinx.serialization.json.JsonElement
import matt.json.ser.MySerializer
import matt.lang.NOT_IMPLEMENTED


actual fun Any?.toJsonElement(
  serializers: List<MySerializer<*>>
): JsonElement = NOT_IMPLEMENTED