# 25 — Add app-update flow

**Status:** In progress (2026-10-22). Shared release checking, startup/Settings prompts, version metadata, target-aware destinations, and Android DownloadManager delivery with package/signature verification are implemented. Human source/device acceptance remains open.

**Depends on:** 13, 14, 19, 21  
**Outcome:** Update checks, prompts, and destination actions reproduce Android policy with platform-safe installation.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port GitHub release DTOs, version comparison, asset selection, check cadence, cached required updates, optional/required prompts, and release notes.
2. Keep Android APK identity validation, download manager, notification, and install launcher in Android code.
3. iOS: route to the configured App Store/TestFlight release destination; desktop: route to the correct DMG/MSI/DEB package; web: prompt reload after a deployed version changes.
4. Make release/channel metadata target-specific so Android APK URLs are never offered to iOS, desktop, or web.
5. Preserve manual check behavior and useful errors when GitHub/policy data is unavailable.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Tests cover older/equal/newer version, malformed release, missing asset, policy failure, cached required update, and cancelled download.
- Prompts cannot launch a package for another OS; all target actions work from Settings and startup.

## Implementation progress — 2026-10-21

- Shared GitHub latest-release and update-policy parsing, semantic version comparison, per-platform asset selection, persisted daily/hourly check cadence, and cached required-update fallback are implemented in `shared/src/commonMain/kotlin/com/arrazyfathan/kbbi/feature/appupdate/`.
- Startup and Settings checks show localized optional/required prompts and release notes. Android uses APK assets (or the GitHub release page if an APK asset is missing); iOS uses the configured App Store/TestFlight URL only when the policy explicitly includes `ios`; Desktop selects the package extension for its host OS; Web reloads only for an explicitly targeted web release.
- Target-format production release publishing is not configured in this checkout.
- Android now downloads HTTPS APK assets through DownloadManager, checks the expected package ID/version, requires a greater version code and the installed signing certificate, and presents a notification that opens the system installer after a user tap. The release page remains the fallback when GitHub has no APK asset. Android debug build passes; device installation, permission-denial, cancellation, and update-result scenarios remain manual.
- Human 1:1 visual comparison and device/browser acceptance remain open; agents do not perform these interactions.
