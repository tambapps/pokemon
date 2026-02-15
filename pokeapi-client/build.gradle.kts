plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  `maven-publish`
}

val ktorVersion = "3.0.3"

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(":pokemon-core"))
        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("serializationVersion")}")
      }
    }
    jvmMain {
      dependencies {
        implementation("io.ktor:ktor-client-cio:$ktorVersion")
      }
    }
    iosMain {
      dependencies {
        implementation("io.ktor:ktor-client-darwin:$ktorVersion")
      }
    }
    jsMain {
      dependencies {
        implementation("io.ktor:ktor-client-js:$ktorVersion")
      }
    }
    wasmJsMain {
      dependencies {
        implementation("io.ktor:ktor-client-js:$ktorVersion")
      }
    }
    commonTest {
      dependencies {
        implementation("io.ktor:ktor-client-mock:$ktorVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
      }
    }
  }
}
