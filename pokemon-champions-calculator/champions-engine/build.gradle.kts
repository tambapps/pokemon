plugins {
  kotlin("multiplatform")
  `maven-publish`
}

// Damage formula + KO chance computation. Depends on champions-data for
// Pokemon/move/ability/item definitions, but never the other way around.
kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(":pokemon-champions-calculator:champions-data"))
      }
    }
  }
}
