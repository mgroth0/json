@file:Suppress("UnstableApiUsage")

modtype = LIB

apis(
  ":k:klib".jvm(),
  ":k:file".jvm()
)

dependencies {
  //  api(projects.k.kjlib)
  //  api(projects.k.async)
  //  api(libs.gson)
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
  //  implementation(projects.k.reflect)
  //  projectOrLocalMavenJVM("api", ":k:klib")
  implementation(kotlin("reflect"))
}

plugins {
  kotlin("plugin.serialization")
}