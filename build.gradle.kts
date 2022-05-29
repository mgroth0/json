@file:Suppress("UnstableApiUsage")

dependencies {
  api(projects.kj.kjlib)
  api(projects.kj.async)
//  api(libs.gson)
  api(libs.kotlinx.serialization.json)
}

plugins {
  kotlin("plugin.serialization")
}