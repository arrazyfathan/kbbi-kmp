# 26 — Restore delivery and verify parity

**Depends on:** 01–25  
**Outcome:** The five runtime targets are buildable, tested, and ready for their respective release processes.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source development/production metadata, versioning, Android signing/distribution, shrink rules, and release notes without copying secrets.
2. Add iOS archive/export configuration, desktop DMG/MSI/DEB packaging, and web HTTPS hosting for assets, service worker, and stateless AI proxy.
3. Configure CI gates for common tests, Android debug build/tests, iOS simulator framework and Xcode build, JVM package/launch smoke, JS production bundle, and Wasm production bundle.
4. Port meaningful source unit tests into `commonTest` or platform test suites; add DI resolution, navigation, persistence, API contract, and OS-adapter tests.
5. Run a manual feature matrix on each target: startup, search, word/proverb/figure detail, bookmarks, settings, AI, offline/error, deep link, notification, and update.
6. Reconcile every task against the task 01 checklist; record support limits and external configuration requirements.
7. Review release artifacts and permissions; verify no signing keys, API keys, service account files, or personal local paths entered tracked files.

## Verification

- All build and focused test gates pass, with device/browser smoke results attached to the checklist.
- Every Android source capability is marked implemented with evidence, mapped to a native equivalent, or documented with an in-app path where the OS lacks the capability.
- Android, iOS, desktop, JS, and Wasm launch from their packaged artifacts.
