apis {
  klib
  file
}

dependencies {
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
  implementation(kotlin("reflect"))
}