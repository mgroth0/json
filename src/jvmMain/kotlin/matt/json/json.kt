@file:JvmName("JsonJvmKt")

package matt.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import matt.model.obj.stream.Streamable
import kotlin.reflect.KClass

inline fun <reified T> Json.decodeFromStreamable(f: Streamable) = decodeFromStream<T>(f.inputStream())

/*

fun Any.loadProperties(obj: JsonElement) {
  require(obj is JsonObject)
  val a = this
  println(
	"it is actually ambiguous what to do here. like if a default is set in the Any but absent from the json, does the prop here get reset or ignored?"
  )
  obj.forEach { key, v ->
	require(v is JsonPrimitive)
	if (key != "type") {
	  a::class.memberProperties.first { it.name == key }.call(a)
	}

  }
}

*/


actual fun <T : Any> String.parseNoInline(
    json: Json,
    cls: KClass<T>
) = json.decodeFromString(
    cls.serializer(),
    this
)