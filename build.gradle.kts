

@file:Suppress("UnstableApiUsage")
modtype = LIB
dependencies {
//  api(projects.kj.kjlib)
//  api(projects.kj.async)
//  api(libs.gson)
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
//  implementation(projects.kj.reflect)
  projectOrLocalMavenJVM("api", ":k:klib")
}

plugins {
  kotlin("plugin.serialization")
}