package matt.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import java.io.File

val gson by lazy {
  GsonBuilder().serializeNulls().create()
}

fun String.parseJson(): JsonElement = gson.fromJson(
  this, JsonElement::class.java
) as JsonElement

fun File.parseJson() = readText().parseJson()
