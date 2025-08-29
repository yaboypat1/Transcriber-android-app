plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.android") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")

        extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class) {
            version.set("1.2.1")
            android.set(true)
            enableExperimentalRules.set(true)
            filter {
                exclude("**/build/**")
            }
        }

        extensions.configure(io.gitlab.arturbosch.detekt.extensions.DetektExtension::class) {
            toolVersion = libs.versions.detekt.get()
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom(files(rootProject.file("detekt.yml")))
            baseline = null
        }

        tasks.named("check").configure {
            dependsOn("ktlintCheck", "detekt")
        }
    }
}
