# PvP Combat Limits Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task by task. Track completion here.

**Goal:** Implement the approved 40-second combat flag, independent block quotas, advanced-tool restrictions and three-icon HUD.

**Architecture:** Extend the current PvP replacement with a pure deadline/budget state class, server event enforcement, saved state and a small client payload/HUD. Guard the two advanced mods at shared server entry points and execution queues.

**Tech Stack:** Java 25 bytecode, Minecraft 26.2, NeoForge 26.2.0.90, existing ASM packaging tools, disposable client/server fixtures.

**Spec:** `docs/superpowers/specs/2026-10-25-pvp-combat-limits-design.md` (approved by user).

## Global constraints

Preserve same active JAR filenames, existing damage multipliers, earlier pack patches, party protection, personal installations and unrelated changes. No commit/push. Final artifacts must be installed in repository mods and ignored fixed packs only after verification; archive predecessors.

## Review focus

Confirmed damage must exclude canceled/zero/party hits. Reconnect/death/dimension changes must not reset budgets. Pending mass actions must stop without consuming resources. Denied block changes must correct client predictions. Dedicated servers must not load client classes.

## Tasks

- [x] 1. Add `docs/patches/pvp-combat-limits/CombatState.java` and its executable test. Pin 40s/30s deadlines, 20/30 quotas, independent starts, silent refresh, expiry cleanup and save/restore. Run the failing test before implementation, then require PASS.
- [x] 2. Add `CombatRules.java`, networking and server hooks. Use existing `ServerEventsForge.playerSource`, native post-damage event and party guards; persist only IDs/deadlines/counters. Cover direct/projectile/summon hits, denied/zero hits, real block events and lifecycle continuity in an isolated server fixture. Preserve damage-retention regression.
- [x] 3. Patch Sophisticated Building permission/queue and OneKeyMiner shared chain operations with optional guarded calls into `CombatRules`. Test actual entry points and queued work, rejected advanced actions, resources and post-expiry recovery. Package exact baselines with asserted ASM edit counts.
- [x] 4. Add client-only HUD and payload handling; package supplied icon assets unchanged except ordinary texture resizing if required. Match inspected Spell Engine slot dimensions. Test payload decoding and inspect real screenshots at two GUI scales, exact screen-edge bounds, timer order, inactive state and hide-GUI behavior.
- [x] 5. Verify final archives, rerun meaningful server/client checks, install matching final/fixed copies and preserve predecessors. Update README, MODLIST, patch notes and handbook PDF; render/review changed pages and audit TOC. Record exact artifact hashes and actual verification limits.

Implementation proceeds inline. User design approval already covers these tasks; no additional approval gate is needed.


## Execution record

Implemented inline against the current dirty workspace using scoped patch/build/test directories; unrelated changes were preserved. The initial pure test failed on absent CombatState, then passed 45 checks. Native fixtures now pass 118 preceding damage checks and 85 new combat checks. The review requested one correction: charge from successful native removeBlock rather than destroyBlock's handled result. A refusing registered test block demonstrated RED before the fix and GREEN after it. Final connected-client checks pass at GUI scales 3 and 2, including real status packets, hidden-HUD and deadline expiry. Fresh notifications occur twice around one silent refresh. Review accepted the correction.

Ruling: persist the namespaced state through NeoForge's existing player data rather than adding a separate SavedData store ? this avoids extra save paths and live-player maps while retaining offline-expiring deadlines. Block/HUD maintenance uses player-only ticks. The prior damage attribute cleanup remains unchanged.

Ruling: expiry assertions wait on elapsed wall time rather than a fixed client tick number ? GUI resize catch-up and asynchronous server scheduling can make those tick counts finish before a one-second deadline. No production timer change was needed.

Final hashes are recorded in docs/patches/pvp-combat-limits/artifacts.json and match both runtime fixtures and installed/fixed-pack copies. Handbook changes preserve distribution counts and use the existing catalog entries. No commit or push.
