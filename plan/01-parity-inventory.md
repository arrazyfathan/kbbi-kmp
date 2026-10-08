# 01 — Build the live parity inventory

**Depends on:** none  
**Outcome:** Every Android source feature has a tracked KMP implementation and platform acceptance row.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Inventory production modules, screens, actions, ViewModels, contracts, DTOs, storage, assets, manifests, background tasks, tests, and release scripts in the Android repository.
2. For each behavior, record the source entry point, KMP counterpart, data dependencies, and status: present, partial, or missing.
3. Add columns for Android, iOS, JVM desktop, browser JS, and browser Wasm. Include cold starts, offline behavior, denied permissions, and failure states.
4. Assign every gap to one task in this roadmap; record evidence for every item marked complete.
5. Update the historical `MIGRATION_STATE.md` to point at the inventory and remove its outdated “functionally complete” claim.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- No source-app capability lacks a checklist row or task owner.
- Reconcile the checklist after each later task with code and run evidence.

**Status:** Static inventory and gap ownership completed 2026-10-07; see [live parity inventory](parity-inventory.md). Platform runtime/build checks are recorded as unverified and belong to task 02.
