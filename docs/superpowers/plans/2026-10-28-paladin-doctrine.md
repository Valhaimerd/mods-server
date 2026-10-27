# Paladin Doctrine Implementation Plan

> Use superpowers:executing-plans inline, with independent scoped work and one final fresh review.

**Goal:** Restrict Paladin healing staves/wands and rebalance path/plate Healing Power.
**Architecture:** Same-filename pack overlays use native skill/equipment APIs. Existing armor-balance helpers gain per-set values; actual skill definitions/runtime defaults change together. Server-side hand enforcement has no item loss or stat/cast bypass.
**Tech Stack:** Java25, NeoForge26.2.0.90, installed Paladins/Armory/SkillTree/SpellEngine/Puffish.
**Design:** docs/patches/paladin-doctrine/design.md; user selected prevent-wielding in either hand.

- [x] Preserve current input JARs; inspect exact skill/equipment/cast APIs and inventory edge cases.
- [x] Add native failing probes; update per-set armor helpers and test fresh/legacy configs, preserving all other attributes.
- [x] Update Paladin root/ordinary nodes and runtime defaults; verify actual unlock rewards and displayed values.
- [x] Implement authoritative rejection in both hands with a short warning, safe full inventory and reset/reconnect handling; verify stat and spell paths.
- [x] Build final replacements, run isolated integration/client warning checks and independent review; install matching active/fixed artifacts with ignored backups.
- [x] Update class references/patch records/player handbook; build/render/check PDF and internal links; report exact validation limits. No commit or push.

## Completion and rulings

Completed28October2026. Explicit user choice: prevent wielding in both hands. Full-inventory ruling: use a safe inventory swap; only an inventory consisting entirely of prohibited weapons needs persisted overflow storage. Never drop/delete on rejection. Normal death follows ordinary drops; keepInventory and native respawn preserve storage. Root identity is actual class allocation; Priest and weapon-training-only allocations are not restricted.

Core native84/84, actual client49/49, fresh/legacy armor29/29 each. Independent review found no Critical/Important defects; its lifecycle-coverage comment was resolved with actual native player save/load and respawn tests. The rapid role/slot-switch regression was first observed failing (15.25 vs10.25), fixed and retested. Exactly2,106 other archive entries remain unchanged; installed/fixed hashes match client and native test manifests.

Handbook79pages built, all pages rendered/reviewed, updated RPG/Armory pages and final contents inspected at reading size;51 contents destinations match. Both PDF copies match,191 internal Markdown targets resolve to generated HTML, icon checks and git diff --check pass. Prior uncommitted Delight work retained. No commit/push or personal installation changes.
