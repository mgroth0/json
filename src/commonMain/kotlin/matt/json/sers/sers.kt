package matt.json.sers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import matt.json.ser.JsonArraySerializer
import matt.json.ser.MySerializer
import matt.obs.col.olist.BasicObservableListImpl
import matt.obs.prop.BindableProperty

class BindablePropertySerializer<T>(val serializer: KSerializer<T>):
  MySerializer<BindableProperty<T>>(BindableProperty::class) {
  override fun deserialize(jsonElement: JsonElement): BindableProperty<T> {
	return BindableProperty(Json.decodeFromJsonElement(serializer, jsonElement))
  }

  override fun serialize(value: BindableProperty<T>): JsonElement {
	return Json.encodeToJsonElement(serializer, value.value)
  }

}

class BasicObservableListImplSerializer<E: Any>(val serializer: KSerializer<E>):
  JsonArraySerializer<BasicObservableListImpl<E>>(BasicObservableListImpl::class) {
  override fun deserialize(jsonArray: JsonArray): BasicObservableListImpl<E> {
	return BasicObservableListImpl(jsonArray.map { Json.decodeFromJsonElement(serializer, it) })
  }

  override fun serialize(value: BasicObservableListImpl<E>): JsonArray {
	return JsonArray(value.map { Json.encodeToJsonElement(serializer, it) })
  }

}