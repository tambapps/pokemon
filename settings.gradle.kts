pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        id("org.jetbrains.kotlin.multiplatform") version kotlinVersion
    }

}

plugins {
    // Automatically resolves and downloads JDK toolchains from Foojay Disco API (see jvmToolchain(...))
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "pokemon"

include(":pokepaste-parser")
include(":pokemon-sd-replay-parser")
include(":pokemon-core")