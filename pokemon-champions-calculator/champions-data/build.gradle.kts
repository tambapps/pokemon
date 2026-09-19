plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  `maven-publish`
}

// Pokedex/movedex/ability/item data for the Champions format.
// Kept separate from champions-engine because this data changes on every
// Regulation update, independently of the (much more stable) damage formula.
kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(":pokemon-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("serializationVersion")}")
      }
    }
  }
}
