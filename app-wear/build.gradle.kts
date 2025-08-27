plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

sourceSets {
    val main by getting {
        kotlin.setSrcDirs(listOf("src/main/kotlin"))
    }
    val test by getting {
        kotlin.setSrcDirs(listOf("src/test/java"))
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
