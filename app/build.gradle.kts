plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.transcriber"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    testImplementation(kotlin("test"))
}
