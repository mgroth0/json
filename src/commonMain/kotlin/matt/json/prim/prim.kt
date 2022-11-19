package matt.json.prim

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import matt.json.parseJson
import matt.model.obj.text.HasText
import matt.model.obj.text.WritableText

fun String.parseJsonObj(): JsonObject = Json.decodeFromString<JsonObject>(this)

fun HasText.parseJsonObj() = text.parseJsonObj()
fun WritableText.writeJson(jsonElement: JsonElement, pretty: Boolean = false) {
  text=((if (pretty) PrettyJson else Json).encodeToString(jsonElement))
}


inline fun <reified T: Any?> WritableText.writeJson(t: T, pretty: Boolean = false) {
  text=((if (pretty) PrettyJson else Json).encodeToString(t))
}


inline fun <reified T: Any?> HasText.readJson(): T = Json.decodeFromString(text)

fun String.parseJsonObjs(): JsonArray = Json.decodeFromString<JsonArray>(this)


fun HasText.parseJsonObjs() = text.parseJsonObjs()


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

fun HasText.toPrettyJson() = text.toPrettyJson()


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

fun HasText.isValidJson() = text.isValidJson()

fun WritableText.save(je: JsonElement) {
  /*getParentFile()!!.mkdirs()*/
  text=(je.toString())
}

inline fun <reified T> WritableText.save(t: T, pretty: Boolean = true) {
  /*getParentFile()!!.mkdirs()*/
  val j = if (pretty) PrettyJson else Json
  text=(j.encodeToString(t))
}


//fun <T: Any> T.toGson(): String = gson.toJson(this)

fun String.loadAndFormatJson() = toPrettyJson()
//  parseJson().let {
//  GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(it)
//}

fun HasText.loadAndFormatJson() = text.loadAndFormatJson()

inline fun <reified T: Any> String.loadJson(): T = Json.decodeFromString(this)

//  gson.fromJson(
//  this, type.java
//)


inline fun <reified T: Any> HasText.loadJson(): T = text.loadJson()

inline fun <reified T: Any> T.saveAsJsonTo(f: WritableText, pretty: Boolean = true) = f.save(this,pretty=pretty)

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

inline fun <reified T: Any> HasText.loadJsonList(): List<T> = text.loadJsonList<T>()


//operator fun JsonElement.set(s: String, v: Any) {
//  println("s=$s")
//  println("v=$v")
//  jsonObject[s] = v
//  jsonObject
//  //  this.asJsonObject.add(s, gson.fromJson(gson.toJson(v), JsonElement::class.java))
//}


//inline fun <reified T> matt.klib.file.File.loadJsonList(): List<T> = readText().loadJsonList()