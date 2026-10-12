# 16 — Add AI word-study contracts

**Depends on:** 05, 06  
**Outcome:** Backend and custom-provider word-study generation share validated models and errors.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

Completed in shared common code:

- Added word-study/provider models, repository contracts, and `GenerateWordStudyUseCase`; the AI configuration repository interface is available for Plan 17's persistence implementation.
- Added backend provider discovery and generation, plus OpenAI-compatible custom-provider generation through shared Ktor.
- Preserved Indonesian/English prompt rules, KBBI/visitor source formatting, provider attribution, and minimum generated-content validation.
- Added typed provider-not-configured, invalid-content, provider-failure, and timeout errors. Coroutine cancellation propagates.
- Custom-provider keys are sent only as bearer authorization, excluded from request/response logging, and omitted from generated result models.

## Verification

- [x] MockEngine and use-case tests cover provider catalog, backend and custom generation, language/source prompt behavior, invalid content/JSON, provider/HTTP errors, and unsafe or missing credentials. `./gradlew --no-daemon :shared:jvmTest --tests '*WordStudy*' --console=plain` passed.
- [x] Shared request models compile for Android, iOS Simulator, JVM, JS, and Wasm. The Android debug app, iOS framework, Desktop app sources, JS/Wasm shared and web sources, and JS/Wasm tests compiled with `./gradlew --no-daemon :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 :desktopApp:compileKotlin :shared:compileKotlinJs :shared:compileKotlinWasmJs :shared:compileTestKotlinIosSimulatorArm64 :shared:compileTestKotlinJs :shared:compileTestKotlinWasmJs :webApp:compileKotlinJs :webApp:compileKotlinWasmJs --console=plain`.
- [ ] Compare any delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. UI remains owned by Plan 18. Any device or simulator interaction must be done manually by a person.
- [ ] Person manually tests the generated word-study flow on supported devices after Plan 18 exposes it.

The broader `:shared:jvmTest` run completed 52 tests with four failures in existing `PlatformCapabilitiesTest` and `NavigatorTest` cases; the isolated Plan 16 tests passed. No device or Simulator test was run. Logs: `/tmp/kbbi-plan16-wordstudy-tests.log` and `/tmp/kbbi-plan16-platform-compile.log`.
