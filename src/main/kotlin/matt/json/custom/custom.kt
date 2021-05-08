package matt.json.custom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import matt.json.custom.JsonWriter.BooleanJsonWriter
import matt.json.custom.JsonWriter.GsonElementJsonWriter
import matt.json.custom.JsonWriter.JsonPropMapWriter
import matt.json.custom.JsonWriter.ListJsonWriter
import matt.json.custom.JsonWriter.MapJsonWriter
import matt.json.custom.JsonWriter.NumberJsonWriter
import matt.json.custom.JsonWriter.StringJsonWriter
import matt.json.klaxon.Render
import matt.json.prim.gson
import matt.json.prim.toGson
import matt.kjlib.delegate.NO_DEFAULT
import matt.kjlib.delegate.SuperDelegate
import matt.kjlib.delegate.SuperDelegateBase
import matt.kjlib.delegate.SuperListDelegate
import matt.kjlib.err
import matt.klibexport.boild.Builder
import matt.klibexport.klibexport.Identified
import matt.reflect.NoArgConstructor
import matt.reflect.subclasses
import matt.reflect.testProtoTypeSucceeded
import matt.reflect.toStringBuilder
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation

@Suppress("RemoveExplicitTypeArguments")
/*Hopefully this will help reduce the huge kotlin compiler problem*/
@ExperimentalContracts
abstract class SimpleJsonList<T: SimpleJsonList<T>>(prop: JsonArrayProp<T>): Json<T> {
  override val json = JsonArrayModel<T>(prop)
}

interface SimpleGson

val AUTOMATIC_TYPEKEY = "AUTOMATIC_TYPEKEY"

@Suppress("RemoveExplicitTypeArguments")
/*Hopefully this will help reduce the huge kotlin compiler problem*/
@ExperimentalContracts
abstract class SimpleJson<T: SimpleJson<T>>(typekey: String?): Json<T> {

  private var loaded = false
  final override fun onload() {
	val wasfalse = !loaded
	loaded = true
	if (wasfalse) {
	  toRunAfterFirstLoad.forEach {
		it.invoke()
	  }
	}
	toRunAfterEachLoad.forEach {
	  it.invoke()
	}
  }

  private val toRunAfterFirstLoad = mutableListOf<()->Unit>()
  private val toRunAfterEachLoad = mutableListOf<()->Unit>()
  fun afterFirstLoad(op: ()->Unit) {
	if (loaded) op()
	else {
	  toRunAfterFirstLoad += op
	}
  }

  fun afterEachLoad(op: ()->Unit) {
	if (loaded) op()
	else {
	  toRunAfterEachLoad += op
	}
  }

  private val registry = mutableMapOf<String, SuperDelegate<*, *>>()
  val nonListRegistry get() = registry.toMap()

  override fun toString(): String {
	return super.toJson().toJsonString()
  }

  override val json = JsonModel<T>(
	typekey = typekey
  )

  open inner class JsonProperty<P: Any>(
	val toJ: T.(P)->JsonWriter,
	val fromJ: T.(JsonElement)->P,
	val optional: Boolean = false,
	val noload: Boolean = false, /*implied optional*/
	val list: Boolean = false,
	val default: Any? = NO_DEFAULT,
	val set: ((P)->P)? = null,
	val get: ((P)->P)? = null
  ) {

	init {
	  if (list) {
		require(set == null)
		require(get == null)
	  }
	  assert(!optional || default != NO_DEFAULT) {
		"optional without default doesnt really make sense and will cause issues"
	  }
	  require(!noload || default != NO_DEFAULT) {
		"noload without default doesnt make any sense"
	  }
	}

	operator fun provideDelegate(
	  thisRef: SimpleJson<T>,
	  prop: KProperty<*>
	): SuperDelegateBase<SimpleJson<T>, P> {
	  val d = if (list) {
		if (default != NO_DEFAULT) {
		  require(default is Collection<*>)
		}
		SuperListDelegate<SimpleJson<T>, P>(
		  thisRef = thisRef,
		  name = prop.name,
		  default = default,
		)
	  } else {
		val dd = SuperDelegate<SimpleJson<T>, P>(
		  thisRef = thisRef,
		  name = prop.name,
		  default = default,
		  setfun = set,
		  getfun = get
		)
		registry[prop.name] = dd
		dd
	  }

	  /*here I remove any json props with the same name. this should allow overriding json props to work. */

	  val toRemove = json.props.filter { it.key == prop.name }
	  toRemove.forEach {
		require(!it.d!!.hasAListener()) /*this should mostly work. small risk that the superdelegate would be listening to an fx prop, though with the current implementation those listeners are bidirectional... it'll work for now*/
	  }
	  json.props.removeAll(toRemove)

	  json.props.add(
		JsonProp<T>(
		  key = prop.name,
		  toJ = {
			d.get()?.let { toJ(it) } ?: JsonNull.INSTANCE.toJsonWriter()
		  },
		  fromJ = {
			when (d) {
			  is SuperDelegate     -> d.set(fromJ(it))
			  is SuperListDelegate -> d.setAll(fromJ(it) as Collection<*>)
			}
		  },
		  optional = optional
		).apply {
		  this.d = d
		}
	  )
	  if (noload) {
		json.ignoreKeysOnLoad.add(prop.name)
	  }
	  return d
	}
  }

  inner class JsonEnumProp<E: Enum<E>>(
	eCls: KClass<E>,
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((E)->E)? = null,
	get: ((E)->E)? = null,
  ): JsonProperty<E>(
	toJ = { it.name.toJsonWriter() },
	fromJ = { j -> eCls.java.enumConstants.first { it.name == j.asString } },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonIntProp(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Int)->Int)? = null,
	get: ((Int)->Int)? = null
  ): JsonProperty<Int>(
	toJ = { it.toJsonWriter() },
	fromJ = { it.asInt },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonLongProp(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Long)->Long)? = null,
	get: ((Long)->Long)? = null
  ): JsonProperty<Long>(
	toJ = { it.toJsonWriter() },
	fromJ = { it.asLong },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonStringProp(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((String)->String)? = null,
	get: ((String)->String)? = null
  ): JsonProperty<String>(
	toJ = { it.toJsonWriter() },
	fromJ = { it.asString },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonDoubleProp(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Double)->Double)? = null,
	get: ((Double)->Double)? = null
  ): JsonProperty<Double>(
	toJ = { it.toJsonWriter() },
	fromJ = { it.asDouble },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonBoolProp(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Boolean)->Boolean)? = null,
	get: ((Boolean)->Boolean)? = null
  ): JsonProperty<Boolean>(
	toJ = { it.toJsonWriter() },
	fromJ = { it.asBoolean },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonJsonProp<J: Json<*>>(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	builder: GsonParser<J>,
	set: ((J)->J)? = null,
	get: ((J)->J)? = null
  ): JsonProperty<J>(
	toJ = { it.toJson() },
	fromJ = {
	  builder.fromGson(it)
	},
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )


  inner class JsonEnumPropN<E: Enum<E>>(
	eCls: KClass<E>,
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((E?)->E?)? = null,
	get: ((E?)->E?)? = null
  ): JsonProperty<E?>(
	toJ = { it?.name?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { j -> if (j.isJsonNull) null else eCls.java.enumConstants.first { it.name == j.asString } },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonIntPropN(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Int?)->Int?)? = null,
	get: ((Int?)->Int?)? = null
  ): JsonProperty<Int?>(
	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { if (it.isJsonNull) null else it.asInt },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonLongPropN(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Long?)->Long?)? = null,
	get: ((Long?)->Long?)? = null
  ): JsonProperty<Long?>(
	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { if (it.isJsonNull) null else it.asLong },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonStringPropN(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((String?)->String?)? = null,
	get: ((String?)->String?)? = null
  ): JsonProperty<String?>(
	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { if (it.isJsonNull) null else it.asString },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonDoublePropN(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Double?)->Double?)? = null,
	get: ((Double?)->Double?)? = null
  ): JsonProperty<Double?>(
	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { if (it.isJsonNull) null else it.asDouble },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonBoolPropN(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	set: ((Boolean?)->Boolean?)? = null,
	get: ((Boolean?)->Boolean?)? = null
  ): JsonProperty<Boolean?>(
	toJ = { it?.toJsonWriter() ?: NullJsonWriter },
	fromJ = { if (it.isJsonNull) null else it.asBoolean },
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )

  inner class JsonJsonPropN<J: Json<*>>(
	default: Any? = NO_DEFAULT,
	optional: Boolean = false,
	noload: Boolean = false,
	builder: GsonParser<J>,
	set: ((J?)->J?)? = null,
	get: ((J?)->J?)? = null
  ): JsonProperty<J?>(
	toJ = { it?.toJson() ?: NullJsonWriter },
	fromJ = {
	  if (it.isJsonNull) null else builder.fromGson(it)
	},
	optional = optional,
	noload = noload,
	default = default,
	set = set,
	get = get
  )


  inner class JsonIntListProp(
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<Int>(),
	size: Int? = null
  ): JsonProperty<List<Int>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }

	  it.toJsonWriter()
	},
	fromJ = {
	  it.asJsonArray.map { it.asInt }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inner class JsonLongListProp(
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<Long>(),
	size: Int? = null
  ): JsonProperty<List<Long>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }

	  it.toJsonWriter()
	},
	fromJ = {
	  it.asJsonArray.map { it.asLong }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inner class JsonStringListProp(
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<String>(),
	size: Int? = null
  ): JsonProperty<List<String>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }
	  it.toJsonWriter()
	},
	fromJ = {
	  it.asJsonArray.map { it.asString }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inner class JsonDoubleListProp(
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<Double>(),
	size: Int? = null
  ): JsonProperty<List<Double>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }
	  it.toJsonWriter()
	},
	fromJ = {
	  it.asJsonArray.map { it.asDouble }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inner class JsonBoolListProp(
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<Boolean>(),
	size: Int? = null
  ): JsonProperty<List<Boolean>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }
	  it.toJsonWriter()
	},
	fromJ = {
	  it.asJsonArray.map { it.asBoolean }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inner class JsonJsonListProp<J: Json<*>>(
	builder: GsonParser<J>,
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<J>(),
	size: Int? = null
  ): JsonProperty<List<J>>(
	toJ = {
	  if (size != null) {
		require(it.size == size)
	  }
	  it.toJsonWriter(builder as? JsonProxyMap<*>)
	},
	fromJ = {
	  it.asJsonArray.map {
		builder.fromGson(it)
	  }.toMutableList().also {
		if (size != null) require(it.size == size)
	  }
	},
	optional = optional,
	noload = noload,
	list = true,
	default = default
  )

  inline fun <reified J: Json<in J>> jjLiProp(
	builder: GsonParser<J>? = null,
	optional: Boolean = false,
	noload: Boolean = false,
	default: Any? = listOf<J>(),
	size: Int? = null
  ): JsonJsonListProp<J> {
	val defBuild = object: GsonParser<J> {
	  override fun fromGson(jv: JsonElement): J {
		return jv.deserialize()
	  }
	}
	//	(JsonElement) -> J = { it.deserialize<J>() }
	return JsonJsonListProp<J>(
	  builder = builder ?: defBuild,
	  optional = optional,
	  noload = noload,
	  default = default,
	  size = size
	)
  }
}


@JvmName("toJsonWriterInt")
@ExperimentalContracts
fun List<Int>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}


@ExperimentalContracts
fun List<Long>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterBoolean")
@ExperimentalContracts
fun List<Boolean>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterDouble")
@ExperimentalContracts
fun List<Double>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@JvmName("toJsonWriterString")
@ExperimentalContracts
fun List<String>.toJsonWriter(): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { it.toJsonWriter() })
}

@ExperimentalContracts
fun List<Json<*>>.toJsonWriter(
  proxyMap: JsonProxyMap<*>? = null
): ListJsonWriter<JsonWriter> {
  return ListJsonWriter(map { tosave ->
	if (proxyMap != null && tosave::class.simpleName in proxyMap.proxies.keys.map { it.simpleName }) {
	  JsonObject().apply {
		addProperty("id", (tosave as Identified).id)
		addProperty(TYPE_KEY, tosave::class.simpleName!!)
	  }.toJsonWriter() // .toJsonString()
	  //	  object {
	  //
	  //	  }
	} else {
	  tosave.toJson()
	}
	//	tosave.toJson()
  })
}


@ExperimentalContracts
interface Json<T: Json<T>> {
  /*
	//	example when I remove prop from model:
	init {
	  json.ignoreKeysOnLoad.add("index")
	}*/
  val json: JsonModelBase<T>

  fun toJson(): JsonWriter {
	return when (json) {
	  is JsonModel<T>      -> {
		@Suppress("UNCHECKED_CAST")
		var m = (json as JsonModel<T>).props.associate {
		  val k = it.key.toJsonWriter()
		  val v = it.toJ.invoke(this as T)
		  //		  println("K:${k.s}")
		  //		  println("V:${v.toJsonString()}")
		  k to v
		}
		m = if ((json as JsonModel<T>).typekey != null) {
		  m.toMutableMap().plus(TYPE_KEY.toJsonWriter() to (json as JsonModel<T>).typekey!!.toJsonWriter())
		} else m
		MapJsonWriter(m)
	  }
	  is JsonArrayModel<*> -> {
		@Suppress("UNCHECKED_CAST")
		(json as JsonArrayModel<T>).prop.toJ.invoke(this as T)
	  }
	}

  }

  fun loadProperties(
	jo: JsonElement,
	usedTypeKey: Boolean = false,
	pretendAllPropsOptional: Boolean = true
  ) {

	when (json) {
	  is JsonModel<T>      -> {


		val loaded = mutableListOf<String>()
		jo.asJsonObject.entrySet().forEach {
		  if (usedTypeKey && it.key == TYPE_KEY) return@forEach

		  if (it.key !in (json as JsonModel<T>).ignoreKeysOnLoad) {
			@Suppress("UNCHECKED_CAST")
			(this as T).apply {
			  (json as JsonModel<T>)[it.key].fromJ.invoke(this, it.value)
			  loaded += it.key
			}
		  }
		}
		if (!pretendAllPropsOptional) {
		  val json4debug = (json as JsonModel<T>)
		  json4debug.props.filter { it.key !in loaded }.forEach {
			if (!it.optional) {
			  err("json property $it is not optional")
			}
		  }
		}

		//		assert(json4debug.props.filter { it.key !in loaded }.all { it.optional }) {
		//		  "expected to find key ${}"
		//		}
	  }
	  is JsonArrayModel<*> -> {
		@Suppress("UNCHECKED_CAST")
		(json as JsonArrayModel<T>).prop.fromJ.invoke(this as T, jo.asJsonArray)
	  }
	}

	onload()


  }

  fun onload() = Unit

}


@ExperimentalContracts
class JsonProp<T: Json<T>>(
  val key: String,
  val toJ: T.()->JsonWriter,
  val fromJ: T.(JsonElement)->Unit,
  val optional: Boolean = false,
) {
  override fun toString() = toStringBuilder(::key, ::optional)
  var d: SuperDelegateBase<*, *>? = null
}


@ExperimentalContracts
class JsonArrayProp<T: Json<T>>(
  val toJ: T.()->JsonWriter,
  val fromJ: T.(com.google.gson.JsonArray)->Unit
)


@ExperimentalContracts
interface JsonArray<T: Any> {

  val json: Triple<
		(JsonElement)->Unit,
		(T)->JsonWriter,
		()->Sequence<T>
	  >

  fun toJson() = json.third().map { json.second(it) }
  fun loadElements(ja: com.google.gson.JsonArray) {
	ja.forEach {
	  json.first(it)
	}
  }
}

val TYPE_KEY = "type"

interface GsonParser<T: Any>: Builder<T> {
  fun fromGson(jv: JsonElement): T
}


@ExperimentalContracts
inline fun <reified T: Json<out T>> JsonElement.deserialize(
  superclass: KClass<T>,
  typekey: String? = null /*for migration*/
): T {
  //  val r = jv.deserialize()
  //  val o = jv.asJsonObject
  val type = typekey ?: asJsonObject[TYPE_KEY].asString

  /*1.5*/
  /*val skcls = Resource::class.sealedSubclasses*/

  val skcls = superclass.subclasses()

  val c = skcls.first {
	it.simpleName == type
  }


  val instance = c.createInstance()
  //  println("instance is a ${instance::class.qualifiedName}")
  instance.loadProperties(this, usedTypeKey = true)
  //  println("nope, did not get here")
  return instance
}


@ExperimentalContracts
inline fun <reified T: Json<in T>> JsonElement.deserialize(): T {
  require(T::class.hasAnnotation<NoArgConstructor>()) {
	"${T::class} must be annotated with ${NoArgConstructor::class}"
  }
  val o = T::class.createInstance()
  o.loadProperties(this)
  return o
}


@ExperimentalContracts
fun JsonElement.toJsonWriter(): GsonElementJsonWriter {
  return GsonElementJsonWriter(this)
}


@ExperimentalContracts
object NullJsonWriter: JsonWriter() {
  override fun toJsonString(): String {
	return "null"
  }

}


interface ToJsonString {
  fun toJsonString(): String
}

@ExperimentalContracts
sealed class JsonWriter: ToJsonString {

  //  class MJsonWriter(val o: matt.json.custom.Json<*>): JsonWriter() {
  //	override fun toJsonString(): String {
  //	  return MapJsonWriter(o.toJson().map { it.key.matt.json.custom.toJsonWriterInt() to it.value }.toMap()).toJsonString()
  //	}
  //  }

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

  class StringJsonWriter(val s: String): JsonWriter() {
	override fun toJsonString() = Render.renderString(s)
  }

  class NumberJsonWriter(val n: Number): JsonWriter() {
	override fun toJsonString() = n.toGson()
  }

  class BooleanJsonWriter(val b: Boolean): JsonWriter() {
	override fun toJsonString() = b.toGson()
  }

  class ListJsonWriter<T: JsonWriter>(
	val l: List<T>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "[${l.joinToString(",") { it.toJsonString() }}]"
	}

  }

  class ArrayJsonWriter<T: JsonWriter>(
	val l: Array<T>
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "[${l.joinToString(",") { it.toJsonString() }}]"
	}

  }

  class GsonElementJsonWriter(
	val e: JsonElement
  ): JsonWriter() {

	override fun toJsonString(): String {
	  return gson.toJson(e)
	}

  }

  class GsonArrayWriter<T: JsonWriter>(
	val jarray: com.google.gson.JsonArray
  ): JsonWriter() {


	override fun toJsonString(): String {
	  return "[${jarray.toList().joinToString(",") { it.toGson() }}]"
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

@ExperimentalContracts
fun <K: JsonWriter, V: JsonWriter> Map<K, V>.toJsonWriter() = MapJsonWriter(this)

@ExperimentalContracts
fun String.toJsonWriter() = StringJsonWriter(this)

@ExperimentalContracts
fun Number.toJsonWriter() = NumberJsonWriter(this)

@ExperimentalContracts
fun Boolean.toJsonWriter() = BooleanJsonWriter(this)

@ExperimentalContracts
fun <T: Any> JsonArray<T>.toJsonWriter() = ListJsonWriter(this.toJson().toList())


@ExperimentalContracts
interface JsonPropMap<T> {
  fun toJson(): Map<KProperty<Any?>, JsonWriter>
  fun toJsonWriter() = JsonPropMapWriter(this)
}

@ExperimentalContracts
sealed class JsonModelBase<T: Json<T>>

@ExperimentalContracts
object JsonModelBaseShouldBeSealed {
  init {
	if (KotlinVersion.CURRENT.isAtLeast(1, 5)) {
	  err("OK NOW I CAN MAKE ${JsonModelBase::class} A SEALED INTERFACE")
	}
  }
}

@ExperimentalContracts
class JsonArrayModel<T: Json<T>>(
  val prop: JsonArrayProp<T>
): JsonModelBase<T>()


@ExperimentalContracts
class JsonModel<T: Json<T>>(
  var typekey: String?,
  vararg props: JsonProp<T>,
  val ignoreKeysOnLoad: MutableList<String> = mutableListOf() /*same thing as marking prop as noload*/
): JsonModelBase<T>() {
  val props = mutableListOf(*props)

  init {
	require(props.map { it.key }.toSet().size == props.map { it.key }.size)
  }

  operator fun get(key: String?) = props.firstOrNull { it.key == key }.let {
	require(it != null) {
	  "$this is missing a JsonProp for \"$key\""
	}
	it
  }
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


@ExperimentalContracts
fun <T: JsonWriter> T.toGsonElement(): JsonElement {
  val json = toJsonString()
  return GsonBuilder()
	  .serializeNulls()
	  .create()
	  .fromJson(json, JsonElement::class.java)
}


interface JsonProxyMap<T: Any>: GsonParser<T> {
  val proxies: Map<KClass<out T>, List<T>>
}