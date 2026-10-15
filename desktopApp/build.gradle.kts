import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.core)
}

compose.desktop {
    application {
        mainClass = "com.arrazyfathan.kbbi.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KBBI"
            packageVersion = "1.0.0"
        }
    }
}

// Register the custom URL scheme in the generated macOS app bundle. This is
// applied after jpackage creates the .app and before its DMG is assembled.
tasks.matching { it.name == "createDistributable" }.configureEach {
    notCompatibleWithConfigurationCache("Patches the generated macOS Info.plist after jpackage creates the app bundle")
    doLast {
        val appRoot =
            layout.buildDirectory
                .dir("compose/binaries/main/app")
                .get()
                .asFile
        val infoPlist =
            fileTree(appRoot).matching { include("**/*.app/Contents/Info.plist") }.files.singleOrNull()
                ?: return@doLast
        val document =
            javax.xml.parsers.DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(infoPlist)
        val plistDict =
            document.documentElement.getElementsByTagName("dict").item(0) as org.w3c.dom.Element
        val key = document.createElement("key").apply { textContent = "CFBundleURLTypes" }
        val types = document.createElement("array")
        val type = document.createElement("dict")
        type.appendChild(document.createElement("key").apply { textContent = "CFBundleURLName" })
        type.appendChild(
            document.createElement("string").apply { textContent = "com.arrazyfathan.kbbi" },
        )
        type.appendChild(document.createElement("key").apply { textContent = "CFBundleURLSchemes" })
        type.appendChild(
            document.createElement("array").apply {
                appendChild(document.createElement("string").apply { textContent = "kbbi" })
            },
        )
        types.appendChild(type)
        plistDict.appendChild(key)
        plistDict.appendChild(types)
        javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(
            javax.xml.transform.dom
                .DOMSource(document),
            javax.xml.transform.stream
                .StreamResult(infoPlist),
        )
    }
}
