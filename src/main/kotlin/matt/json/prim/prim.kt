package matt.json.prim

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import matt.json.parseJson
import java.io.File
import kotlin.reflect.KClass

fun String.parseJsonObj(): JsonObject = Json.decodeFromString<JsonObject>(this)

fun File.parseJsonObj() = readText().parseJsonObj()

fun String.parseJsonObjs(): JsonArray = Json.decodeFromString<JsonArray>(this)


fun File.parseJsonObjs() = readText().parseJsonObjs()


val PrettyJson = Json {
  prettyPrint = true
}

fun String.toPrettyJson() = PrettyJson.encodeToString(parseJson())

//  try {


//
//  val json = JsonParser.parseString(this)
//
//
//  val gson = GsonBuilder()
//	.setPrettyPrinting()
//	.serializeNulls() /*TODO: dont do this, its slower*/
//	.create()
//  gson.toJson(json)
//} catch (e: JsonSyntaxException) {
//  println(e.message)
//  println("\n\n\n${this}\n\n\n")
//  e.printStackTrace()
//  exitProcess(1)
//}

fun File.toPrettyJson() = readText().toPrettyJson()


fun String.isValidJson(): Boolean = try {
  Json.decodeFromString<kotlinx.serialization.json.JsonElement>(this)
  true
} catch (e: kotlinx.serialization.SerializationException) {
  false
}


//try {
//  GsonBuilder()
//	.serializeNulls()
//	.create().fromJson(this, Any::class.java)
//  true
//} catch (ex: JsonSyntaxException) {
//  false
//}

fun File.isValidJson() = readText().isValidJson()

fun File.save(je: JsonElement) {
  parentFile.mkdirs() //  MatchGroupCollection
  //  JsonObject().

  writeText(je.toString())
}


//fun <T: Any> T.toGson(): String = gson.toJson(this)

fun String.loadAndFormatJson() = toPrettyJson()
//  parseJson().let {
//  GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(it)
//}

fun File.loadAndFormatJson() = readText().loadAndFormatJson()

inline fun <reified T: Any> String.loadJson(type: KClass<T>): T = Json.decodeFromString<T>(this)

//  gson.fromJson(
//  this, type.java
//)

inline fun <reified T: Any> File.loadJson(type: KClass<T>): T = readText().loadJson(type)

inline fun <reified T: Any> String.loadJsonList(): List<T> {

  return Json.decodeFromString<JsonArray>(this).map {
	Json.decodeFromJsonElement<T>(it)
  }

  //  @Suppress("UNCHECKED_CAST") return (gson.fromJson(
  //	this, type.java.arrayType()
  //  ) as Array<T>).toList()
}
//inline fun <reified T> String.loadJsonList(): List<T> {
//  return gson.fromJson(this, arrayOf<T>()::class.java).toList()
//}

inline fun <reified T: Any> File.loadJsonList(): List<T> = readText().loadJsonList<T>()


operator fun JsonElement.set(s: String, v: Any) {
  jsonObject[s] = v
  //  this.asJsonObject.add(s, gson.fromJson(gson.toJson(v), JsonElement::class.java))
}


//inline fun <reified T> File.loadJsonList(): List<T> = readText().loadJsonList()