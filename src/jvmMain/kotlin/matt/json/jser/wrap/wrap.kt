package matt.json.jser.wrap

import matt.json.jser.readJson
import matt.json.jser.writeJson
import matt.lang.anno.SeeURL
import matt.lang.anno.ser.TsSerializable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.KClass

@SeeURL("https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:not_yet_implemented:java_serialization")
@SeeURL("https://github.com/gradle/gradle/issues/13588")
abstract class JavaIoSerializableWrapper<T : Any>(innerObject: T? = null) : TsSerializable {

    abstract val cls: KClass<T>


    lateinit var innerObject: T
        private set

    init {
        if (innerObject != null) {
            this.innerObject = innerObject
        }
    }


    final override fun writeObject(out: ObjectOutputStream) {
        out.writeJson(cls, innerObject)
    }


    final override fun readObject(`in`: ObjectInputStream) {
        innerObject = `in`.readJson(cls)
    }

    final override fun equals(other: Any?): Boolean {
        return other != null && other::class == this::class && (other as JavaIoSerializableWrapper<*>).innerObject == innerObject
    }

    final override fun hashCode(): Int {
        var result = cls.hashCode()
        result = 31 * result + innerObject.hashCode()
        return result
    }


}


