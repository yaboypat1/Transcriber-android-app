plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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
    // Wear Tiles API
    implementation("androidx.wear.tiles:tiles:1.3.0")
    implementation("androidx.wear.tiles:tiles-material:1.3.0")
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Google Guava for ListenableFuture
    implementation("com.google.guava:guava:32.1.3-android")
    
    // Testing dependencies
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
