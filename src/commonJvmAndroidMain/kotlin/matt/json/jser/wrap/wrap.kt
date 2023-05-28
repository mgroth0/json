package matt.json.jser.wrap

import matt.json.jser.readJson
import matt.json.jser.writeJson
import matt.lang.anno.SeeURL
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass

@SeeURL("https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:not_yet_implemented:java_serialization")
@SeeURL("https://github.com/gradle/gradle/issues/13588")
abstract class JavaIoSerializableWrapper<T : Any>(innerObject: T? = null) : Serializable {

    abstract val cls: KClass<T>


    lateinit var innerObject: T
        private set

    init {
        if (innerObject != null) {
            this.innerObject = innerObject
        }
    }


    private fun writeObject(out: ObjectOutputStream) {
        out.writeJson(cls, innerObject)
    }


    private fun readObject(`in`: ObjectInputStream) {
        innerObject = `in`.readJson(cls)
    }
}


