@file:Suppress("UnstableApiUsage")

modtype = LIB

apis {
  klib
  file
}

dependencies {
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
  implementation(kotlin("reflect"))
}

plugins {
  kotlin("plugin.serialization")
}