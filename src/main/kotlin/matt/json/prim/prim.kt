package matt.json.prim

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import matt.json.gson
import matt.json.parseJson
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

fun String.parseJsonObj(): JsonObject = gson.fromJson(
  this, JsonObject::class.java
)

fun File.parseJsonObj() = readText().parseJsonObj()

fun String.parseJsonObjs(): Array<JsonObject> = gson.fromJson(
  this, arrayOf<JsonObject>()::class.java
)


fun File.parseJsonObjs() = readText().parseJsonObjs()


fun String.toPrettyJson(): String = try {
  val json = JsonParser.parseString(this)



  val gson = GsonBuilder()
	  .setPrettyPrinting()
	  .serializeNulls() /*TODO: dont do this, its slower*/
	  .create()
  gson.toJson(json)
} catch (e: JsonSyntaxException) {
  println(e.message)
  println("\n\n\n${this}\n\n\n")
  e.printStackTrace()
  exitProcess(1)
}

fun File.toPrettyJson() = readText().toPrettyJson()


fun String.isValidJson(): Boolean = try {
  GsonBuilder()
	  .serializeNulls()
	  .create().fromJson(this, Any::class.java)
  true
} catch (ex: JsonSyntaxException) {
  false
}

fun File.isValidJson() = readText().isValidJson()

fun File.save(je: JsonElement) {
  parentFile.mkdirs()
//  MatchGroupCollection
//  JsonObject().

  writeText(je.toString())
}


fun <T: Any> T.toGson(): String = gson.toJson(this)

fun String.loadAndFormatJson() = parseJson().let {
  GsonBuilder()
	  .setPrettyPrinting()
	  .serializeNulls()
	  .create().toJson(it)
}

fun File.loadAndFormatJson() = readText().loadAndFormatJson()

fun <T: Any> String.loadJson(type: KClass<T>): T = gson.fromJson(
  this,
  type.java
)

fun <T: Any> File.loadJson(type: KClass<T>): T = readText().loadJson(type)

fun <T: Any> String.loadJsonList(type: KClass<T>): List<T> {
  @Suppress("UNCHECKED_CAST")
  return (gson.fromJson(
	this,
	type.java.arrayType()
  ) as Array<T>).toList()
}

fun <T: Any> File.loadJsonList(type: KClass<T>): List<T> = readText().loadJsonList(type)


operator fun JsonElement.set(s: String, v: Any) {
  this.asJsonObject.add(s, gson.fromJson(gson.toJson(v), JsonElement::class.java))
}

inline fun <reified T> String.loadJsonList(): List<T> {
  return gson.fromJson(this, arrayOf<T>()::class.java).toList()
}

inline fun <reified T> File.loadJsonList(): List<T> = readText().loadJsonList()