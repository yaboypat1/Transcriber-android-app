pluginManagement {
    repositories {
        google()
        mavenCentral()
        // Explicit Google Maven endpoint as a fallback
        maven(url = "https://dl.google.com/dl/android/maven2/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        // Explicit Google Maven endpoint as a fallback
        maven(url = "https://dl.google.com/dl/android/maven2/")
    }
}

rootProject.name = "TranscriberAndroidApp"

include("app-mobile", "app-wear", "app")
