plugins {
    kotlin("jvm") version "2.0.21" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    // Skip compilation; source files are illustrative only
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        enabled = false
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
