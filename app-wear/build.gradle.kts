plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wear"
        minSdk = 26
        targetSdk = 36
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
<<<<<<< HEAD
    
    // Core Wear OS dependencies - using compatible versions
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear.tiles:tiles:1.4.0")
    // ProtoLayout (modern Tiles UI API)
    implementation("androidx.wear.protolayout:protolayout:1.0.0")
    implementation("androidx.wear.protolayout:protolayout-material:1.0.0")
    
    // Google Play Services for Wear
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    
    // AndroidX dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    
    // Concurrent futures to create ListenableFuture without Guava dependency
    implementation("androidx.concurrent:concurrent-futures:1.2.0")
=======
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    // Wear Tiles API
    implementation("androidx.wear.tiles:tiles:1.3.0")
    implementation("androidx.wear.tiles:tiles-material:1.3.0")
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Google Guava for ListenableFuture
    implementation("com.google.guava:guava:32.1.3-android")
>>>>>>> fb9df69ed72c58ee2bc168e83960bedd7bd752db
    
    // Testing dependencies
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
