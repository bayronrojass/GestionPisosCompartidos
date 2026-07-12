// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("com.google.gms.google-services") version "4.4.4" apply false
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.5")
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    ktlint {
        verbose.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        enableExperimentalRules.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/test/**")
            exclude("**/androidTest/**")
        }
    }

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("detekt.yml"))
        parallel = true
    }

    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("ktlintFormat")
    }
}
