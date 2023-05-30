package matt.json.jser.oser

import matt.lang.anno.SeeURL
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

@SeeURL("https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:not_yet_implemented:java_serialization")
@SeeURL("https://github.com/gradle/gradle/issues/13588")
interface JavaIoSerializable : Serializable {

    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
    }


    private fun readObject(`in`: ObjectInputStream) {
        `in`.defaultReadObject()
    }


}

