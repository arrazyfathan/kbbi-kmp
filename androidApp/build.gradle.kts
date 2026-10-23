import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// CI supplies this project's own Firebase configuration; source credentials are never copied.
if (file("google-services.json").isFile) {
    pluginManager.apply("com.google.gms.google-services")
    pluginManager.apply("com.google.firebase.crashlytics")
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
fun releaseInput(name: String): String? =
    providers.gradleProperty(name).orNull?.takeIf(String::isNotBlank)
        ?: providers.environmentVariable(name).orNull?.takeIf(String::isNotBlank)

val releaseSigning =
    listOf(
        releaseInput("ANDROID_KEYSTORE_PATH"),
        releaseInput("ANDROID_KEYSTORE_PASSWORD"),
        releaseInput("ANDROID_KEY_ALIAS"),
        releaseInput("ANDROID_KEY_PASSWORD"),
    )
require(releaseSigning.all { it == null } || releaseSigning.all { it != null }) {
    "Provide all four Android release signing inputs, or omit all four to build an unsigned artifact."
}
val hasReleaseSigning = releaseSigning.all { it != null }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(projects.shared)
    implementation(projects.widgetCore)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.glance.appwidget)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.arrazyfathan.kbbi"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.arrazyfathan.kbbi.kmp"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }
    bundle {
        language {
            // The app has an in-app language picker, so package all supported
            // language resources instead of relying on the device locale split.
            enableSplit = false
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseSigning[0]!!)
                storePassword = releaseSigning[1]
                keyAlias = releaseSigning[2]
                keyPassword = releaseSigning[3]
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
