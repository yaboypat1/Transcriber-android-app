plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wear"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
        getByName("test") {
            java.srcDir("src/test/java")
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("androidx.wear.tiles:tiles:1.2.0")
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Wear OS Tiles dependencies
    implementation("androidx.wear.tiles:tiles-material:1.2.0")
    implementation("com.google.android.horologist:horologist-tiles:0.4.8")
    
    // Testing dependencies
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
