@file:Suppress("UnstableApiUsage")

modtype = LIB

apis(
  ":k:klib".jvm()
)

dependencies {
  //  api(projects.kj.kjlib)
  //  api(projects.kj.async)
  //  api(libs.gson)
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
  //  implementation(projects.kj.reflect)
  //  projectOrLocalMavenJVM("api", ":k:klib")
  implementation(kotlin("reflect"))
}

plugins {
  kotlin("plugin.serialization")
}