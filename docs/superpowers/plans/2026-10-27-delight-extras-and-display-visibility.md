# Delight Extras and Display Visibility Implementation Plan

> For agentic workers: use superpowers:executing-plans inline, with one fresh final reviewer.

**Goal:** Deliver the three approved 26.2 ports and hide unavailable Display Delight items from all three discovery/acquisition sources.
**Architecture:** Preserve upstream add-on systems using source-level API/resource ports. A shared Display Delight source-item resolver feeds creative, JEI and trader eligibility while keeping registries stable.
**Tech Stack:** Java 25, NeoForge 26.2.0.90, existing FD/Display Delight source/build infrastructure, isolated native client/server probes.
**Spec:** ../specs/2026-10-27-delight-extras-and-display-visibility-design.md

## Global constraints

Keep all existing saved IDs and pack fixes; target the actual installed FD 1.3.3 fix5. No metadata-only ports. No personal instances/worlds. Install only final tested JARs; keep same Display Delight filename. No commit/push requested. User has approved execution; proceed without another planning approval.

## Review focus

- Source mod present but food absent: hide that individual display, never use bowls as dummy inputs.
- Saved traders and empty offer pools: remove only invalid Display Delight offers; keep ordinary trades.
- Client initialization/reload order: no server-only availability cache or stale/duplicated maps.
- Real food/container/stove behavior: native recipes and display return paths must work, not merely boot.
- Old worlds: registry IDs and existing display blocks remain, even when discovery is hidden.

## Tasks

### 1. Inputs and baseline
- [x] Pin three upstream branches/commits, licenses and supplied JAR hashes under ignored mod fixing/source/delight-batch-20261027; snapshot active FD/Expanded/DD JARs under tmp/delight-batch/before.
- [x] Record original target mismatch and current missing-addon JEI/creative/trader behavior. Establish build/runtime classpaths from current installed dependencies.

### 2. Ocean's Delight
- [x] Port source/resources preserving IDs, all foods and native preparation paths; add/execute input/resource/native recipe checks. Build final candidate with correct 26.2 metadata.

### 3. Large Meals
- [x] Port source/resources preserving all meals, feasts/servings, food effects and recipes. Verify preparation, serving and display mapping. Build final candidate.

### 4. End's Delight
- [x] Port source/resources preserving End food systems, End stove/block entity/renderer and all native recipes. Verify stove/cutting/serving/containers. Build final candidate.

### 5. Display Delight shared visibility
- [x] Add a direct source-food resolver independent of server-only initialized maps; preserve official aliases and display categories.
- [x] Write native/JEI/creative/trader regression assertions; observe missing-addon baseline failures.
- [x] Filter JEI ingredients/info, creative/search and new/saved trader offers with the same eligibility rule. Empty categories omit offers. Clear/rebuild derived maps safely across reloads. Preserve retrieval/empty-plate behavior and registry IDs.

### 6. Integration and final review
- [x] Test the base/individual/all-three server matrix and isolated client with JEI, recipes, consumption, placement/serving, rendering and absent-source cases; record exact outcomes and screenshots.
- [x] Capture source patches/manifests and reproducible build/test instructions under docs/patches/delight-batch. Verify final archives and independent whole-change review, fixing actionable findings with focused checks.

### 7. Delivery and documentation
- [x] Back up old DD, install all final JARs in mods plus matching fixed copies; verify hashes and no duplicate mod IDs.
- [x] Reconcile handbook entries/indexes/counts/sources/icons, README/mod-fixing notes and PDF; render/review and verify contents links. Preserve all unrelated user changes and ignored backups. Report verified behavior and any material limits without claiming unobserved runtime coverage.

## Completion evidence

Final installed hashes: `docs/patches/delight-batch/installed.json`. Native exact-installed combined397 checks; final client base135/all322 checks; Ocean455 and Large124 checks; End six-slot cooking/packet roundtrip. Independent final review found no actionable issues; late resource-only visual fixes were followed by one complete all-client rerun with zero missing textures. All14 client screenshots and all78 PDF pages reviewed; mod/icon/index/TOC/output-hash checks passed. No commit or push. Personal installations untouched.
