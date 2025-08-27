plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.transcriber.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.transcriber.mobile"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
        getByName("test") {
            java.srcDir("src/test/kotlin")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.google.mlkit:common:18.10.0")
    implementation("com.google.mlkit:translate:17.0.1")
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
