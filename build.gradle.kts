plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
}

group = "com.tambapps.pokemon"
version = "1.0-SNAPSHOT"

subprojects {
  group = rootProject.group
  version = rootProject.version

  repositories {
    mavenCentral()
  }

  // Apply common configuration to all subprojects that use Kotlin Multiplatform
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
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
        commonTest {
          dependencies {
            implementation(kotlin("test"))
          }
        }
      }
    }
  }
}