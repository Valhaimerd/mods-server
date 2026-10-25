# Priest Tier III Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement the approved mobile Lightwell and expanded Barrier without regressing current Priest/party mechanics.

**Architecture:** Overlay exact current archives. Extend native summon tick/target selection and projectile interception with a focused `PriestUltimates` helper; preserve existing PriestRules/passive hooks. Data resources define coefficients, haste and upgrade descriptions; isolated probes exercise native behavior.

**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90, Java 25 bytecode, ASM, Python archive tools.

**Spec:** `docs/superpowers/specs/2026-10-26-priest-tier-three-design.md`, approved by the user's implementation request.

## Global constraints

- Exact current same-name JAR overlays; no historical-builder replacement of newer classes.
- Better Party safety and current Priest rework/passives remain intact.
- No personal worlds/installations, commit or push.
- Concise descriptions follow approved rewrite; numeric details stay in raw reference.

## Review focus

- Multiple wells share a recipient's two-heart cap; no refresh healing.
- Full-health debuffed allies remain useful targets with Cleansing Light.
- A departed owner cannot leave an orphan hovering well.
- Barrier protects only allies currently inside, without lingering immunity.
- Repeated/overlapping projectile contact cannot duplicate retaliation.

### Task 1: Current snapshots and data contract

Files: `docs/patches/priest-tier-three/{build.py,verify.py,artifacts.json}`.

- [x] Snapshot active Paladins, Skill Tree and Spell Engine; inspect native summon/Barrier ownership, target selection and damage paths.
- [x] Add exact resource checks for Lightwell 80s/16s/16 blocks/0.6 speed/5% inheritance/100% healing/16-tick action interval; Barrier 1s/60s/10 blocks/10s.
- [x] Implement resource overlay, all-Priest haste flags, upgrade changes and concise spell/tree names. Confirm checks and unrelated-byte preservation.

### Task 2: Mobile well and mote upgrades

Files: `docs/patches/priest-tier-three/{PriestUltimates.java,PatchUltimates.java,UltimatesProbe.java,run_probe.py}`.

Interfaces: helper `wellTick(SummonedEntity)`, `moteHit(...)`, effect registration and native hooks determined from installed signatures before editing.

- [x] Add failing isolated probes for well movement, injured-target priority, invalid owner and full-health useful upgrade targets.
- [x] Implement owner-head following and native targeting override; preserve summon lifespan/collision.
- [x] Implement one cleanse and per-recipient 2-heart/6s effect before native healing, no extra heal on grant/refresh, correct expiry cleanup.
- [x] Verify multiple wells, allies/pets, misses and current Priest passive behavior.

### Task 3: Barrier immunity and retaliation

Same helper/patch/probe files; reuse native barrier collision and spell damage path.

- [x] Add failing checks for damage families, exceptions, boundary departure and projectile duplicate interception.
- [x] Implement current-volume support protection for ordinary damage and repeated outward knockback; preserve kill/void/cost exceptions.
- [x] Implement once-per-projectile 25% Healing Power retaliation through native spell impacts with caster attribution; ignore friendly/unowned projectiles.
- [x] Verify Improved 12s, independent Sacred Refuge 4-block/10s cloud and normal damage outside.

### Task 4: Install, visual checks and documentation

Files: patch README/results, `docs/patches/priest-reference/{readable.md,export.py}`, `docs/MODLIST.md`, `mod fixing/README.md`, handbook PDF.

- [x] Run isolated server regression checks and connected-client hovering/barrier checks on exact candidates.
- [x] Review patch and preservation evidence; install matching active/fixed archives only after passing checks, archive originals ignored.
- [x] Regenerate raw reference, update handbook, render/review changed pages, verify links/PDF copies.
- [x] Report verified scope and limits; leave changes uncommitted/unpushed.
