package matt.json.prim.saveload

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import matt.json.prim.loadJson
import matt.model.obj.text.MightExistAndWritableText
import kotlin.reflect.KClass

inline fun <T> MightExistAndWritableText.loadOrSaveNonBlankTxt(
    forceRecreate: Boolean = false,
    create: () -> T,
    fromText: (String) -> T,
    toText: (T) -> String
): T {
    val theText = lazy { text }
    return if (!forceRecreate && exists() && theText.value.isNotBlank()) {
        fromText(theText.value)
    } else create().also {
        text = toText(it)
    }
}

inline fun MightExistAndWritableText.loadOrSaveNonBlankString(
    forceRecreate: Boolean = false,
    op: () -> String
): String =
    loadOrSaveNonBlankTxt(
        create = op,
        toText = {
            it
        },
        fromText = {
            it
        },
        forceRecreate = forceRecreate
    )


inline fun <reified T> MightExistAndWritableText.loadOrSaveJson(
    forceRecreate: Boolean = false,
    op: () -> T
): T =
    loadOrSaveNonBlankTxt(
        create = op,
        toText = {
            Json.encodeToString(it)
        },
        fromText = {
            it.loadJson()
        },
        forceRecreate = forceRecreate
    )

fun <T: Any> MightExistAndWritableText.loadOrSaveJson(
    cls: KClass<T>,
    forceRecreate: Boolean = false,
    op: () -> T
): T =
    loadOrSaveNonBlankTxt(
        create = op,
        toText = {
            Json.encodeToString(cls.serializer(), it)
        },
        fromText = {
            it.loadJson(cls)
        },
        forceRecreate = forceRecreate
    )
