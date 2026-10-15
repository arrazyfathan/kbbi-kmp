# 22 — Add widgets and glanceable surfaces

**Depends on:** 07, 14, 20, 21  
**Outcome:** Word of day, saved word, and quick search are accessible outside or at the front of the app.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

**Status:** Implementation and package wiring are in place. The Android APK and lint, iOS Simulator and device `WidgetCore` frameworks, Desktop JVM, browser JS/Wasm distributions, and iOS Simulator host with embedded Share and WidgetKit extensions build successfully. Android manifest/provider wiring and the packaged iOS extension metadata, executable, WidgetCore symbols, catalog, and localizations were inspected. The full shared and WidgetCore JVM test suites pass (90 tests), and JS/Wasm test sources compile. On 2026-10-15, the user confirmed that all Plan 22 source-parity, device, Simulator, Desktop, and browser acceptance scenarios passed.

## 1. Source baseline and existing integration points

Read these files together before implementation:

- Source `app/src/main/java/com/arrazyfathan/kbbi/widgets/KbbiWidgets.kt`, `DailyItemSelector.kt`, `WidgetRefresh.kt`, and `WidgetLaunchRequest.kt`.
- Source `app/src/main/res/xml/widget_*_info.xml`, widget loading layout/drawables, day/night palettes, and English/Indonesian widget strings.
- Source `app/src/main/java/com/arrazyfathan/kbbi/navigation/AppNavigation.kt` and Home handling of external searches.
- Target `shared/.../feature/reminders/domain/ReminderModels.kt`: `DailyReminderSelector` already implements the source daily seed.
- Target `BookmarkRepository`, `WordCatalogRepository`, `WordSearchRepository`, `AssetWordCatalogRepository`, and the existing bookmark/detail flows.
- Target `navigation/IncomingNavigation.kt`, `DestinationParser.kt`, Android incoming launch adapter, iOS URL/scene handlers, desktop launch broker, and browser navigation/PWA manifest.

Current gaps: no Android widget providers/resources, no WidgetKit target, no widget synchronization coordinator, and no equivalent desktop/browser content surface. Plan 21 already provides the launch queue and most native entry plumbing. Its Android saved-widget mapping currently always selects `OPEN_BOOKMARKS`; this must be corrected for populated widgets.

The source daily algorithm is `Random(year * 366 + dayOfYear).nextInt(size)`. Word of day preserves the nonblank bundled catalog order. Saved word filters blank values and sorts words before selection. Preserve this algorithm, casing, and ordering; do not replace it with a hash, platform random generator, or differently sorted catalog. Reminders and widgets must select the same daily catalog word.

## 2. User-visible behavior to deliver

| Surface/state | Content | Tap result |
| --- | --- | --- |
| Word of day, catalog available | Selected word and localized daily heading | Submit that exact word through Home's existing search flow; show its result/detail or the existing lookup error |
| Word of day, catalog unavailable | Source unavailable message and search fallback | Open Home/search safely, without submitting an empty word |
| Saved word, bookmarks available | Deterministically selected saved word and localized heading | Search the displayed word, rather than always opening the bookmark list |
| Saved word, no bookmarks | Source empty-state message and bookmark prompt | Open Bookmarks |
| Quick search | Localized search heading/prompt | Open Home with search focused, once |
| Old widget after a saved word was removed | Refresh current content when possible | A valid previously displayed word remains a valid search; never re-save it or substitute a different word silently |
| Offline lookup | Widget content still comes from local data | Existing cached meaning when available; existing retry/error behavior otherwise |

Definitions are not bundled with the word index. Do not promise offline definitions for a never-looked-up daily word, fetch definitions during widget rendering, add search history during refresh, or treat a removed bookmark as an invalid dictionary word.

On Android reproduce the source compact `110 × 56 dp` and expanded `220 × 56 dp` responsive layouts: 18 dp outer radius, 8 dp padding, 36 dp icon tile with 12 dp radius and 20 dp icon, 10 dp text gap, 17 sp bold primary line, and expanded-only 11 sp bold supporting line plus 36 dp arrow tile in a 52 dp trailing area. Port the exact source light/dark palettes, loading layout, icon assets, one-line truncation, labels, descriptions, and horizontal resizing metadata. Widget colors follow the source day/night treatment rather than arbitrarily adopting a selected app accent.

iOS uses native WidgetKit families and system rendering rules while retaining the same three identities, selected words, headings, empty states, actions, visual hierarchy, and accessible labels. Desktop/browser equivalents must expose the actual selected words and empty states; three generic shortcut buttons alone are insufficient.

## 3. Architecture and ownership

### 3.1 Small extension-safe Kotlin core

- [x] Add `:widgetCore` to `settings.gradle.kts`. Match the repository's required Android, iOS device/Simulator, JVM, JS, and Wasm variants, using existing catalog-managed plugins and versions.
- [x] Keep this module limited to deterministic selection, widget data models, JSON serialization/validation, and a small Swift-callable snapshot factory. It must not depend on Compose, Room/SQLite, Ktor, Koin, application startup, or platform UI APIs.
- [x] Expose an independent static `WidgetCore` iOS framework for the widget extension. The containing app continues using `Shared`; it must not link a second copy of the standalone framework. This module exists to let WidgetKit run the same selection logic without importing the entire application framework.
- [x] Move the canonical daily algorithm into this core. Keep `DailyReminderSelector` as a delegating compatibility wrapper so existing reminder callers retain identical output. Add explicit sorted-saved-word selection alongside catalog-order selection.
- [x] Define plain `WidgetKind`, daily date inputs, `WidgetSnapshot`, and versioned `WidgetSnapshotEnvelope`/`WidgetBookmarkSnapshot` records. Snapshots contain selected words, availability, local date, language, generation/revision metadata, and action kind; they contain no database entities, definitions, search history, or secrets.
- [x] Keep dates explicit in pure APIs: year and day-of-year must come from the same Gregorian local date. Native adapters own clocks, time zones, and next-local-midnight calculation. Never add a fixed 24 hours to calculate the next calendar day.
- [x] Specify missing/corrupt/unsupported-schema handling and bounded decode sizes. Reject malformed snapshot input with an honest unavailable/empty state instead of crashing an extension. Preserve the full valid bookmark candidate set; do not silently truncate it and change selection.

### 3.2 Shared application integration

- [x] Add `shared/.../feature/widgets/domain/` contracts and `data/WidgetContentProvider.kt`. Reuse `WordCatalogRepository`, `BookmarkRepository`, and settings; do not add a second database or remote word repository.
- [x] Cache the parsed catalog in the provider and invalidate it only when its resource/version changes. Keep expensive parsing and serialization off the UI thread.
- [x] Add a platform publisher contract and lifecycle-owned `WidgetSyncCoordinator`. Observe bookmark words and widget-relevant language changes; compare normalized inputs before publishing so unrelated UI recompositions or preference changes do not reload widgets.
- [x] Start one coordinator per application process after DI is ready. Android owns its application scope; iOS/desktop/web use explicit host/session ownership with cancellation. A headless Android widget render must also work when no Activity has been opened.
- [x] Serialize concurrent snapshot writes and revisions so an older bookmark read cannot overwrite a newer delete/save. A failed native refresh must not fail the bookmark operation; preserve cancellation and allow later reconciliation to recover.
- [x] Register provider/publisher/coordinator through the existing DI assembly and target bindings. No platform-specific type should leak into the common widget contract.

## 4. Shared navigation and analytics

- [x] Extend incoming requests with a validated widget kind and optional selected word, or an equivalent typed widget payload. Keep source-specific widget semantics separate from generic arbitrary shared text: a catalog entry containing whitespace must not be reduced to its first token.
- [x] Dispatch populated daily/saved widgets into the existing Home search action using the complete validated word. Empty saved widgets open Bookmarks; unavailable daily widgets and quick search open Home/search appropriately.
- [x] Fix the Android adapter's populated saved-widget mapping. Preserve bounded input validation, stable identities during redelivery, and acknowledgement only after the request is handed to navigation/action handling.
- [x] Define explicit widget URLs under `kbbi://widget/...` and matching browser hash paths for daily, saved, and search actions. Encode selected words with the existing URL utilities. Validate allowed widget kinds/arguments; do not globally relax the parser's rejection of unknown or malformed routes.
- [x] Use the word encoded in a populated widget tap rather than recalculating a new daily selection on navigation. A tap across midnight must open the word the person actually saw.
- [x] Preserve each accepted action through splash, cold startup, background/foreground activation, and Home mounting. Repeated delivery of the same request must not duplicate search/history/navigation, while a later deliberate tap may run again.
- [x] Log existing `AnalyticsEvent.WidgetOpened(WidgetKind)` and corresponding widget source attribution through the reporting controller, once per accepted interaction and subject to analytics consent. Never include raw words in analytics parameters. Do not log widget opens for rendering or timeline refreshes.

## 5. Android implementation

- [x] Add the source-compatible `androidx.glance:glance-appwidget` dependency through `gradle/libs.versions.toml` and `androidApp/build.gradle.kts`; use the source's current `1.2.0` baseline after checking compatibility with the target's Compose dependencies. Reuse the existing WorkManager dependency.
- [x] Port `WordOfDayWidget`, `SavedWordWidget`, `QuickSearchWidget`, shared Glance content, three receivers, and provider metadata into `androidApp/.../widgets/` and Android resources. Glance uses its own UI primitives; ordinary shared Compose screen components cannot be rendered directly inside it.
- [x] Register all provider receivers with explicit metadata, labels, and required launcher visibility. Port both resource locales and day/night palettes, loading background, preview layout, arrow, and source word/saved/search icons with target resource references.
- [x] Render through the shared content provider. Read current local bookmark/catalog state in the headless widget path rather than relying solely on an in-memory foreground observer.
- [x] Use explicit Activity launch actions with appropriate `SINGLE_TOP`/`CLEAR_TOP` behavior, correct target application ID, distinguishable widget payloads, and no mutable shared intent that can point multiple widgets at the last rendered word.
- [x] Add widget refresh worker/scheduler and bookmark coordination. Refresh after installation, provider update/resize, bookmark membership changes, relevant locale/configuration changes, and reconciliation after process start.
- [x] Schedule the next local-day refresh only while at least one data widget is installed. Reconcile time/date/time-zone changes and restored widgets; cancel data refresh work when the final data widget is removed. Quick-search-only installations do not need daily data work.
- [x] Recompute the next calendar boundary after each run to handle DST. Use unique WorkManager work without accidentally replacing/cancelling the currently executing worker. Retry unexpected rendering failures, log safely, and rethrow cancellation. Do not introduce exact-alarm or notification permission requests for widgets.
- [x] Refresh all instances consistently and avoid work churn from unchanged bookmark emissions. Avoid issuing a new widget redraw in response to every read/recomposition.

WorkManager is best-effort: content is selected for the current local day whenever rendering occurs, with refresh requested for rollover. Acceptance must account for OS-delayed execution rather than claim an exact midnight wakeup.

## 6. iOS WidgetKit implementation

### 6.1 Target and shared data

- [x] Add a separate `iosApp/WidgetExtension/` folder and `KBBIWidgetExtension` target with a SwiftUI `WidgetBundle`, three widget definitions, timeline provider, localized resources, assets, Info.plist, and entitlements. Include only extension files in that target; do not accidentally add its `@main` declaration to the host or Share Extension.
- [x] Use a unique bundle ID such as `com.arrazyfathan.kbbi.WidgetExtension`, matching host deployment/version settings, `APPLICATION_EXTENSION_API_ONLY`, and `com.apple.widgetkit-extension`. Set `CFBundleExecutable = $(EXECUTABLE_NAME)` and verify its processed value matches the packaged binary.
- [x] Embed/sign the extension under the host's PlugIns build phase and add the target dependency. Add architecture/configuration-aware Gradle framework build/link integration for `WidgetCore`, covering Simulator ARM64 and device ARM64 without importing `Shared`.
- [x] Reuse `group.com.arrazyfathan.kbbi` in the host and Widget Extension. Keep the existing Share Extension's handoff keys/files intact. Record the new bundle ID's Apple Developer App Group/signing setup requirement.
- [x] Have the containing app publish a versioned current snapshot plus bookmark-word candidates and language into an atomic App Group file. Write to a temporary sibling and replace the completed file; the extension must never read half-written JSON. Store only the fields needed for display/selection.
- [x] Add a build-time copy of the exact existing `entries.json` word index to the widget's resources, preserving order and avoiding a separately maintained catalog. The extension uses this bundled index and App Group bookmark data with `WidgetCore` to compute future days even if the main app remains closed.
- [x] Bridge snapshot updates from Kotlin to a native publisher that writes data and requests reloads for the affected widget kinds through `WidgetCenter`. Reconcile on first launch, app activation, language changes, and successful bookmark edits; coalesce unchanged updates.

### 6.2 Timeline and interactions

- [x] Implement placeholder, gallery snapshot, and live timeline paths for every widget kind. Gallery previews may use representative sample content; installed empty/unavailable states must reflect actual available local data.
- [x] Support `systemSmall` and `systemMedium` families, preserving source visual hierarchy and an appropriate compact/expanded layout. Do not add Lock Screen, Live Activity, watchOS, or a separate native macOS target to this plan.
- [x] Use the shared factory to generate today's entry and a short sequence of future local-day entries, then request a new timeline at the end. Regenerate from the local catalog/bookmark snapshot on every timeline request, so the feature does not expire after a host-generated fixed-date schedule runs out.
- [x] Resolve local calendar dates/time zone on each provider run; handle date/time-zone changes on the next reload and host activation. Honor WidgetKit's refresh budget instead of repeatedly requesting immediate reloads or promising exact delivery times.
- [x] Use `widgetURL`/native links to carry widget kind and displayed word into the host's existing URL/scene handling and shared incoming queue. Exercise URL normalization and percent encoding explicitly.
- [x] Handle first install before the host has opened, unavailable App Group data, empty saved words, corrupted/version-mismatched files, and extension restart. Daily catalog content can still work from the bundled index; Saved Word shows a safe empty/unavailable state until the host publishes bookmark data.
- [x] Localize gallery descriptions, content, and accessibility labels for English/Indonesian. Verify system color scheme, native content margins, tinted rendering, large text, and VoiceOver reading order. Reuse source icon artwork where possible rather than inventing unrelated identities.

WidgetKit owns rendering/reload timing. Bookmark updates request reloads after the new snapshot is committed; successful app writes do not prove the OS has already redrawn every widget.

## 7. Desktop, browser JS, and browser Wasm

- [x] Add one reusable shared `GlanceableEntrySection` presenting the daily and saved words in a discoverable Home entry area on desktop and browser targets only; the existing Home search field provides quick search without a duplicate card. Preserve the existing mobile Home screen and native quick-search widgets.
- [x] Reuse the same snapshot provider, labels, palette identities, empty states, and action resolver. Preserve source density/typography hierarchy while adapting to available width; support mouse, keyboard activation, focus indication, and accessible names.
- [x] Refresh on local-day change while active and immediately on application/window resume or browser visibility restoration. Sleeping computers and throttled background tabs must reconcile when resumed; timers must be cancellable and must not rely on an always-running background browser.
- [x] Observe bookmark edits and language changes. Keep initial local loading/error states explicit; do not flash a different random word before the deterministic selection arrives.
- [x] Add date-independent daily-word, saved-word, and search entry paths to the existing PWA manifest alongside existing useful shortcuts. Resolve daily/saved selections from browser-local data on launch. Keep those launch-time shortcuts distinct from word-specific content-card URLs, which must preserve the displayed word.
- [x] Handle the new semantic hash routes before the generic destination parser and acknowledge them once. Update the service-worker shell cache version if needed so installed PWAs receive new manifest/routes without retaining incompatible bundles.
- [x] Keep existing desktop CLI/URL forwarding working. Use in-process typed actions from the shared cards; no native macOS desktop-widget target or Windows/Linux widget integration is required here.
- [x] Ensure all changes compile for both JS and Wasm. Unsupported PWA shortcut menus still have the same three actions available inside the app. Browser offline content uses available local catalog/bookmark/cache data and existing service-worker behavior.

## 8. Ordered delivery checkpoints

| Checkpoint | Deliverable | Exit condition |
| --- | --- | --- |
| A | Source behavior/resource inventory and canonical core | Exact selector ordering/seed, behavior table, snapshot schema, and extension-safe framework are established; reminder output is unchanged |
| B | Shared provider, coordinator, typed widget actions | Current local content, complete-word handling, empty fallbacks, one-time dispatch, and consent-aware event attribution are wired |
| C | Android providers and refresh lifecycle | Three widgets/resources are packaged, headless rendering can resolve DI, scheduler ownership is correct, and Android builds |
| D | WidgetKit target and App Group bridge | Framework, catalog, entitlements, processed plists, host embedding, timeline factory, links, and iOS host builds are verified |
| E | Desktop/browser surfaces and PWA routes | Three live entry cards, resume/day/bookmark refresh, keyboard support, and existing external launch compatibility are present |
| F | Cross-platform build/package evidence and human acceptance | Build evidence is recorded separately from completed source/device checks; F21 is updated only to the level actually verified |

Do not mark Plan 22 complete after only an Android port or a successful compilation. Resolve implementation gaps across all targets, then leave only explicitly listed human checks pending until evidence is supplied.

## 9. Verification plan

### 9.1 Build and package checks

Run during implementation, not during preparation of this plan:

```sh
./gradlew --no-daemon :androidApp:assembleDebug :androidApp:lintDebug :shared:compileKotlinIosSimulatorArm64 :widgetCore:linkDebugFrameworkIosSimulatorArm64 :desktopApp:compileKotlin :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution --console=plain
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
git diff --check
```

- Inspect Android's merged manifest and APK for all three receiver/provider resources, locales, loading assets, and correct explicit Activity targets.
- Inspect the built iOS host's `PlugIns/KBBIWidgetExtension.appex`: executable key and matching binary, extension-point identifier, WidgetCore linkage, bundled catalog/localizations, bundle/version consistency, and embedded target membership. Verify device framework compilation separately; a signing-disabled Simulator build does not establish provisioning or install acceptance.
- Confirm dependency/license inventory updates for newly introduced Glance/core dependencies using the repository's existing catalogue process. Keep unrelated pre-existing working-tree changes intact.
- Record meaningful focused logic scenarios: source-selector equivalence for fixed dates, empty/one/many bookmarks, input ordering, year/leap-day boundaries, complete-word encoding, corrupt snapshots, duplicate request IDs, and retry/cancellation behavior. If automated tests are explicitly requested, implement these against the pure core/provider/action resolver; do not replace manual launcher/WidgetKit acceptance with unit-test results.

### 9.2 Manual source and runtime acceptance

All device, Simulator, and app/browser interaction below is performed by a person. Capture platform/version, locale, size/family, source comparison result, and observed navigation/refresh behavior in `plan/parity-inventory.md`.

| Scenario | Android | iOS | Desktop / browser JS / browser Wasm |
| --- | --- | --- | --- |
| Discovery and first use | Three launcher gallery entries, correct previews/names; add before opening an Activity | Three WidgetKit gallery entries; add before host has published data | Daily and saved cards beside the existing search field; PWA shortcuts where supported |
| Visual/source comparison | Compact/expanded, exact light/dark palettes, text/icon geometry, EN/ID, truncation, font scale | Small/medium native equivalents, EN/ID, light/dark/tint, large text, VoiceOver | Equivalent card states at narrow/wide widths, themes/locales, keyboard/screen reader |
| Daily selection | Same word as source/reminder for the same date and ordered catalog | Same selection with app closed; future timeline rollover without reopening host | Same local-date selection; resume after midnight/sleep/tab suspension |
| Saved state | Zero, one, multiple bookmarks; add/remove selected/final bookmark; all widget instances refresh | Same states, atomic snapshot and requested reload; extension restart | Same states while cards are visible, across restart/reload |
| Populated tap | Daily and saved words submit displayed word, including encoded spaces/punctuation | Matching URL opens displayed word through native scene lifecycle | Matching card/URL action uses displayed word; existing CLI broker remains valid |
| Empty/invalid/stale tap | Empty saved → Bookmarks; absent catalog → search; removed saved word can still be searched | Same outcomes, missing/corrupt/version-mismatched snapshot recovery | Same outcomes plus unavailable PWA menus and offline local storage |
| Lifecycle/duplicate delivery | App stopped, backgrounded, already on Home/detail; rapid repeated deliveries and deliberate second tap | Cold/warm/background launches, splash mounting, URL redelivery | Running-instance forwarding, existing window/tab, reload/history restoration |
| Time and scheduling | Next local day, DST/time-zone change, reboot/restoration, removal of final data widget | Future local-day entries, time-zone reconciliation, host closed, WidgetKit delayed reload | Local-day refresh plus resume after clock/time-zone change |
| Offline/cache | Cached word works; unseen word shows existing failure/retry; rendering performs no remote lookup | Local timeline still renders; host lookup follows same cache/error semantics | Available local content/cache works; same missing-definition behavior |

### 9.3 Completion checklist

- [x] Compare all delivered states/interactions directly with the Android source at equivalent sizes; record any required native adaptation and resolve unintended differences.
- [x] Three Android widgets are implemented and manual launcher behavior is accepted.
- [x] Three iOS widgets are implemented; packaged extension metadata/linkage and human installation/timeline/navigation checks pass.
- [x] Desktop, browser JS, and browser Wasm show daily and saved live cards alongside the existing search field and pass human interaction checks.
- [x] Daily selection matches the source and reminders; saved selection is stable for unchanged date/bookmarks across every target.
- [x] Bookmark/day/locale/resume updates reconcile correctly within native OS scheduling constraints; no claim of exact background wakeup timing is made.
- [x] Complete-word taps, empty states, offline lookups, stale taps, and repeated requests follow the behavior table without duplicate side effects.
- [x] Builds/package checks pass and evidence is added to `plan/parity-inventory.md`; F21 implementation/runtime columns reflect current evidence.

## Platform references

- [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance): separate widget rendering APIs.
- [Manage and update GlanceAppWidget](https://developer.android.com/develop/ui/compose/glance/glance-app-widget): persisted content/state and explicit update requests.
- [Creating a WidgetKit extension](https://developer.apple.com/documentation/widgetkit/creating-a-widget-extension): native extension setup, views, and widget links.
- [Keeping a widget up to date](https://developer.apple.com/documentation/widgetkit/keeping-a-widget-up-to-date): timelines, reload requests, and system refresh budgets.
