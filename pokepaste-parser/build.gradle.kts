plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  `maven-publish`
}

kotlin {
  jvmToolchain(21)

  // JVM target
  jvm()

  // iOS targets
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  // Web target
  js(IR) {
    browser()
    nodejs()
  }
  
  // WASM target
  wasmJs {
    browser()
    nodejs()
  }

  sourceSets {
    commonMain {
      dependencies {
        api(project(":pokemon-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${property("serializationVersion")}")
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}