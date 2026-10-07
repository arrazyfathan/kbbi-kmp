import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain = getByName("commonMain")
        val webMain =
            create("webMain") {
                dependsOn(commonMain)
                dependencies {
                    implementation(projects.shared)
                    implementation(libs.compose.ui)
                    implementation(libs.koin.core)
                }
            }
        getByName("jsMain").dependsOn(webMain)
        getByName("wasmJsMain").dependsOn(webMain)
    }
}
