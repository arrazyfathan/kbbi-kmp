# 03 — Port assets and both languages

**Depends on:** 01, 02  
**Outcome:** All Android user-facing resources needed by migrated features exist in KMP.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Resource audit

The migrated shared screens are home/search, word list, word detail, bookmarks, splash, and proverbs. All 57 shared string IDs now have English and Indonesian values in Compose Multiplatform resources. Existing text formatting placeholders match in both locales. The existing visitor count uses a localized `%1$d` string; notification plurals remain with the Android reminder feature until task 20 is ported.

| Resource group | KMP location | Audit result |
|---|---|---|
| English strings | `shared/src/commonMain/composeResources/values/strings.xml` | 57 IDs; visible labels and accessibility text use resource IDs. Corrected the search button and logo descriptions and the English home-menu subtitle. |
| Indonesian strings | `shared/src/commonMain/composeResources/values-id/strings.xml` | Matching 57 IDs, including localized visitor count, errors, dialog actions, and accessibility descriptions. |
| Fonts | `shared/src/commonMain/composeResources/font/` | All 12 fonts used by migrated shared UI are present. |
| Drawables | `shared/src/commonMain/composeResources/drawable/` | Shared icons, logos, and home/bookmark illustrations referenced by migrated screens are present. |
| Animations and word catalog | `shared/src/commonMain/composeResources/files/` | Home, loading, empty-state, and reading animations plus the word catalog are present. |

Android launcher icon variants, widget layouts/strings, shortcuts, and app-update/notification assets stay in their Android bundles. Their owning features are not part of the migrated shared UI yet. Android privacy-policy and terms text is also not used by a migrated screen; port it with the settings/legal UI rather than shipping unused copies here. The figure/settings/AI strings and assets are similarly owned by their later roadmap tasks.

## Implementation

- Added the Indonesian `values-id` resource set, retaining the Android translations for shared keys and providing translations for the four KMP-only/mapped keys.
- Replaced hardcoded English descriptions on the bookmark delete overlay with localized Cancel and Delete resources.
- Corrected placeholder resource values that were being exposed to users or accessibility services (`logo_kbbi` and `button_search`).
- Kept API data and sample proverb content as content, not UI labels subject to locale translation.

## Verification

- [x] English and Indonesian files contain the same 57 resource IDs.
- [x] All string formatting placeholders match across locales.
- [x] Referenced shared fonts, drawables, and animation/catalog files exist.
- [x] Rebuild Android, iOS simulator framework, desktop JVM, Browser JS, and Browser Wasm after adding the locale resources; Gradle reported `BUILD SUCCESSFUL` on 2026-10-07.
- [ ] Review long Indonesian labels and large-font layout on device; a locale-specific visual sweep remains open.

Per the repository-wide device-testing policy, that visual sweep must be performed and recorded manually by a person.
