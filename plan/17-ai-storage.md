# 17 — Add secure AI configuration storage

**Depends on:** 05, 13, 16  
**Outcome:** Provider preferences persist safely on native targets; web keys last one session.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

Completed in shared common code:

- Added provider add/edit/remove, backend/custom mode selection, model selection, connection testing, and configuration validation use cases. Plan 18 owns their UI.
- Added platform configuration storage. Android encrypts credentials with an AES-GCM key held in Android Keystore; iOS stores credentials in Keychain; macOS/Linux desktop use the OS credential store; browser JS/Wasm keep credentials in a module memory map. Provider metadata is stored separately and never contains API keys.
- Custom provider mode remains unavailable until a valid credential is present; backend mode remains the default and works without custom credentials.
- Credential-store write failures are returned instead of reporting a successful save.

Not implemented: a same-origin browser proxy. This repository has no server module or web-host deployment configuration, so there is no defined runtime in which to install or deploy that endpoint. Plan 26 already owns web hosting; choose the deployment runtime there before implementing the proxy. The Plan 18 browser settings UI must explain that custom keys are cleared on reload and request re-entry.

## Verification

- [x] Configuration use-case tests cover HTTPS-only URL validation, userinfo/query/completion-path rejection, and rejecting invalid settings before persistence. `./gradlew --no-daemon :shared:jvmTest --tests '*AiConfigurationUseCasesTest' --console=plain` passed.
- [ ] Person verifies native keys survive restart and are absent from ordinary preferences/logs; browser keys clear on reload. Device and Simulator checks must be done manually by a person; browser runtime review also remains manual.
- [ ] Compare Plan 18’s delivered configuration screens and behavior directly with the Android source at equivalent viewport sizes. Device or Simulator interaction must be done manually by a person.
- [ ] Proxy tests cover valid request, blocked target, redirects, oversized body, timeout, and no credential logging after the hosting runtime is selected and endpoint is added.

Android debug build, iOS Simulator framework, Desktop JVM, browser JS/Wasm shared and web source, and iOS/JS/Wasm test-source compilation passed. Final compile/test log: `/tmp/kbbi-plan17-final-check.log`. No app/device/browser interaction was performed.

- Native keys survive restart and are absent from ordinary preference files/logs; browser keys do not survive reload.
- Proxy tests cover valid request, blocked target, redirect, oversized body, timeout, and no credential logging.
