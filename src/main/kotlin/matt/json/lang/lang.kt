package matt.json.lang

import com.google.gson.JsonElement
import matt.kbuild.gson

inline fun <reified T> JsonElement.getOrNull(s: String): T? {
  return try {
	this[s]
  } catch (e: JsonObjectDoesNotContainKey) {
	null
  }
}


sealed class JsonException: Exception()
class IsNotJsonObjectException: JsonException()
class JsonObjectDoesNotContainKey(val key: String): JsonException() {
  override val message: String
	get() = super.message + " (key: $key)"
}


inline operator fun <reified T> JsonElement.get(s: String): T? {
  if (!this.isJsonObject) {
	throw IsNotJsonObjectException()
  }
  val o = this.asJsonObject
  if (!o.has(s)) {
	throw JsonObjectDoesNotContainKey(s)
  }
  val thing = o[s]
  return if (thing.isJsonNull) {
	null
  } else {
	gson.fromJson(
	  thing.toString(), T::class.java
	)
  }
}
