package matt.json

import kotlinx.serialization.json.Json
import matt.lang.NOT_IMPLEMENTED
import kotlin.reflect.KClass

actual fun <T : Any> String.parseNoInline(
    json: Json,
    cls: KClass<T>
): T = NOT_IMPLEMENTED
