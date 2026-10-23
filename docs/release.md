# Release and delivery

This repository builds release artifacts but does not publish them. Publishing requires account access, signing material, and hosting configuration that are not stored in the repository.

## Version and Android identity

Update `version.properties` for each release. Android's application ID is `com.arrazyfathan.kbbi.kmp`; keep it stable so KMP installs independently from the original Android app. `VERSION_CODE` must increase for every Android production release. The Android release build uses R8 minification and resource shrinking. It can produce an unsigned review APK when no signing inputs are present; supplying only some signing inputs is an error.

Expected Android signing inputs are `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. Keep the keystore outside the checkout and inject these values through a protected CI environment. Firebase configuration is optional for local builds and is supplied as an ignored `androidApp/google-services.json` file when enabled. The Android update installer also requires the user to allow installs from KBBI in system settings; the manifest declares `REQUEST_INSTALL_PACKAGES` for this sideload update path.

## Build artifacts

Use JDK 17 and the commands below to prepare artifacts. The `KBBI KMP CI` workflow runs shared and Android unit tests, Android debug and minified unsigned release builds, DMG/MSI/DEB desktop packaging on native runners, a Linux desktop launch smoke, browser JavaScript/Wasm production bundles, and an unsigned iOS Simulator build. It uploads CI artifacts for review and does not create a store upload or publish a release.

```sh
cp local.properties.example local.properties
./gradlew --no-daemon :androidApp:assembleRelease -PappEnvironment=production
./gradlew --no-daemon :shared:linkReleaseFrameworkIosArm64
./gradlew --no-daemon :desktopApp:createDistributable
./gradlew --no-daemon :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution
```

Desktop native installers are host-specific. Build DMG on macOS, MSI on Windows, and DEB on Linux. The iOS archive and export require a valid Apple team, provisioning profile, and signing certificate. Use the Xcode project with `Release` configuration after injecting the intended release URL and signing configuration.

## Web deployment and AI proxy

The JavaScript and Wasm distribution directories are static assets. Deploy them to an HTTPS origin with SPA fallback to `index.html`, correct JavaScript/Wasm MIME types, and service-worker scope covering the application. The hosting origin and cache policy must be selected before deployment configuration can be committed.

Browser custom-provider credentials are session-only. A same-origin AI proxy is not configured in this repository; choose and secure its hosting runtime before enabling proxy-backed browser requests. Do not put provider credentials in browser bundles or repository files.

## Release review

Before publishing, verify the package identity and version, inspect permissions and the built artifact, compare target behavior with the Android source, and complete the manual target acceptance matrix in [plan 26](../plan/26-delivery-verification.md). Never commit signing keys, API credentials, service account files, Firebase app configuration, or machine-specific paths.
