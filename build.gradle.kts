// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

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
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("ktlintFormat")
    }
}
