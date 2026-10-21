# 24 — Add campaigns and update notifications

**Depends on:** 13, 14, 19, 20, 21  
**Outcome:** Editorial and update messages respect user choices and open the right content on every target.
**Status:** iOS client and server delivery code implemented 2026-10-20; production configuration and human device acceptance remain open. Browser/feed work remains open.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation baseline — 2026-10-20

Plan 24 owns notification preferences, payload validation, subscriptions, delivery, deduplication, fallback announcements, and action routing. [Plan 25](25-app-update.md) owns release fetching, update policy/cadence, prompts, package selection, download, and installation. An update push is advisory and must never authorize installation or supply a trusted download URL.

| Area | Inspected baseline |
| --- | --- |
| Source adapters | `app/src/main/java/com/arrazyfathan/kbbi/notifications/EditorialMessagingService.kt`, `EditorialTopicWorker.kt`, `AppUpdateTopicWorker.kt`, `AppUpdatePushWorker.kt` in the Android source |
| Source settings | `feature/settings/domain/.../model/NotificationSettings.kt`, `feature/settings/data/.../NotificationSettingsDataStore.kt`, and the Settings ViewModel permission-result actions |
| Target settings | `shared/src/commonMain/kotlin/com/arrazyfathan/kbbi/feature/settings/` already persists campaign=false and updates=true and renders both switches; actions currently only save preferences |
| Target navigation | `navigation/DestinationParser.kt`, `IncomingNavigation.kt`, and `core/platform/PlatformCapabilities.kt` provide destinations and an acknowledged incoming-request queue; iOS push actions use stable IDs and update announcements have a dedicated detail route pending Plan 25 |
| Native hosts | Android already has reminder workers/launch handling; iOS has `KBBIAppDelegate`, notification-response handling, local reminders, and Firebase reporting integration |
| Browser | `webApp/src/webMain/resources/service-worker.js` owns shell/runtime caching; JS and Wasm share `webMain`; extend the existing worker |
| Backend | `/Users/macintosh/Personal/NodeJs/kbbi-api` contains the campaign admin API and Android topic sender; Plan 24 now adds authenticated iOS update announcements, installation registration, and per-install APNs delivery. No production credentials were changed |
| Inventory | F20 in [parity-inventory.md](parity-inventory.md) is partially implemented and still needs platform acceptance; existing reminder/external-entry behavior must remain intact |

Shared Kotlin paths below are relative to `shared/src/commonMain/kotlin/com/arrazyfathan/kbbi/` unless qualified. Suggested new types describe proposed implementation, not existing files.

## Source contracts to preserve

| Concern | Source behavior |
| --- | --- |
| Editorial topics | `word_of_day`, `trending_words`, `proverbs`, prefixed by `production_` or `development_`; one campaign switch controls all three |
| Update topic | `production_app_updates` only; non-production unsubscribes |
| Editorial payload | Schema `1`; nonblank `delivery_id` and `campaign_id`; allowlisted topic; nonblank title ≤100 characters and body ≤500; `word/<value>` or `proverb/<value>`; ISO-8601 expiry with timezone, not earlier than receipt time |
| Update payload | `type=app_update`, schema `1`, `flavor=production`, future expiry, `version_name` matching `v?\d+(\.\d+){1,3}`, positive Long `version_code`, nonblank `release_id` ≤100 characters |
| Editorial dedupe | Persist successfully posted delivery IDs; prune records older than seven days; disabling campaigns cancels known editorial notifications |
| Update dedupe | Ignore versions not newer than installed and version codes no greater than the last notified code; unique worker name includes version code |
| Presentation | Editorial channel `editorial_notifications`, default importance, source icon/title/body/expanded text, auto-cancel; update channel `app_update_available`, high importance, localized title/body and update icon |
| Permission | Enabling a category requests permission when needed; denial leaves that category disabled and emits source denial feedback; delivery rechecks settings and permission |
| Routing | Editorial taps open word/proverb content; update taps enter the update flow; receipt alone never navigates |

The source editorial parser does **not** support bookmarks. Preserve that schema-1 restriction. Cover the original bookmark verification requirement through existing bookmark reminder/action regression tests and rejection of unsupported campaign bookmark payloads. A bookmark campaign requires an agreed producer/schema extension, not a silent parser expansion.

## Detailed implementation sequence

### 1. Establish payload, audience, and backend contracts

- [ ] Record the Android source revision and capture sanitized valid/invalid payload fixtures, including sender examples when available.
- [ ] Define messaging build configuration: environment, platform, release channel, installed version/name/code, provider availability, and configured release destination. Target Android has debug/release without source product flavors; do not infer production from build type or reporting eligibility.
- [ ] Verify provider registration for Android application ID `com.arrazyfathan.kbbi.kmp` and the actual iOS bundle ID. Inject each target's own configuration, reuse Firebase initialization, and never copy source credentials.
- [ ] Partition audiences by environment and platform. Preserve Android source topic names; define iOS/web mappings before subscribing so Android version codes/APK announcements cannot become actionable updates elsewhere.
- [ ] Identify the sender/feed/subscription backend and owner. Confirm actual endpoints before adding requests. An absent backend or credentials remains an explicit external prerequisite; fixtures do not prove production readiness.

**Exit:** fixture corpus, configuration requirements, audience mapping, and backend ownership are documented. Missing provider configuration yields an unavailable capability and working startup.

### 2. Build the shared campaign pipeline

- [ ] Add `feature/campaigns` domain/data boundaries following existing result, repository, network, and DI conventions. Suggested contracts: `CampaignPayloadParser`, `CampaignRepository`, `CampaignDeliveryStore`, `CampaignSubscriptionGateway`, `CampaignNotificationPresenter`, and `CampaignCoordinator`.
- [ ] Parse transport maps into typed editorial/update messages before delivery or routing. Inject a clock; preserve source version normalization, timezone/fractional timestamp handling, and expiry boundaries (editorial equality accepted; update equality rejected).
- [ ] Reject invalid schema/topic/destination, missing/oversized required fields, invalid dates/numbers, expired messages, and environment/platform mismatches. Bound incoming data and route length using existing external-entry limits; document stricter boundaries as intentional hardening adaptations.
- [ ] Map word/proverb values through existing destination validation/encoding. Never concatenate unchecked URLs. Unknown optional fields may be ignored; missing required fields must not become valid defaults.
- [ ] Separate parsing, eligibility, presentation, and action consumption. Disabled/invalid receipt never navigates or marks a message displayed. Recheck eligibility immediately before presentation and delayed work.
- [ ] Persist delivery dedupe and feed read/dismiss state with existing native storage patterns and browser IndexedDB. Bound history; if Room changes are needed, provide a preserving migration.
- [ ] Serialize competing deliveries and coordinate push/feed identities. Distinguish received, displayed, read/dismissed, and action-consumed state. Mark presentation only after success; failed posting can retry. Preserve seven-day editorial pruning and update high-water marks per platform/channel.
- [ ] Register shared/platform bindings in existing Koin assembly. Start the coordinator under the app lifecycle owner; cancel observers cleanly and keep provider failures off the startup critical path.

**Exit:** deterministic tests establish validation, eligibility, persistence, concurrent dedupe, and routing without Firebase/devices.

### 3. Connect Settings, permissions, and reconciliation

- [ ] Extend existing Settings actions/events with source campaign/update permission requests, results, and denial feedback. Reuse Plan 20 notification permission handling and avoid duplicate prompts for shared OS permission.
- [ ] Preserve stored defaults and preference keys. Represent unknown/not-requested, granted, denied, and unavailable runtime capability separately from user intent. On fallback-only targets, category controls govern the feed without requiring OS permission.
- [ ] Compute desired subscriptions from category choice, OS/app/channel permission, environment, platform, and provider configuration. Campaigns, updates, reminders, and reporting preferences remain independent.
- [ ] Reconcile at startup, foreground/resume, preference changes, permission/channel changes observed on resume, token rotation, and connectivity recovery. Serialize reconciliation so a stale subscribe completion cannot override a later opt-out.
- [ ] Implement idempotent subscribe/unsubscribe with bounded backoff and pending retry state. Remove old token/environment audiences where supported. Never log raw tokens or subscription keys.
- [ ] Opt-out saves the local gate immediately, cancels pending category work, clears delivered category notifications, hides category feed items, and removes subscriptions. Offline removal retries later while local suppression stays effective.
- [ ] Verify Messaging SDK setup preserves crash/analytics/performance collection gates; notification enablement must not enable reporting.

**Exit:** permission/toggle/retry transitions match source behavior and unavailable-provider startup remains functional.

### 4. Port Android delivery

- [ ] Add Firebase Messaging dependencies in consuming modules/catalog, a non-exported messaging service, and manifest entries. Preserve reminder permission declarations, launch mode, and existing entry filters.
- [ ] Port editorial/update topic workers and push processing with shared parsing/eligibility. Use durable work where processing must survive service lifetime. Preserve unique work, retries/cancellation, and expedited-update fallback behavior.
- [ ] Match localized channels, icons, importance, expanded text, and immutable pending intents. Recheck runtime permission, global notifications, and channel availability immediately before posting.
- [ ] Feed initial-intent and `onNewIntent` notification taps through `AndroidIncomingLaunchRequests` with stable action identity. Preserve reminder/widget/share routing and separate notification identities.
- [ ] Require controlled data payloads for editorial processing so automatic provider display cannot bypass category gates. Document foreground/background/terminated expectations and the supported sender envelope.
- [ ] Port/extract the source version comparator needed for update eligibility; make it reusable by Plan 25 instead of introducing conflicting comparison policies.

**Exit:** Android compiles with configured/unavailable provider paths; worker tests cover permission loss, retries, failed posts, token refresh, and opt-out races.

### 5. Integrate iOS push

- [x] Add Firebase Messaging to the existing Swift package setup, APNs registration, required entitlements/provisioning, and APNs/FCM token bridge. Extend `KBBIAppDelegate`; retain reminder and shortcut handlers. App ID/signing must still be provisioned in the Apple developer account.
- [x] Bridge remote payloads through the shared campaign parser and notification responses into `IosIncomingLaunchRequests` with stable delivery IDs; preserve foreground and cold/warm routing.
- [x] Reconcile on token changes, Settings changes, permission state, and activation. The iOS campaign reconciler does not cancel scheduled local reminders.
- [x] Use visible APNs alerts with server-managed per-install category eligibility. Server sends are idempotent per installation and opt-outs update server eligibility; alerts already submitted to APNs cannot be recalled.
- [ ] Person manually validates opt-out acknowledgement and in-flight behavior on a provisioned iPhone. Keep exact suppression of already queued OS alerts outside the acceptance claim.

**Exit:** framework/Xcode host compile; provisioning and real-device delivery remain explicit human acceptance gates. Reference: [Firebase Apple message handling](https://firebase.google.com/docs/cloud-messaging/ios/receive-messages).

### 6. Implement browser push for JS and Wasm

- [ ] Extend the existing service worker with `push` and `notificationclick`, preserving shell caching, share targets, and icons. Keep one registration/scope and a versioned app-to-worker bridge shared by JS/Wasm.
- [ ] Select FCM web with the existing registration or standard Web Push with server-managed subscriptions, based on sender ownership. Provision public application-server configuration separately from private server credentials. Browser clients must not call privileged topic-administration APIs.
- [ ] Subscribe only from the user's enable action in a secure context. Detect service worker, Notification, and PushManager capability. Denied/unavailable cases retain startup/navigation and the feed. Respect user-visible-push requirements; see [PushManager.subscribe](https://developer.mozilla.org/en-US/docs/Web/API/PushManager/subscribe).
- [ ] Make validation available while the UI is closed: prefer a small worker-compatible shared JS artifact; if worker-side validation is necessary, run identical fixtures against it and Kotlin. Do not require a running Wasm UI to process push.
- [ ] Persist gates, environment, expiry, dedupe IDs, and subscriptions in worker-accessible IndexedDB. Acknowledge opt-out to the worker, remove remote category eligibility, unsubscribe when no categories remain, and close delivered notifications. Keep mutation responses out of runtime caches.
- [ ] Validate click destinations, focus an existing same-origin client or open the app, and forward stable action IDs to the shared acknowledgement queue. Support both shells and deployed base paths; reject arbitrary payload URLs.
- [ ] Handle worker upgrades, multiple tabs, subscription expiry/rotation, and closed-window delivery. Recheck subscriptions on activation rather than depending solely on subscription-change events; recover missed content through the feed.

**Exit:** both production bundles include worker/bridge and pass contract tests; HTTPS/closed-app/browser compatibility are manually verified.

### 7. Add the desktop and unsupported-browser feed

- [ ] The campaign backend exists in the sibling `kbbi-api` checkout and now supports installation registration and direct iOS sends. A public read-only announcement feed is still absent; agree its request dimensions (platform, environment, release channel, locale, bounded page size, cursor) and response envelope before implementing the Desktop/browser fallback.
- [ ] Define ordering, pagination, duplicates, empty results, timestamps, errors, cache validators, cursor expiry, and audience filtering with fixtures. Publish only active public announcements; release entries must be platform-specific. No endpoint name/envelope is established by this proposal.
- [ ] Add remote source/repository using the existing HttpClient/error mapping and bounded last-successful cache. Refresh asynchronously on launch/resume and explicit retry; network failure must not block navigation. Expired cached items never reappear.
- [ ] Provide shared loading, populated, empty, offline-cached, error/retry, read, and dismissed UI states. Use source notification copy/icons, existing theme/typography, localized accessibility semantics, and keyboard/focus support. The source has no feed screen: document this required platform adaptation and compare constituent UI instead of claiming source-screen parity.
- [ ] Follow the existing reminder in-app fallback integration pattern without changing reminder delivery. Opt-out hides category items immediately. OS permission denial alone must not suppress an independently enabled fallback category, but the feed must not re-enable a category rejected by the source-equivalent permission flow.
- [ ] Use stable push/feed identities to prevent repeated presentation; persist read/dismiss state across restart. Feed refresh never auto-navigates. Native targets may use the same feed for missed-message recovery.

**Exit:** desktop and unsupported browsers expose real backend announcements on launch, including offline/retry/opt-out behavior. Fixtures alone do not complete the feed.

### 8. Finish action routing and Plan 25 handoff

- [ ] Route word/proverb taps through existing destinations, preserving source history/cache behavior and useful missing-content errors. Confirm bookmark reminders still reach bookmarks.
- [ ] Represent update actions with validated release identity/platform/channel; queue until navigation is ready and acknowledge once. Receipt dedupe and tap dedupe remain distinct.
- [x] Until Plan 25 is available, provide a localized iOS update-announcement detail route and only open a build-configured HTTPS App Store/TestFlight URL. The server rejects iOS update sends when its HTTPS release URL is missing; URLs are never accepted from push payloads.
- [ ] Plan 25 replaces the interim action with verified release/policy handling. If neither flow nor valid interim destination exists, keep end-to-end update-action acceptance open.
- [ ] Keep reporting within existing typed events/privacy gates. Exclude provider tokens, endpoints/keys, unrestricted URLs, full payloads, and dictionary terms from diagnostics. Messaging works with all reporting disabled.

**Exit:** every delivered item opens useful target-appropriate content once, on cold/warm launch, with functional back navigation.

## Automated verification

Use existing test tooling, injected clocks/fakes, and MockEngine. Tests exercise observable behavior without contacting Firebase.

| Suite | Required cases |
| --- | --- |
| Payload | Word/proverb/update; unsupported campaign bookmark; bookmark reminder regression; missing/blank/long fields; unknown schema/type/topic; invalid/overflow version code; version formats; timezone/fractions; invalid dates; exact expiry boundary; wrong environment/platform |
| Eligibility/reconciliation | Defaults; independent categories; denied/revoked/global/channel permission; provider unavailable; development update unsubscribe; token change; transient failures; cancellation; rapid toggles with delayed subscribe completion |
| Persistence/delivery | Seven-day pruning; simultaneous duplicates; push/feed overlap; restart; failed-post retry; update high-water mark; separate displayed/read/dismissed/action state; offline opt-out |
| Settings/navigation | Enable/request/result/denial; permission refresh; cold/warm tap; duplicate callbacks; navigation readiness; encoded Indonesian values; malformed destination; unavailable release; reminder/widget/share regressions |
| Feed | Contract mapping; pagination; empty/error; cancellation; offline cache/expiry; platform filtering; read/dismiss persistence; opt-out during fetch |
| Worker | Kotlin/worker fixture equivalence; IndexedDB gates/dedupe; multi-tab coordination; safe click URLs/base path; worker upgrade; unsubscribe; JS/Wasm bridge |
| DI/privacy | Coordinator/adapters resolve; unconfigured startup succeeds; reporting gates unchanged; sensitive data excluded; push works with reporting disabled |

Run focused suites first, then regression checks and target builds. Confirm tasks against the checkout during implementation:

```sh
./gradlew --no-daemon :shared:jvmTest :widgetCore:jvmTest --console=plain
./gradlew --no-daemon :androidApp:assembleDebug :androidApp:lintDebug :shared:linkDebugFrameworkIosSimulatorArm64 :desktopApp:compileKotlin :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution --console=plain
./gradlew --no-daemon :shared:compileTestKotlinIosSimulatorArm64 :shared:compileTestKotlinJs :shared:compileTestKotlinWasmJs --console=plain
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' -derivedDataPath /tmp/kbbi-plan24-derived CODE_SIGNING_ALLOWED=NO build
git diff --check
```

Add the worker contract-test command once its build integration is selected. Test-source compilation is not execution. SDK delivery and visual parity need manual evidence.

## Manual acceptance and evidence

Prepare sanitized sender fixtures and reproducible steps specifying environment/platform/version/channel, expected subscriptions, appearance, and destination. A person performs all device, Simulator, desktop-app, and browser interaction checks; agents prepare/build and review supplied evidence.

- [ ] Compare Settings switches, permission/denial feedback, localized notification copy/icons/expanded content, motion, accessibility, and destination screens directly with Android source at equivalent sizes. Record required platform adaptations.
- [ ] Android: fresh install; grant/deny/revoke; channel disablement; foreground/background/terminated delivery; cold/warm taps; duplicates; restart; development/production audiences; offline opt-out then reconnect.
- [ ] iOS: real-device provisioning/APNs/token mapping; foreground/background/terminated and force-quit limitations; reminder delegate coexistence; permission changes; token refresh; opt-out acknowledgement/in-flight behavior. Simulator build alone is insufficient push evidence.
- [ ] JS and Wasm separately: HTTPS; grant/deny; reload; multiple tabs; all windows closed; click routing; worker upgrade; subscription removal; unsupported-browser feed; deployed base path.
- [ ] Desktop: launch/resume fetch; offline cache; empty/error/retry; keyboard/focus/accessibility; read/dismiss/restart; opt-out; word/proverb/update actions.
- [ ] All targets: malformed/expired/wrong-target messages do nothing; duplicate notifications/actions do not occur; startup, back navigation, bookmark reminders, widgets, and share links remain functional.
- [ ] All targets: opt-out removes subscriptions and suppresses local presentation without changing reporting preferences; messaging works with analytics/performance disabled.
- [ ] Backend owner validates audience mapping, publication, removal, credentials, and platform-safe update metadata in a non-production audience before rollout.

## Delivery order and completion gates

Implement reviewable increments: contracts/configuration → shared pipeline → Settings/reconciliation → Android → iOS → browser push → feed/backend integration → action handoff/regression checks. Feed contract work can begin after contract discovery, but production feed availability remains a release prerequisite.

- [ ] All five targets compile; focused and regression checks pass or failures are recorded with actionable scope.
- [ ] Provider configuration, platform audiences, and live feed are available. External prerequisites remain open until verified, regardless of mock test success.
- [ ] Every delivered item opens valid content; Plan 25 integration/interim behavior is explicit.
- [ ] Human acceptance is recorded separately from builds and automated tests.
- [ ] Update F20 and dated evidence in [parity-inventory.md](parity-inventory.md): source revision, commands/results, target configuration, person-run scenarios, adaptations, and remaining limitations. Mark a target verified only after its evidence exists.

Recheck provider documentation against pinned SDKs during implementation. Subscription reference: [Firebase topic subscriptions](https://firebase.google.com/docs/cloud-messaging/manage-topic-subscriptions).
