// Parent module for Pokemon Showdown related modules
// This module contains sub-modules for parsing and processing Pokemon Showdown replays

subprojects {
  // Apply common configuration to all pokemon-sd submodules
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      sourceSets {
        commonMain {
          dependencies {
            // All pokemon-sd modules depend on pokemon-core
            api(project(":pokemon-core"))
          }
        }
      }
    }
  }
}
