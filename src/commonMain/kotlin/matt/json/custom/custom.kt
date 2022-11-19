@file:Suppress("unused")

package matt.json.custom

import kotlinx.serialization.decodeFromString
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
import matt.model.obj.boild.Builder
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty


interface SimpleGson

const val AUTOMATIC_TYPEKEY = "AUTOMATIC_TYPEKEY"

enum class CollectionType {
  ELEMENT, LIST, SET
}

//
//@Suppress("RemoveExplicitTypeArguments")/*Hopefully this will help reduce the huge kotlin compiler problem*/
//abstract class SimpleJson<T: SimpleJson<T>>(typekey: String?, efficient: Boolean = false): matt.json.custom.Json<T> {
//
//  init {
//	err("no")
//  }
//
//  private var loaded = false
//  final override fun onload() {
//	val wasFalse = !loaded
//	loaded = true
//	if (wasFalse) {
//	  toRunAfterFirstLoad.forEach {
//		it.invoke()
//	  }
//	}
//	toRunAfterEachLoad.forEach {
//	  it.invoke()
//	}
//  }
//
//  private val toRunAfterFirstLoad = mutableListOf<()->Unit>()
//  private val toRunAfterEachLoad = mutableListOf<()->Unit>()
//  fun afterFirstLoad(op: ()->Unit) {
//	if (loaded) op()
//	else {
//	  toRunAfterFirstLoad += op
//	}
//  }
//
//  fun afterEachLoad(op: ()->Unit) {
//	if (loaded) op()
//	else {
//	  toRunAfterEachLoad += op
//	}
//  }
//
//  private val registry = mutableMapOf<String, SuperDelegate<*, *>>()
//  val nonListRegistry get() = registry.toMap()
//
//  override fun toString(): String {
//	return super.toJson().toJsonString()
//  }
//
//  override val json = JsonModel<T>(
//	typekey = typekey, efficient = efficient
//  )
//
//  open inner class JsonProperty<P: Any>(
//	val toJ: T.(P)->JsonWriter,
//	val fromJ: T.(JsonElement)->P,
//	val optional: Boolean = false,
//	val noload: Boolean = false, /*implied optional*/
//	val list: CollectionType = CollectionType.ELEMENT,
//	val default: Any? = NoDefault,
//	val set: ((P)->P)? = null,
//	val get: ((P)->P)? = null
//  ) {
//
//	init {
//	  when (list) {
//		CollectionType.LIST, CollectionType.SET -> {
//		  require(set == null)
//		  require(get == null)
//		}
//		CollectionType.ELEMENT                  -> {        //                    do nothing
//		}
//	  }
//	  assert(!optional || default != NoDefault) {
//		"optional without default doesnt really make sense and will cause issues"
//	  }
//	  require(!noload || default != NoDefault) {
//		"noload without default doesnt make any sense"
//	  }
//	}
//
//	operator fun provideDelegate(
//	  thisRef: SimpleJson<T>, prop: KProperty<*>
//	): SuperDelegateBase<SimpleJson<T>, P> {    //            println("providing delegate prop=${prop.name}")
//	  val d = when (list) {
//		CollectionType.LIST    -> {
//		  if (default != NoDefault) {
//			require(default is Collection<*>)
//		  }
//		  SuperListDelegate<SimpleJson<T>, P>(
//			thisRef = thisRef,
//			name = prop.name,
//			default = default,
//		  )
//		}
//		CollectionType.SET     -> {
//		  if (default != NoDefault) {
//			require(default is Collection<*>)
//		  }
//		  SuperSetDelegate<SimpleJson<T>, P>(
//			thisRef = thisRef,
//			name = prop.name,
//			default = default,
//		  )
//		}        //		CollectionType.MAP     -> {
//		//		  if (default != NO_DEFAULT) {
//		//			require(default is Map<*,*>)
//		//		  }
//		//		  SuperSetDelegate<SimpleJson<T>, P>(
//		//			thisRef = thisRef,
//		//			name = prop.name,
//		//			default = default,
//		//		  )
//		//		}
//		CollectionType.ELEMENT -> {
//		  val dd = SuperDelegate<SimpleJson<T>, P>(
//			thisRef = thisRef, name = prop.name, default = default, setfun = set, getfun = get
//		  )
//		  registry[prop.name] = dd
//		  dd
//		}
//	  }
//
//	  /*here I remove any json props with the same name. this should allow overriding json props to work. */
//
//	  val toRemove = json.props.filter { it.key == prop.name }
//	  toRemove.forEach {
//		require(
//		  !it.d!!.hasAListener()
//		) /*this should mostly work. small risk that the superdelegate would be listening to an fx prop, though with the current implementation those listeners are bidirectional... it'll work for now*/
//	  }
//	  json.props.removeAll(toRemove)
//
//	  json.props.add(JsonProp<T>(key = prop.name, toJ = {
//		d.get()?.let { toJ(it) } ?: JsonNull.toJsonWriter()
//	  }, fromJ = {
//
//		//                        println("top of fromJ")
//
//		when (d) {
//		  is SuperDelegate     -> d.set(fromJ(it))
//		  is SuperListDelegate -> d.setAll(fromJ(it) as Collection<*>)
//		  is SuperSetDelegate  -> {                //                                println("running d.setAll(${it.toJsonWriter().toJsonString()})")
//			d.setAll(fromJ(it) as Collection<*>)
//		  }
//		}
//	  }, optional = optional, default = default
//	  ).apply {
//		this.d = d
//	  })
//	  if (noload) {
//		json.ignoreKeysOnLoad.add(prop.name)
//	  }
//	  return d
//	}
//  }
//
//  inner class JsonEnumProp<E: Enum<E>>(
//	eCls: KClass<E>,
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((E)->E)? = null,
//	get: ((E)->E)? = null,
//  ): JsonProperty<E>(
//	toJ = { it.name.toJsonWriter() },
//	fromJ = { j -> eCls.java.enumConstants.first { it.name == j.string } }, optional = optional, noload = noload,
//	default = default, set = set, get = get
//  )
//
//  inner class JsonIntProp(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Int)->Int)? = null,
//	get: ((Int)->Int)? = null
//  ): JsonProperty<Int>(toJ = { it.toJsonWriter() }, fromJ = { it.int }, optional = optional, noload = noload,
//	default = default, set = set, get = get
//  )
//
//  inner class JsonLongProp(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Long)->Long)? = null,
//	get: ((Long)->Long)? = null
//  ): JsonProperty<Long>(toJ = { it.toJsonWriter() }, fromJ = { it.jsonPrimitive.long }, optional = optional,
//	noload = noload, default = default, set = set, get = get
//  )
//
//  inner class JsonStringProp(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((String)->String)? = null,
//	get: ((String)->String)? = null
//  ): JsonProperty<String>(toJ = { it.toJsonWriter() }, fromJ = { it.jsonPrimitive.content }, optional = optional,
//	noload = noload, default = default, set = set, get = get
//  )
//
//  inner class JsonDoubleProp(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Double)->Double)? = null,
//	get: ((Double)->Double)? = null
//  ): JsonProperty<Double>(toJ = { it.toJsonWriter() }, fromJ = { it.double }, optional = optional, noload = noload,
//	default = default, set = set, get = get
//  )
//
//  inner class JsonBoolProp(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Boolean)->Boolean)? = null,
//	get: ((Boolean)->Boolean)? = null
//  ): JsonProperty<Boolean>(toJ = { it.toJsonWriter() }, fromJ = { it.bool }, optional = optional, noload = noload,
//	default = default, set = set, get = get
//  )
//
//  inner class JsonJsonProp<J: matt.json.custom.Json<*>>(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	builder: JsonParser<J>,
//	set: ((J)->J)? = null,
//	get: ((J)->J)? = null
//  ): JsonProperty<J>(toJ = { it.toJson() }, fromJ = {
//	builder.fromJson(it)
//  }, optional = optional, noload = noload, default = default, set = set, get = get
//  )
//
//
//  inner class JsonEnumPropN<E: Enum<E>>(
//	eCls: KClass<E>,
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((E?)->E?)? = null,
//	get: ((E?)->E?)? = null
//  ): JsonProperty<E?>(
//	toJ = { it?.name?.toJsonWriter() ?: NullJsonWriter },
//	fromJ = { j -> if (j is JsonNull) null else eCls.java.enumConstants.first { it.name == j.string } },
//	optional = optional,
//	noload = noload, default = default, set = set, get = get
//  )
//
//  inner class JsonIntPropN(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Int?)->Int?)? = null,
//	get: ((Int?)->Int?)? = null
//  ): JsonProperty<Int?>(toJ = { it?.toJsonWriter() ?: NullJsonWriter }, fromJ = { it.intOrNull },
//	optional = optional, noload = noload, default = default, set = set, get = get
//  )
//
//  inner class JsonLongPropN(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Long?)->Long?)? = null,
//	get: ((Long?)->Long?)? = null
//  ): JsonProperty<Long?>(toJ = { it?.toJsonWriter() ?: NullJsonWriter }, fromJ = { it.longOrNull },
//	optional = optional, noload = noload, default = default, set = set, get = get
//  )
//
//  inner class JsonStringPropN(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((String?)->String?)? = null,
//	get: ((String?)->String?)? = null
//  ): JsonProperty<String?>(
//	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
//	fromJ = { it.stringOrNull }, optional = optional, noload = noload, default = default, set = set,
//	get = get
//  )
//
//  inner class JsonDoublePropN(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Double?)->Double?)? = null,
//	get: ((Double?)->Double?)? = null
//  ): JsonProperty<Double?>(
//	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
//	fromJ = { it.doubleOrNull }, optional = optional, noload = noload, default = default, set = set,
//	get = get
//  )
//
//  inner class JsonBoolPropN(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	set: ((Boolean?)->Boolean?)? = null,
//	get: ((Boolean?)->Boolean?)? = null
//  ): JsonProperty<Boolean?>(
//	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
//	fromJ = { it.boolOrNull }, optional = optional, noload = noload, default = default, set = set,
//	get = get
//  )
//
//  inner class JsonJsonPropN<J: matt.json.custom.Json<*>>(
//	default: Any? = NoDefault,
//	optional: Boolean = false,
//	noload: Boolean = false,
//	builder: JsonParser<J>,
//	set: ((J?)->J?)? = null,
//	get: ((J?)->J?)? = null
//  ): JsonProperty<J?>(toJ = { it?.toJson() ?: NullJsonWriter }, fromJ = {
//	if (it is JsonNull) null else builder.fromJson(it)
//  }, optional = optional, noload = noload, default = default, set = set, get = get
//  )
//
//
//  inner class JsonIntListProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = listOf<Int>(), size: Int? = null
//  ): JsonProperty<List<Int>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map { it.int }.toMutableList().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  )
//
//  inner class JsonLongListProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = listOf<Long>(), size: Int? = null
//  ): JsonProperty<List<Long>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map { it.long }.toMutableList().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  )
//
//  inner class JsonStringListProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = listOf<String>(), size: Int? = null
//  ): JsonProperty<List<String>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map { it.string }.toMutableList().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  )
//
//
//  inner class JsonStringSetProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = setOf<String>(), size: Int? = null
//  ): JsonProperty<Set<String>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map {        /*val thing = */it.string        //                println("json element in fromJ: ${thing}")
//	}.toMutableSet().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.SET, default = default
//  )
//
//  inner class JsonDoubleListProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = listOf<Double>(), size: Int? = null
//  ): JsonProperty<List<Double>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map { it.double }.toMutableList().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  )
//
//  inner class JsonBoolListProp(
//	optional: Boolean = false, noload: Boolean = false, default: Any? = listOf<Boolean>(), size: Int? = null
//  ): JsonProperty<List<Boolean>>(toJ = {
//	if (size != null) {
//	  require(it.size == size)
//	}
//	it.toJsonWriter()
//  }, fromJ = { frm ->
//	frm.jsonArray.map { it.bool }.toMutableList().also {
//	  if (size != null) require(it.size == size)
//	}
//  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  )
//
//  //  inner class JsonStringMapProp(
//  //	optional: Boolean = false,
//  //	noload: Boolean = false,
//  //	default: Any? = mapOf<String, String>(),
//  //	size: Int? = null
//  //  ): JsonProperty<Map<String, String>>(
//  //	toJ = {
//  //	  if (size != null) {
//  //		require(it.size == size)
//  //	  }
//  //	  it.toJsonWriter()
//  //	},
//  //	fromJ = {
//  //	  mutableMapOf(*it.asJsonObject.entrySet().map { it.key to it.value.asString }.toTypedArray()).also {
//  //		if (size != null) require(it.size == size)
//  //	  }
//  //	},
//  //	optional = optional,
//  //	noload = noload,
//  //	list = CollectionType.MAP,
//  //	default = default
//  //  )
//
//  //  inner class JsonJsonListProp<J: Any/*: matt.json.custom.Json<*>*/>(
//  //	builder: JsonParser<J>,
//  //	ser: KSerializer<J>,
//  //	optional: Boolean = false,
//  //	noload: Boolean = false,
//  //	default: Any? = listOf<J>(),
//  //	size: Int? = null
//  //  ): JsonProperty<List<J>>(toJ = {
//  //	if (size != null) {
//  //	  require(it.size == size)
//  //	}
//  //	it.toJsonWriter(builder as? JsonProxyMap<J>, ser)
//  //  }, fromJ = { frm ->
//  //	frm.jsonArray.map {        /*println("fromJson:${it}")*/
//  //	  builder.fromJson(it)
//  //	}.toMutableList().also {
//  //	  if (size != null) require(it.size == size)
//  //	}
//  //  }, optional = optional, noload = noload, list = CollectionType.LIST, default = default
//  //  )
//
//  //  /*matt.json.custom.Json<in J>*/
//  //  inline fun <reified J: Any> jjLiProp(
//  //	builder: JsonParser<J>? = null,
//  //	ser: KSerializer<J>,
//  //	optional: Boolean = false,
//  //	noload: Boolean = false,
//  //	default: Any? = listOf<J>(),
//  //	size: Int? = null
//  //  ): JsonJsonListProp<J> {
//  //	val defBuild = object: JsonParser<J> {
//  //	  override fun fromJson(jv: JsonElement): J {
//  //		return Json.decodeFromJsonElement(J::class.serializer(), jv)
//  //		/*return jv.deserialize()*/
//  //	  }
//  //	}    //	(JsonElement) -> J = { it.deserialize<J>() }
//  //	return JsonJsonListProp<J>(
//  //	  builder = builder ?: defBuild, ser = ser, optional = optional, noload = noload, default = default, size = size
//  //	)
//  //  }
//}


@JvmName("toJsonWriterInt") fun Collection<Int>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}


fun Collection<Long>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterStringString")
fun Map<String, String>.toJsonWriter(): MapJsonWriter<StringJsonWriter, StringJsonWriter> {
  return MapJsonWriter(this.mapKeys { StringJsonWriter(it.key) }.mapValues { StringJsonWriter(it.value) })
}

@JvmName("toJsonWriterBoolean") fun Collection<Boolean>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterDouble") fun Collection<Double>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterString") fun Collection<String>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

//fun <J: Any> List</*matt.json.custom.Json<*>*/J>.toJsonWriter(
//  proxyMap: JsonProxyMap<*>? = null,
//  ser: KSerializer<J>
//): ListJsonWriter<out JsonWriter> {
//  tic(keyForNestedStuff = "listToJsonWriter", enabled = false) //  t.toc("listToJsonWriter1")
//
//  //  please prevent concurrent modification error
//  //  val immutableVersionToAvoidConcurrency = Collections.unmodifiableList(this)
//  val immutableVersionToAvoidConcurrency = this.toList()
//
//
//  Json {
//	this.useArrayPolymorphism
//  }
//
//  val r = ListJsonWriter(
//	immutableVersionToAvoidConcurrency.map { toSave: J ->
//	  if (proxyMap != null && toSave::class.simpleName in proxyMap.proxies.keys.map { it.simpleName }) {
//		matt.json.oldfx.jsonObj(
//		  "id" to (toSave as MaybeIdentified).id!!,
//		  TYPE_KEY to toSave::class.simpleName!!
//		).toJsonWriter()
//	  } else Json.encodeToJsonElement(ser, toSave).apply{
//		println("just encoded json resource here: ${this}")
////		val decoded = Json.decodeFromJsonElement<Resource>(this)
//		println("guess it did decode")
//	  }.toJsonWriter()
//
//
//
//	})
//  return r
//}

/*fun jsonEquivalent(a: Any?,b: Any?) {
  if (a is List && b is List) return List.equals()
}*/



@Suppress("unused") interface JsonArray<T: Any> {

  val json: Triple<(JsonElement)->Unit, (T)->JsonWriter, ()->Sequence<T>>

  fun toJson() = json.third().map { json.second(it) }
  fun loadElements(ja: JsonArray) {
	ja.forEach {
	  json.first(it)
	}
  }
}

const val TYPE_KEY = "type"
val TYPE_KEY_JSON_WRITER = TYPE_KEY.toJsonWriter()

interface JsonParser<T: Any>: Builder<T> {
  fun fromJson(jv: JsonElement): T
}


/*Json<out T>*//*
inline fun <reified T: Any> JsonElement.deserialize(
  superclass: KClass<T>, typekey: String? = null *//*for migration*//*
): T {
  val r = ProfiledBlock["deserialize"].with {    //  val r = jv.deserialize()
	//  val o = jv.asJsonObject


	Json {
	  classDiscriminator = typekey ?: classDiscriminator
	}.decodeFromJsonElement<T>(object: JsonContentPolymorphicSerializer<T>(superclass) {
	  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out T> {
		return superclass.subclasses().first {
		  it.simpleName == element.jsonObject["type"]!!.string
		}.serializer()
	  }
	}, this)


	*//*kotlinx.serialization.json.Json.decodeFromJsonElement<T>(this)*//*

	*//*val type = typekey ?: jsonObject[TYPE_KEY]!!.string*//*

	*//*1.5*//*    *//*val skcls = Resource::class.sealedSubclasses*//*

	*//*val skcls = superclass.subclasses()*//*

	*//*val c = skcls.first {
	  it.simpleName == type
	}


	val instance = c.createInstance()    //  println("instance is a ${instance::class.qualifiedName}")
	instance.loadProperties(this, usedTypeKey = true)    //  println("nope, did not get here")
	instance*//*
  }
  return r

}*/


/*
inline fun <reified T: matt.json.custom.Json<in T>> JsonElement.deserialize(): T {
  require(T::class.hasAnnotation<NoArgConstructor>()) {
	"${T::class} must be annotated with ${NoArgConstructor::class}"
  }
  val o = T::class.createInstance()
  o.loadProperties(jo = this)
  return o
}
*/


fun JsonElement.toJsonWriter() = GsonElementJsonWriter(this)


object NullJsonWriter: JsonWriter() {
  override fun toJsonString(): String {
	return "null"
  }

}


interface ToJsonString {
  fun toJsonString(): String
}

sealed class JsonWriter: ToJsonString {


  class MapJsonWriter<K: JsonWriter, V: JsonWriter>(
	val m: Map<K, V>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "{${
		m.toList().joinToString(",") {
		  "${it.first.toJsonString()}:${it.second.toJsonString()}"
		}
	  }}"
	}

  }

  data class StringJsonWriter(val s: String): JsonWriter() {
	override fun toJsonString() = Render.renderString(s)
  }

  data class NumberJsonWriter(val n: Number): JsonWriter() {
	override fun toJsonString() = Json.encodeToString(n)
  }

  data class BooleanJsonWriter(val b: Boolean): JsonWriter() {
	override fun toJsonString() = Json.encodeToString(b)
  }

  class ListJsonWriter<T: JsonWriter>(
	val l: List<T>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "[${l.joinToString(",") { it.toJsonString() }}]"
	}

  }

  @Suppress("unused") class ArrayJsonWriter<T: JsonWriter>(
	val l: Array<T>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "[${l.joinToString(",") { it.toJsonString() }}]"
	}

  }

  class GsonElementJsonWriter(
	val e: JsonElement
  ): JsonWriter() {
	override fun toJsonString() = Json.encodeToString(e)
  }

  class GsonArrayWriter<T: JsonWriter>(
	private val jarray: JsonArray
  ): JsonWriter() {


	override fun toJsonString(): String {

	  return Json.encodeToString(jarray)

	  //	  return "[${jarray.toList().matt.async.flow.joinToString(",") { it.toGson() }}]"
	}

  }

  class JsonPropMapWriter(
	val m: JsonPropMap<*>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return MapJsonWriter(m.toJson().map { it.key.name.toJsonWriter() to it.value }.toMap()).toJsonString()
	}

  }

}

fun <K: JsonWriter, V: JsonWriter> Map<K, V>.toJsonWriter() = MapJsonWriter(this)

fun String.toJsonWriter() = StringJsonWriter(this)

fun Number.toJsonWriter() = NumberJsonWriter(this)

fun Boolean.toJsonWriter() = BooleanJsonWriter(this)

//fun <T: Any> JsonArray.toJsonWriter() = ListJsonWriter<T>(this.toJson().toList())


interface JsonPropMap<T> {
  fun toJson(): Map<KProperty<Any?>, JsonWriter>
  fun toJsonWriter() = JsonPropMapWriter(this)
}


interface LinkedProp<T> {
  fun update(value: T)
}


//// NOTE: doesnt work
//interface JPropCastHelper<P: Any> {
//  var field: P
//  var linkedProp: LinkedProp<P>?
//}
//class HelperField() {
//  lateinit var value: Any?
//}


val DEBUG_COMPILER = """
  
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


fun convertJsonKey(v: Any?): String {
  return when (v) {
	is KProperty<*> -> v.name
	else            -> v.toString()
  }
}


fun <T: JsonWriter> T.toJsonElement() = Json.decodeFromString<JsonElement>(toJsonString())


//interface JsonProxyMap<T: Any>: JsonParser<T> {
//
//}


val JsonElement.intOrNull get() = (this as? JsonPrimitive)?.intOrNull
val JsonPrimitive.intOrNull
  get() = try {
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
  get() = jsonPrimitive.also {
	require(
	  it.isString
	) { "expected string but got \"${it.content}\"" }
  }.content

val JsonElement.boolOrNull get() = (this as? JsonPrimitive)?.booleanOrNull
val JsonElement.bool get() = jsonPrimitive.boolean

//fun jsonArray(elements: Array<Any?>, serializeNulls: Boolean = false) = jsonArray(*elements, serializeNulls = serializeNulls)


fun <T> JsonElement.nullOr(op: JsonElement.()->T): T? = if (this is JsonNull) null else this.op()

