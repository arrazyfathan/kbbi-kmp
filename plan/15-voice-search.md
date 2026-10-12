# 15 — Add voice search

**Depends on:** 05, 08, 13  
**Outcome:** Android voice input uses native recognition; typed search remains usable everywhere. Other targets' voice input is temporarily deferred.

**Current rollout (2026-10-12):** Per the person's request, voice search is enabled and manually accepted on Android only. iOS, desktop JVM, browser JS and browser Wasm voice search are temporarily disabled. Those targets display a search-only button, do not show the microphone/listening sheet or install voice lifecycle observers, and resolve `SpeechInput` to `UnsupportedSpeechInput`. The iOS/browser recognition implementations are retained for future re-enabling; their verification is deferred. Typed search remains available on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Ported source microphone/search-button morph, listening bottom sheet and pulse animation, partial text, final candidates, dictionary-aware normalization, cancellation, permission-denied, empty/busy/failed and unavailable copy in English and Indonesian. As in the source, final speech fills the search field and suggestions; the person confirms it by tapping Search, which uses the existing search/history use case.
2. [x] Android uses `SpeechRecognizer` with source `id-ID` locale, partial results, five alternatives, and source silence timings. Added microphone permission and recognition-service package visibility. The Home platform leaf owns its Activity Result permission launcher; the sheet opens only after permission is granted. Recognition sessions cancel/destroy in coroutine cleanup.
3. [x] Implemented the iOS Speech/`AVAudioEngine` adapter, permission flow, background audio operations, cleanup and localized purpose strings. Temporarily disabled at the Home UI and DI binding per the person's request after Simulator recognition failed; implementation is retained and runtime acceptance is deferred.
4. [x] Implemented the browser JS/Wasm Web Speech adapter, partial/final results, availability checks and cancellation. Temporarily disabled at the Home UI and DI binding per the person's request; implementation is retained and runtime acceptance is deferred.
5. [x] Desktop JVM has no bundled compatible speech engine or configured transcription service. Its microphone/listening UI is temporarily disabled and `SpeechInput` remains unavailable; typed search is available. A future desktop engine can implement `SpeechInput` without changing Home UI.
6. [x] Caller-owned recognition sessions stop on cancellation, Home disposal, and lifecycle stop. Stale callbacks cannot mutate a cancelled/replaced request. Spoken transcripts are not logged; iOS diagnostics record stage, OS version/Simulator flag, input format, buffer count, signal-presence flag, NSError domain/code and underlying domain/code (never audio samples, transcripts, descriptions or userInfo dumps); lookup and translation HTTP logging is filtered so a confirmed spoken query is not echoed in API URLs or response bodies.

## Later implementation on other platforms

These tasks remain open after Android acceptance. Keep the current Android implementation and its completed manual checks. iOS, desktop and browser voice search stay disabled until their follow-up implementation is ready for a person's manual acceptance.

### iOS

- [ ] Resume the retained `IosSpeechInput` adapter and resolve recognition initialization failures using the actual OS/runtime and native error diagnostics. Distinguish Simulator service limitations from failures on supported physical iPhones; do not claim code 300 is fixed from compilation alone.
- [ ] Complete Indonesian recognition, partial/final results, permission handling, bounded recording, audio-session cleanup, cancellation and retry without blocking the main thread.
- [ ] Restore the `IosSpeechInput` Koin binding and enable `isHomeVoiceSearchEnabled` in a candidate build for manual acceptance.
- [ ] Person checks source-identical voice UI/behavior, successful recognition on a supported iPhone, Simulator availability/fallback, denied permission, empty speech, cancellation, backgrounding and repeated retry. Record platform-specific results before marking iOS done.

### Desktop JVM

- [ ] Select and implement a compatible desktop speech engine behind `SpeechInput`, including Indonesian language support and an explicit unavailable outcome on unsupported hosts. Record supported operating systems and any engine/model installation requirements.
- [ ] Implement microphone access, partial/final candidates, cancellation, recording limits, cleanup, retry and permission/service errors through the existing common contract.
- [ ] Replace `UnsupportedSpeechInput` in the desktop Koin binding and enable `isHomeVoiceSearchEnabled` in a candidate build for manual acceptance.
- [ ] Person checks recognition on each supported desktop OS, microphone selection/access, source-identical voice UI/behavior, keyboard interaction, cancellation, window/app lifecycle and typed-search fallback before marking desktop done.

### Browser JS and Wasm

- [ ] Resume the retained `BrowserSpeechInput` adapter and complete Indonesian recognition on supported browsers, standard/prefixed API detection, HTTPS/localhost requirements and microphone permission handling.
- [ ] Confirm partial/final candidate delivery, cancellation, timeout, callback cleanup, page lifecycle and unavailable/denied/error outcomes for both JS and Wasm builds.
- [ ] Restore the `BrowserSpeechInput` Koin binding and enable `isHomeVoiceSearchEnabled` in browser candidate builds for manual acceptance. Unsupported browsers must retain typed search and the source unavailable behavior.
- [ ] Person checks JS and Wasm separately in supported browsers over HTTPS/localhost, compares voice UI/behavior with the Android source, and checks denial, insecure context, unsupported API, cancellation, page backgrounding and retry before marking either browser target done.

### Completion

- [ ] Complete the open common automated-test gate below for the restored adapters and shared normalization/session behavior.
- [ ] Build all five targets, update this plan's rollout status and `parity-inventory.md` with each person's platform evidence, and mark only the accepted targets verified. All device/simulator and runtime UI acceptance remains manual; agents may prepare builds and steps.

## Verification

- [x] Person compares Android's microphone/search icon morph, search-row spacing, listening sheet, pulse animation, detected-text styling, Cancel action, and English/Indonesian copy directly with the Android source at equivalent viewport sizes. Marked done on the person's confirmation, 2026-10-12.
- [x] Person allows Android microphone permission, speaks an Indonesian word, sees partial text and the final normalized query/suggestions, taps Search, and confirms the ordinary detail/history flow. Marked done on the person's confirmation, 2026-10-12.
- [x] Person checks Android permission denial, cancellation/dismissal, empty speech, leaving Home and app backgrounding; checks there is no stale result, retained microphone, duplicate session, or blocked typed search, and retries where the OS permits it. Marked done on the person's confirmation, 2026-10-12.
- [ ] Deferred: person verifies iOS voice search when it is re-enabled. Previous Simulator failures remain recorded below.
- [ ] Deferred: person verifies browser JS and Wasm recognition and fallback when voice search is re-enabled.
- [ ] Deferred: person verifies desktop voice behavior if a compatible recognition engine is added and voice search is re-enabled.
- [ ] Automated tests cover partial/final result, empty result, cancellation, permission denial, unavailable capability and dictionary normalization. Tests have not been added or run for this implementation.
- [x] Android APK, iOS Simulator framework, Xcode iOS app, desktop JVM and both JS/Wasm browser app shells built successfully on 2026-10-12. The built iOS app contains microphone/speech purpose keys and English/Indonesian purpose-string resources. No device or browser interaction performed.

## Platform notes

The recognition notes below describe the retained adapters. iOS/browser voice recognition is currently disabled, and its runtime checks apply only after re-enabling.

Android/iOS recognition-service availability and Indonesian language support depend on the target device. The iOS Simulator may lack a usable microphone/recognition service; a person must confirm actual recognition on a supported iPhone as well as the simulator fallback. Recognition audio is ended explicitly on iOS; partial text inactivity is checked every 2.5 seconds, capture is capped at approximately 12 seconds, and a session waits at most 20 seconds for completion. Android/browser sessions also have a 20-second bound. No device or simulator interaction is performed by agents.

Official platform references: [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [Apple live-audio speech request](https://developer.apple.com/documentation/speech/sfspeechaudiobufferrecognitionrequest), and [Web Speech interim results](https://developer.mozilla.org/en-US/docs/Web/API/SpeechRecognition/interimResults).

## iOS audio regression checks

These iOS checks are deferred while voice search is disabled; the unchecked items below preserve the regression history and do not apply to the current Android-only acceptance.

- [ ] Person retries voice start → speak → finish and start → Cancel several times; confirms the main-thread AVAudioSession hang-risk warning is gone and no overlapping capture or double teardown occurs.
- [ ] Person grants permissions, opens the listening sheet, backgrounds the app, returns, and starts another session; checks the sheet itself does not immediately cancel capture and actual app backgrounding releases the microphone.
- [ ] Person checks a real iPhone and the simulator separately; records any remaining HAL/audio-route or Compose appearance warnings with the reproduction steps. These OS/framework logs must not be treated as agent-verified fixes.

## iOS recognition-failure follow-up

- [ ] Person retries the Simulator and a supported iPhone after the startup/routing changes; records any `KBBI-Speech` stage/domain/code diagnostics if recognition still fails. HAL device-object logs alone do not identify the Speech framework failure.
- [ ] Person checks empty speech, missing recognition assets/service, authorization denial and a busy recognizer produce the appropriate source message.
- [ ] Person checks cancellation/failure messages are transient nonmodal iOS toasts, and presenting a message while the listening sheet closes causes no extra UIKit alert presentation or blocked typed search.

The iOS adapter checks supported Indonesian locales, connects the recognizer before starting the engine, and uses `playAndRecord` with `mixWithOthers` to keep an output route while capturing input. The toast implementation now matches the source’s nonmodal Toast behavior instead of presenting a UIKit alert. Error mapping follows [Apple’s recognition error table](https://developer.apple.com/documentation/speech/sfspeechrecognitiontask/error). Recognition failure is not considered runtime-resolved until a person confirms the retry.

## iOS recognition completion retry

Audio completion now calls both `request.endAudio()` and `task.finish()` after microphone capture stops. [Apple documents `finish()`](https://developer.apple.com/documentation/speech/sfspeechrecognitiontask/finish%28%29) as necessary to finish buffer-based recognition. The task remains attached while awaiting the final result; explicit cancellation still cancels it. Missing microphone buffers at the deadline produce `Unavailable` rather than an empty-speech result.

- [ ] Person rebuilds, says an Indonesian word, pauses and checks that partial recognition completes into the search field before the 20-second deadline.
- [ ] If it fails, person supplies the `KBBI-Speech` lines for `start`, `input-format`, `engine-start`, `end-audio`, `recognition` and `timeout`, whichever appear. `buffers=0` identifies missing capture delivery; `signalDetected=false` with buffers identifies no detected input signal, not a proven Speech-service error. A native recognition domain/code identifies the service failure.
- [ ] Person checks Simulator audio input uses the intended Mac microphone and the host's microphone permission is enabled for Simulator; compares a supported physical iPhone separately.

A [developer report on Apple's forum](https://developer.apple.com/forums/thread/840302) describes unavailable recognition and `kLSRErrorDomain` code 300 on iOS 26.4/26.5 Simulators, with earlier runtimes unaffected in that reporter's tests. This is not Apple confirmation and does not establish the cause here; record the actual runtime/error before attributing a failure to it. This retry remains unverified until a person confirms recognition.

### Reported failure, 2026-10-12 11:23

The person's Simulator log contains two failed attempts with `stage=recognition domain=kLSRErrorDomain code=300 underlyingDomain=kLSRErrorDomain underlyingCode=300`. [Apple defines code 300 as recognizer initialization failure](https://developer.apple.com/documentation/speech/sfspeechrecognitiontask/error). Input format is 48,000 Hz / one channel; that establishes a valid format, not successful microphone-buffer delivery or transcription. The excerpt does not include the new `stage=start` OS version or `end-audio` buffer diagnostics, so the Simulator runtime and use of the latest completion build remain unconfirmed.

The adapter already maps this error to `Unavailable` and releases its capture resources. The `finish()` fix addresses completion after recording and cannot repair a recognizer that fails to initialize. There is no confirmed app-only workaround for this report. A person should record the runtime version, rebuild the latest source, and compare the same Indonesian-word flow on a supported physical iPhone or another Simulator runtime; iOS 26.2 is a comparison candidate based on the developer report, not a guaranteed fix. Voice-recognition acceptance remains unchecked.

## Android-only rollout build

On 2026-10-12, `:shared:compileAndroidMain`, `:shared:compileKotlinIosSimulatorArm64`, `:desktopApp:compileKotlin`, `:webApp:compileKotlinJs` and `:webApp:compileKotlinWasmJs` passed after temporarily disabling non-Android voice UI and bindings. Build log: `/tmp/kbbi-android-only-voice-build.log`. No automated tests or agent device/browser interaction performed.
