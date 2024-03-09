@file:Suppress("unused")

package matt.json.custom

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import matt.json.custom.JsonWriter.BooleanJsonWriter
import matt.json.custom.JsonWriter.GsonElementJsonWriter
import matt.json.custom.JsonWriter.JsonPropMapWriter
import matt.json.custom.JsonWriter.ListJsonWriter
import matt.json.custom.JsonWriter.MapJsonWriter
import matt.json.custom.JsonWriter.NumberJsonWriter
import matt.json.custom.JsonWriter.StringJsonWriter
import matt.json.klaxon.Render
import matt.lang.anno.Open
import matt.model.obj.boild.Builder
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty


interface SimpleGson

const val AUTOMATIC_TYPEKEY = "AUTOMATIC_TYPEKEY"

enum class CollectionType {
    ELEMENT, LIST, SET
}




@JvmName("toJsonWriterInt")
fun Collection<Int>.toJsonWriter(): ListJsonWriter<JsonWriter> = ListJsonWriter(map { it.toJsonWriter() })


fun Collection<Long>.toJsonWriter(): ListJsonWriter<JsonWriter> = ListJsonWriter(map { it.toJsonWriter() })

@JvmName("toJsonWriterStringString")
fun Map<String, String>.toJsonWriter(): MapJsonWriter<StringJsonWriter, StringJsonWriter> =
    MapJsonWriter(
        mapKeys {
            StringJsonWriter(it.key)
        }.mapValues { StringJsonWriter(it.value) }
    )

@JvmName("toJsonWriterBoolean")
fun Collection<Boolean>.toJsonWriter(): ListJsonWriter<JsonWriter> = ListJsonWriter(map { it.toJsonWriter() })

@JvmName("toJsonWriterDouble")
fun Collection<Double>.toJsonWriter(): ListJsonWriter<JsonWriter> = ListJsonWriter(map { it.toJsonWriter() })

@JvmName("toJsonWriterString")
fun Collection<String>.toJsonWriter(): ListJsonWriter<JsonWriter> = ListJsonWriter(map { it.toJsonWriter() })


/*fun jsonEquivalent(a: Any?,b: Any?) {
  if (a is List && b is List) return List.equals()
}*/



const val TYPE_KEY = "type"
val TYPE_KEY_JSON_WRITER = TYPE_KEY.toJsonWriter()

interface JsonParser<T : Any> : Builder<T> {
    fun fromJson(jv: JsonElement): T
}




fun JsonElement.toJsonWriter() = GsonElementJsonWriter(this)


object NullJsonWriter : JsonWriter() {
    override fun toJsonString(): String = "null"
}


interface ToJsonString {
    fun toJsonString(): String
}

sealed class JsonWriter : ToJsonString {


    class MapJsonWriter<K : JsonWriter, V : JsonWriter>(
        val m: Map<K, V>
    ) : JsonWriter() {


        override fun toJsonString(): String = "{${
            m.toList().joinToString(",") {
                "${it.first.toJsonString()}:${it.second.toJsonString()}"
            }
        }}"
    }

    data class StringJsonWriter(val s: String) : JsonWriter() {
        override fun toJsonString() = Render.renderString(s)
    }

    data class NumberJsonWriter(val n: Number) : JsonWriter() {
        override fun toJsonString() = Json.encodeToString(n)
    }

    data class BooleanJsonWriter(val b: Boolean) : JsonWriter() {
        override fun toJsonString() = Json.encodeToString(b)
    }

    class ListJsonWriter<T : JsonWriter>(
        val l: List<T>
    ) : JsonWriter() {


        override fun toJsonString(): String = "[${l.joinToString(",") { it.toJsonString() }}]"
    }

    @Suppress("unused")
    class ArrayJsonWriter<T : JsonWriter>(
        val l: Array<T>
    ) : JsonWriter() {


        override fun toJsonString(): String = "[${l.joinToString(",") { it.toJsonString() }}]"
    }

    class GsonElementJsonWriter(
        val e: JsonElement
    ) : JsonWriter() {
        override fun toJsonString() = Json.encodeToString(e)
    }

    class GsonArrayWriter<T : JsonWriter>(
        private val jarray: JsonArray
    ) : JsonWriter() {


        override fun toJsonString(): String = Json.encodeToString(jarray)
    }

    class JsonPropMapWriter(
        val m: JsonPropMap<*>
    ) : JsonWriter() {


        override fun toJsonString(): String =
            MapJsonWriter(
                m.toJson().map {
                    it.key.name.toJsonWriter() to it.value
                }.toMap()
            ).toJsonString()
    }
}

fun <K : JsonWriter, V : JsonWriter> Map<K, V>.toJsonWriter() = MapJsonWriter(this)

fun String.toJsonWriter() = StringJsonWriter(this)

fun Number.toJsonWriter() = NumberJsonWriter(this)

fun Boolean.toJsonWriter() = BooleanJsonWriter(this)



interface JsonPropMap<T> {
    fun toJson(): Map<KProperty<Any?>, JsonWriter>

    @Open
    fun toJsonWriter() = JsonPropMapWriter(this)
}


interface LinkedProp<T> {
    fun update(value: T)
}



val DEBUG_COMPILER =
    """
    
    annotation class json(
      val key: String = "",
      val optional: Boolean = false
    )

    @ExperimentalContracts
    abstract class AnnoJson<T: AnnoJson<T>>(val typekey: String? = null): matt.json.custom.Json<T> {
      override val json
    	get() = JsonModel<T>(typekey = typekey).apply {
    	  this::class.members.forEach { m ->
    		val anno = m.annotations.firstOrNull { it is json }
    		require(anno is json)
    		anno?.go {
    		  val jsonProp = JsonProp<T>(
    			key = it.key.takeIf { it.isNotBlank() } ?: m.name,
    			optional = it.optional,
    			toJ = {
    			  err("nah")
    			},
    			fromJ = {
    			  err("nah")
    			}
    		  )
    		}
    	  }
    	}
    }
    
    """.trimIndent()


fun convertJsonKey(v: Any?): String =
    when (v) {
        is KProperty<*> -> v.name
        else            -> v.toString()
    }


fun <T : JsonWriter> T.toJsonElement() = Json.decodeFromString<JsonElement>(toJsonString())




val JsonElement.intOrNull get() = (this as? JsonPrimitive)?.intOrNull
val JsonPrimitive.intOrNull
    get() =
        try {
            int
        } catch (e: NumberFormatException) {
            null
        }
val JsonElement.int get() = jsonPrimitive.int

val JsonElement.doubleOrNull get() = (this as? JsonPrimitive)?.doubleOrNull
val JsonElement.double get() = jsonPrimitive.double

val JsonElement.longOrNull get() = (this as? JsonPrimitive)?.longOrNull
val JsonElement.long get() = jsonPrimitive.long

val JsonElement.jsonObjectOrNull get() = (this as? JsonObject)?.jsonObject

val JsonElement.stringOrNull get() = (this as? JsonPrimitive)?.stringOrNull
val JsonPrimitive.stringOrNull get() = takeIf { it.isString }?.content
val JsonElement.string
    get() =
        jsonPrimitive.also {
            require(
                it.isString
            ) { "expected string but got \"${it.content}\"" }
        }.content

val JsonElement.boolOrNull get() = (this as? JsonPrimitive)?.booleanOrNull
val JsonElement.bool get() = jsonPrimitive.boolean



fun <T> JsonElement.nullOr(op: JsonElement.() -> T): T? = if (this is JsonNull) null else this.op()

