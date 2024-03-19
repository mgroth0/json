package matt.json.j

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import matt.json.JsonStringConverter
import matt.json.prim.loadJson
import matt.lang.anno.Duplicated
import matt.lang.file.toJFile
import matt.lang.fnf.runCatchingFileTrulyNotFound
import matt.model.code.sharedmem.SharedMemoryDomain
import matt.model.data.prop.ConvertedSuspendProperty
import matt.model.obj.stream.Streamable
import matt.model.obj.text.ReadableFile

@OptIn(ExperimentalSerializationApi::class)
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










faster than checking if the file exists on each load. Especially if the file is there, in which case it didn't need to be checked in the first place. Fewer OS calls.*/
inline fun <reified T : Any> ReadableFile<*>.loadJsonOrNullIfFileNotFound(ignoreUnknownKeys: Boolean = false): T? =
    runCatchingFileTrulyNotFound(
        file = { toJFile() }
    ) {
        text.loadJson<T>(ignoreUnknownKeys = ignoreUnknownKeys)
    }.getOrNull()


@Duplicated
inline fun <reified T : Any> SharedMemoryDomain.myJson2(
    key: String
) = ConvertedSuspendProperty(str(key), JsonStringConverter(serializer<T>(), json))
