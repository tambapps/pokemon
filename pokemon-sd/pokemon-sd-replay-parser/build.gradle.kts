plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  `maven-publish`
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("serializationVersion")}")
      }
    }
  }
}