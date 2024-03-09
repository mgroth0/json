package matt.json.jser.wrap

import kotlinx.serialization.KSerializer
import matt.json.jser.readJson
import matt.json.jser.writeJson
import matt.lang.anno.SeeURL
import matt.lang.anno.ser.TsSerializable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@SeeURL("https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:not_yet_implemented:java_serialization")
@SeeURL("https://github.com/gradle/gradle/issues/13588")
abstract class JavaIoSerializableWrapper<T : Any>(innerObject: T? = null) : TsSerializable {

    abstract val serializer: KSerializer<T>


    @Transient
    lateinit var innerObject: T
        private set

    init {
        if (innerObject != null) {
            this.innerObject = innerObject
        }
    }


    final override fun superWriteObject(out: ObjectOutputStream) {
        out.writeJson(serializer, innerObject)
    }


    final override fun superReadObject(`in`: ObjectInputStream) {
        innerObject = `in`.readJson(serializer)
    }

    final override fun equals(other: Any?): Boolean =
        other != null
            && other::class == this::class
            && (other as JavaIoSerializableWrapper<*>).innerObject == innerObject

    final override fun hashCode(): Int {
        var result = serializer.hashCode()
        result = 31 * result + innerObject.hashCode()
        return result
    }

    final override fun toString(): String = "A JavaIoSerializableWrapper for $innerObject"
}



