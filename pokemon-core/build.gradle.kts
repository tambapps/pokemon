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
    
    sourceSets {
        commonMain {
            dependencies {
                // Core dependencies that will be shared
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}