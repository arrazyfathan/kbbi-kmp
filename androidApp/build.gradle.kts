import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val appVersion =
    Properties().apply {
        providers
            .fileContents(rootProject.layout.projectDirectory.file("version.properties"))
            .asText
            .get()
            .reader()
            .use(::load)
    }

fun Properties.requiredVersionPart(name: String): Int =
    requireNotNull(getProperty(name)) { "$name is missing from version.properties" }
        .toInt()
        .also { require(it >= 0) { "$name must be non-negative" } }

val versionMajor = appVersion.requiredVersionPart("VERSION_MAJOR")
val versionMinor = appVersion.requiredVersionPart("VERSION_MINOR")
val versionPatch = appVersion.requiredVersionPart("VERSION_PATCH")
val appVersionCode = appVersion.requiredVersionPart("VERSION_CODE")
val appVersionName = "$versionMajor.$versionMinor.$versionPatch"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.arrazyfathan.kbbi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.arrazyfathan.kbbi"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
