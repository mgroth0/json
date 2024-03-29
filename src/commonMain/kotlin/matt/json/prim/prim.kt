package matt.json.prim

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer
import matt.json.parseJson
import matt.model.obj.text.HasText
import matt.model.obj.text.WritableText
import kotlin.reflect.KClass

fun String.parseJsonObj(): JsonObject = Json.decodeFromString<JsonObject>(this)

fun HasText.parseJsonObj() = text.parseJsonObj()
fun WritableText.writeJson(
    jsonElement: JsonElement,
    pretty: Boolean = false
) {
    text = ((if (pretty) PrettyJson else Json).encodeToString(jsonElement))
}


inline fun <reified T : Any?> WritableText.writeJson(
    t: T,
    pretty: Boolean = false
) {
    text = ((if (pretty) PrettyJson else Json).encodeToString(t))
}


inline fun <reified T : Any?> HasText.readJson(): T = Json.decodeFromString(text)

fun String.parseJsonObjs(): JsonArray = Json.decodeFromString<JsonArray>(this)


fun HasText.parseJsonObjs() = text.parseJsonObjs()


val PrettyJson by lazy {
    Json {
        prettyPrint = true
    }
}
val IgnoreUnknownKeysJson by lazy {
    Json {
        ignoreUnknownKeys = true
    }
}
val EncodeDefaultsJson by lazy {
    Json {
        encodeDefaults = true
    }
}

private var warnedAboutUnknownKeys = false
fun json(
    ignoreUnknownKeys: Boolean
) = when {
    ignoreUnknownKeys -> {
        if (!warnedAboutUnknownKeys) {
            println("WARNING: ignoring unknown keys")
            warnedAboutUnknownKeys = true
        }

        IgnoreUnknownKeysJson
    }

    else              -> Json
}

fun String.toPrettyJson() = PrettyJson.encodeToString(parseJson())


fun HasText.toPrettyJson() = text.toPrettyJson()


fun String.isValidJson(): Boolean =
    try {
        Json.decodeFromString<kotlinx.serialization.json.JsonElement>(this)
        true
    } catch (e: kotlinx.serialization.SerializationException) {
        false
    }



fun HasText.isValidJson() = text.isValidJson()

fun WritableText.save(je: JsonElement) {
    /*getParentFile()!!.mkdirs()*/
    text = (je.toString())
}

inline fun <reified T> WritableText.saveJson(
    t: T,
    pretty: Boolean = true
) {
    /*getParentFile()!!.mkdirs()*/
    val j = if (pretty) PrettyJson else Json
    text = (j.encodeToString(t))
}



fun String.loadAndFormatJson() = toPrettyJson()

fun HasText.loadAndFormatJson() = text.loadAndFormatJson()

inline fun <reified T : Any> String.loadJson(
    ignoreUnknownKeys: Boolean = false
): T = json(ignoreUnknownKeys = ignoreUnknownKeys).decodeFromString(this)

@OptIn(InternalSerializationApi::class)
fun <T : Any> String.loadJson(
    cls: KClass<T>,
    ignoreUnknownKeys: Boolean = false
): T = json(ignoreUnknownKeys = ignoreUnknownKeys).decodeFromString(cls.serializer(), this)



inline fun <reified T : Any> HasText.loadJson(ignoreUnknownKeys: Boolean = false): T =
    text.loadJson(ignoreUnknownKeys = ignoreUnknownKeys)



fun <T : Any> HasText.loadJson(
    cls: KClass<T>,
    ignoreUnknownKeys: Boolean = false
): T =
    text.loadJson(cls, ignoreUnknownKeys = ignoreUnknownKeys)

inline fun <reified T : Any> T.saveAsJsonTo(
    f: WritableText,
    pretty: Boolean = true
) = f.saveJson(this, pretty = pretty)

inline fun <reified T : Any> String.loadJsonList(): List<T> =
    Json.decodeFromString<JsonArray>(this).map {
        Json.decodeFromJsonElement(it)
    }

inline fun <reified T : Any> HasText.loadJsonList(): List<T> = text.loadJsonList<T>()
