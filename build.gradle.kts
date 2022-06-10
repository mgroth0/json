

@file:Suppress("UnstableApiUsage")
modtype = LIB
dependencies {
//  api(projects.kj.kjlib)
//  api(projects.kj.async)
//  api(libs.gson)
  api(libs.kotlinx.serialization.json)
  implementation(libs.fx.base)
//  implementation(projects.kj.reflect)
}

plugins {
  kotlin("plugin.serialization")
}