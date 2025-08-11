plugins {
  kotlin("multiplatform")
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
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}