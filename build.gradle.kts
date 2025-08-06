plugins {
    kotlin("multiplatform") version "2.2.0" apply false
}

group = "com.tambapps.pokemon"
version = "1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
    
    repositories {
        mavenCentral()
    }
}