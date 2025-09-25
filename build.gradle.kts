// Top-level build file where you can add configuration options common to all sub-projects/modules.
tasks.named("build") {
    dependsOn("ktlintCheck") // ahora build ejecuta ktlintCheck antes de compilar
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)          // falla el build si hay errores
    enableExperimentalRules.set(true)  // reglas experimentales
    filter {
        exclude("**/generated/**")    // excluir carpetas
    }
}