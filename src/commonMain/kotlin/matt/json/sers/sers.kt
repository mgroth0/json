package matt.json.sers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import matt.json.ser.MySerializer
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