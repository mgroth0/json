package matt.json.problem

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.collections.set

@Serializable
abstract class SuperClass

@Serializable
@SerialName("SubA")
data class SubA(val data: Double): SuperClass()

@Serializable
@SerialName("SubB")
data class SubB(val data: Double): SuperClass()

@Serializable
@SerialName("MapContainer")
data class MapContainer<K: SuperClass, V>(val map: Map<K, V>): Map<K, V> by map

@Serializable
@SerialName("StringWrapper")
data class StringWrapper(val s: String)

@Serializable
@SerialName("DoubleWrapper")
data class DoubleWrapper(val d: Double)

object StringClassSerializer: KSerializer<String> {
  override val descriptor = buildClassSerialDescriptor("string")
  override fun deserialize(decoder: Decoder) = decoder.decodeSerializableValue(StringWrapper.serializer()).s
  override fun serialize(encoder: Encoder, value: String) =
	encoder.encodeSerializableValue(StringWrapper.serializer(), StringWrapper(value))
}

object DoubleClassSerializer: KSerializer<Double> {
  override val descriptor = buildClassSerialDescriptor("double")
  override fun deserialize(decoder: Decoder) = decoder.decodeSerializableValue(DoubleWrapper.serializer()).d
  override fun serialize(encoder: Encoder, value: Double) =
	encoder.encodeSerializableValue(DoubleWrapper.serializer(), DoubleWrapper(value))
}

@Serializable
object FakeNull

fun main() {
  val theMap = mutableMapOf<SuperClass, Any?>()
  theMap[SubA(1.0)] = "valueA"
  theMap[SubB(2.0)] = 2.0
  theMap[SubB(3.0)] = SubA(1.0)
  theMap[SubB(4.0)] = FakeNull /*wish I could make this just `null`*/
  val theMapContainer = MapContainer(theMap)
  val format = Json {
	allowStructuredMapKeys = true
	ignoreUnknownKeys = true
	serializersModule = SerializersModule {
	  polymorphic(SuperClass::class) {
		subclass(SubA::class)
		subclass(SubB::class)
	  }
	  polymorphic(Any::class) {


		/*I wish I could remove all of this primitive wrapper stuff*/
		default {
		  when (it) {
			StringWrapper::class.simpleName -> StringClassSerializer
			DoubleWrapper::class.simpleName -> DoubleClassSerializer
			else                            -> throw RuntimeException("unknown type: ${it}?")
		  }
		}
		subclass(String::class, StringClassSerializer)
		subclass(Double::class, DoubleClassSerializer)
		subclass(SubA::class)
		subclass(SubB::class)
		subclass(FakeNull::class)
	  }

	  @Suppress("UNCHECKED_CAST")
	  polymorphic(
		MapContainer::class, MapContainer::class, actualSerializer = MapContainer.serializer(
		  PolymorphicSerializer(SuperClass::class),
		  PolymorphicSerializer(Any::class)
		) as KSerializer<MapContainer<*, *>>
	  )
	}
  }
  val encoded = format.encodeToString(PolymorphicSerializer(MapContainer::class), theMapContainer)
  println("\n\n${encoded}\n\n")
  val decoded = format.decodeFromString(PolymorphicSerializer(MapContainer::class), encoded)

  if (theMapContainer != decoded) {
	throw RuntimeException("the decoded object is not the same as the original")
  } else {
	println("success")
  }
}