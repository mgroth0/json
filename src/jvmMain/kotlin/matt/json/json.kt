@file:JvmName("JsonJvmKt")

package matt.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import matt.json.prim.loadJson
import matt.lang.anno.Duplicated
import matt.model.code.sharedmem.SharedMemoryDomain
import matt.model.data.prop.ConvertedSuspendProperty
import matt.model.obj.stream.Streamable
import matt.model.obj.text.HasText
import java.io.FileNotFoundException

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

//
//actual fun <T : Any> String.parseNoInline(
//    json: Json,
//    cls: KClass<T>
//) = json.decodeFromString(
//    cls.serializer(),
//    this
//)

/*faster than checking if the file exists on each load. Especially if the file is there, in which case it didn't need to be checked in the first place. Fewer OS calls.*/
inline fun <reified T : Any> HasText.loadJsonOrNullIfFileNotFound(ignoreUnknownKeys: Boolean = false): T? = try {
    text.loadJson(ignoreUnknownKeys = ignoreUnknownKeys)
} catch (e: FileNotFoundException) {
    null
}



@Duplicated
inline fun <reified T : Any> SharedMemoryDomain.myJson2(
    key: String,
) = ConvertedSuspendProperty(str(key), JsonStringConverter(serializer<T>(),json))
