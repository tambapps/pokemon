plugins {
  kotlin("multiplatform")
  `maven-publish`
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(":pokemon-core"))
      }
    }
  }
}