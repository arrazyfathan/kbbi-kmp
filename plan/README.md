# Android-to-KMP parity roadmap

Source of truth: `/Users/macintosh/Personal/Android/Samples/kbbi`. Target: this repository's Android, iOS, desktop JVM, browser JS, and browser Wasm apps.

Tasks are numbered in implementation order, approximately easy to hard. Complete dependencies first. Every task must leave all five targets compiling and update the live parity checklist from task 01.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity is the completion bar for every plan:** `/Users/macintosh/Personal/Android/Samples/kbbi` is the source of truth. Each planned feature must reproduce the complete relevant source UI and behavior: screens, layouts, typography, colors, assets, animations, gestures, accessibility, copy, state transitions, navigation, errors, and edge cases. No simplified, placeholder, or intentionally different screen counts as complete. At equivalent viewport sizes, compare the KMP result directly with the source. Where a target requires a platform-specific API, use its native equivalent and preserve the same user-visible result and behavior. Each plan must leave explicit source-comparison checks in its verification section; code presence or successful compilation alone does not establish parity.

Decisions: cover app behavior and release operations; use native OS equivalents where supported and an in-app path otherwise; fresh Android installation is acceptable; browser custom-AI keys are session-only. Keep reusable logic and UI in `:shared`, with OS adapters in platform source sets or apps. Do not copy Android-only libraries into `commonMain`.

## Ordered tasks

Live status and platform evidence: [parity inventory](parity-inventory.md).

01. [Parity inventory](01-parity-inventory.md)
02. [Build baseline](02-build-baseline.md)
03. [Resources and localization](03-resources-localization.md)
04. [Design system](04-design-system.md)
05. [Platform contracts](05-platform-contracts.md)
06. [Word data](06-word-data.md)
07. [Persistence](07-persistence.md)
08. [Home and words UI](08-home-words-ui.md)
09. [Detail and bookmarks UI](09-detail-bookmarks-ui.md)
10. [Proverb and splash](10-proverb-splash.md)
11. [Figure data](11-figure-data.md)
12. [Figure UI](12-figure-ui.md)
13. [Settings](13-settings.md)
14. [Navigation](14-navigation.md)
15. [Voice search](15-voice-search.md)
16. [AI contracts](16-ai-contracts.md)
17. [AI storage](17-ai-storage.md)
18. [AI UI](18-ai-ui.md)
19. [Observability](19-observability.md)
20. [Reminders](20-reminders.md)
21. [External entry points](21-external-entry.md)
22. [Widgets](22-widgets.md)
23. [App icons](23-icons.md)
24. [Campaigns](24-campaigns.md)
25. [App updates](25-app-update.md)
26. [Delivery and verification](26-delivery-verification.md)

## Completion gates

| Target | Required evidence |
| --- | --- |
| Android | Debug build, focused tests, fresh-install smoke, 1:1 source UI/behavior comparison, intents/widgets/notifications |
| iOS | Simulator framework and Xcode build, launch/navigation, 1:1 source UI/behavior comparison, native integrations |
| Desktop JVM | JVM build and app launch, window/keyboard behavior, 1:1 source UI/behavior comparison, OS adapters |
| Browser JS | Production bundle and HTTPS browser smoke, routing/storage/service worker, 1:1 source UI/behavior comparison |
| Browser Wasm | Production bundle and HTTPS browser smoke, routing/storage/service worker, 1:1 source UI/behavior comparison |

Device builds may be automated, but all device and simulator interaction/acceptance checks are manual human checks. Do not use an agent to operate devices or claim device verification.

Credentials, signing assets, push configuration, and deployment secrets must be injected securely. Never commit or print them.
