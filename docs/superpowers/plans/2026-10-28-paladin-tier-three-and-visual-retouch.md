# Paladin Tier III and Visual Retouch Implementation Plan

> **Execution:** Primary agent inline with `superpowers:executing-plans`, task by task. No implementation workers. **GO received29 October2026: execute this plan inline.** This plan does not authorize code edits, JAR replacement, mod removal, installation or publishing before that signal.

**Goal:** Implement the Paladin gameplay rework and Paladin visual retouch, retire Cute Cats in favor of the user's supplied Enhanced Cats, and leave the other classes' proposed retouches for a later pass.

**Architecture:** Extend the existing Spell Engine/Paladin native casting, impact, effect, party and received-detail handlers. Keep per-cast support and channel state separate, using existing native resources for visuals and the established same-name repack/evidence workflow. Treat the cat replacement as a separate, independently verified pack change.

**Tech Stack:** Minecraft26.2, NeoForge26.2.0.90, Java25; current installed RPG Series/Spell Engine/Skill Tree/Effect Insights; Python archive tooling and asserted ASM patches.

**Spec:** [Paladin tier III design](../specs/2026-10-28-paladin-tier-three-design.md). The [class visual draft](../specs/2026-10-28-class-visual-retouches-design.md) is a deferred backlog; its other-class proposals are not execution scope.

**Required wording reference:** [RPG Skills - description rewrite.md](../../../mod%20fixing/RPG%20Skills%20-%20description%20rewrite.md), especially “Direction from brainstorming.” The original rewrite excluded Priest/Paladin from its historical list of196 keys; the user's latest instruction explicitly applies its writing rules to this Paladin pass. Use the current gameplay spec for facts and the rewrite MD for style, not older balance examples as current values.

## Current scope and hold

- Implementation authorized by the29 October2026 go signal.
- Current gameplay work: moving Battle Banner, Purgatory and its upgrades, Ardent Defender and Reinforced.
- Current presentation work: Paladin tiersI–III and passives, with emphasis on the reworked ultimates and truthful received-state feedback. Preserve tierI/II mechanics already installed.
- Pack housekeeping: retire `mods/cutecats-neoforge-26.2-1.0.jar` and validate the already-present `mods/EnhancedCats-NeoForge-26.2-1.2.1.jar` as the replacement.
- Defer Archer, Rogue, Warrior, Priest and Arcane/Fire/Frost retouches. The earlier Rogue Vanish mark-clearing request remains recorded for the Rogue pass, including its cleanse shimmer; do not silently discard it or implement it in this Paladin-only batch.
- Preserve current uncommitted edits, the user's newly added Enhanced Cats JAR, and ignored backups. No commit, push, personal `.minecraft` edits or separate desktop-server edits unless later requested.

## Confirmed gameplay decisions

- Purgatory self-cost occurs every pulse:2 HP in the middle phase and3 HP in the final phase. Ordinary invulnerability, reduction and absorption cannot remove that cost.
- Final-phase invulnerability is0.2s +0.1s per complete10 P, capped0.7s. Continuous ordinary-damage protection is allowed at sufficient P.
- Ardent Defender benefits only the triggering party player or caster, never pets. Its90s cooldown is shared across possible recipients, and it does **not** cancel a fatal hit.
- Banner caps apply to accumulated bonuses. Intense adds5 percentage points to each cap, excluding healing.
- Keep visible names, skill-tree nodes, scrolls, spellbook details and status names consistent while retaining saved internal IDs.
- The exact draft defaults—fixed pulse/stage timing, hybrid weighting, Ardent buff duration, Damnation interpolation and lifecycle ordering—are listed in the linked spec. Re-read the latest user instructions when the go signal arrives; do not invent additional choices from silence.

## Observed cat inputs — read-only inventory

| Active file | Embedded identity | SHA-256 |
|---|---|---|
| `cutecats-neoforge-26.2-1.0.jar` |`kingdomcats`, Kingdom Cats Replacer,1.2.2-neoforge.1; metadata describes a client-only replacement |`b18b31a1f6d06dba251c540f5f181b7e3532b5fa2419ef52dea02182daea1adb` |
| `EnhancedCats-NeoForge-26.2-1.2.1.jar` |`enhancedcats`, Enhanced Cats NeoForge26.2,1.2.1 |`9d14cf4397733fcce03f524f66d74ffeb1a0c3bbd0cea3095a2e59fd38ef125f` |

Both files were present in `mods/` when planning. Enhanced Cats declares NeoForge>=26.2.0.88, Minecraft26.2 and a client YACL dependency whose version range contains the literal placeholder `[YACL_VERSION_HERE,)`. This is an observed metadata issue to investigate, **not a runtime failure already proved**. Verify exact installed YACL compatibility, official source and actual side behavior before deciding placement/readiness. Do not infer behavior from filenames.

## Global constraints

- Preserve80% player/summon damage to mobs,35% to players, and the installed25% holy-skill bonus against undead. Do not apply these to healing or direct caster HP costs.
- Keep party players and protected pets safe from harmful effects; use the spec's explicit player-only Ardent rule.
- Keep native cooldown/trigger rows and brief result-first node wording. Use `%`; no raw placeholders, floor expressions or repeated haste/cooldown explanations in descriptions.
- Read the dedicated description-rewrite MD before drafting copy. Most descriptions should be one sentence, occasionally two; retain additional text only for essential ultimate risks or survival exceptions. Describe an upgrade's added benefit instead of repeating its parent. Keep detailed scaling arithmetic in raw references and show resolved received amounts in effect tooltips. Use short times such as `6s`; apply the engine's required `%%` escaping only in the relevant language resources, so players see `%`.
- Support/status details show caster-resolved received values. Shield refreshes never refill consumed HP.
- Preserve active filenames for existing-version repacks. Build/test before replacing active/fixed JARs; retain ignored rollback archives.
- A compilation result is not runtime proof. Use isolated native server/client fixtures and visual captures.

## Review focus

1. Simultaneous final pulse, lethal damage and interruption must produce exactly one explosion and consume only the relevant protection.
2. Overlapping Banners need independent source amounts/caps/expiry; weaker casts cannot prolong stronger values or preserve Intense caps.
3. Direct Purgatory HP costs must bypass ordinary protection but still follow the explicit Final Resolute/Ardent rules.
4. Stealth/hidden traps and other classes' mechanics must remain unchanged by shared rendering/helper edits.
5. Death, logout, dimension change, dispel and respec must leave no stale glint, pose, audio loop, aura or stat modifier.

## File responsibilities

Create implementation/evidence under `docs/patches/paladin-tier-three/`, following `docs/patches/paladin-revamp/` patterns:

- `PaladinUltimates.java`: Banner cast/recipient state, Purgatory channel/end state, defensive arbitration and native delivery integration.
- `PaladinUltimates.java` also owns player-only Ardent observation and low-health Reinforced updates; no separate passive helper is needed.
- `PaladinVisuals.java`: Paladin event-driven particle/sound/glint presentation; keep client-only types isolated where required.
- `PatchTierThree.java`, `build.py`: exact asserted hooks, resource/language/node edits and same-name archives from pinned current inputs.
- `TierThreeProbe.java`, `run_server.py`, `verify.py`, `client/`: native acceptance fixtures, exact tested hashes, actual client captures and archive preservation checks.
- Reuse/extend current `net.spell_engine.internals.EffectDetails` and existing Paladin helper sources narrowly; do not replace the completed tierI/II implementation wholesale.
- `docs/patches/cat-replacement/`: metadata/side findings, replacement checks, retirement record and final installed hashes.
- Canonical references: `mod fixing/Paladin skills - raw attributes and nodes.md`, `mod fixing/README.md`, `README.md`, `docs/MODLIST.md`, icon/index manifests as needed, `modpack-player-handbook.pdf`.

Proposed shared helper contracts, finalized against native APIs at execution:

- `startBanner(LivingEntity caster)`: register one cast with stat snapshots, fixed pulse schedule, radius/lifetime and unique-recipient rescue use.
- `tickBanner(...)`: advance one due pulse, accumulate source-separated recipient values and perform eligible rescue checks.
- `deliverPurgatoryPulse(...)`: process a numbered native delivery and its exact stage, targeting, protection and HP cost.
- `endPurgatory(..., EndReason reason)`: reserve one terminal state before native explosion/shield impacts; distinguish release, completion, death, Final Resolute and cleanup-only exits.
- `updateReinforced(ServerPlayer player)`: reconcile one current-health band without stale or duplicate modifiers.
- `tryArdent(...)`: reserve the owning Paladin's shared cooldown and grant benefits to one qualifying player without cancelling incoming damage.
- Effect-detail output continues through the existing synchronized attachment path; do not compute caster-scaled values from recipient stats.

## Task1 — Pin inputs and validate the cat replacement

- [x] After the go signal, re-read current requirements/status and pin actual JAR bytes in an ignored task workspace; preserve all unrelated changes.
- [x] Inspect Enhanced Cats metadata/mixins/entrypoints and official source for dependencies and side compatibility. Check installed YACL against its real API/version requirements; reproduce any placeholder-version problem before repairing metadata.
- [x] Boot the smallest isolated server/client set that exercises Enhanced Cats and verify representative cat rendering/behavior. Do not label it server-compatible from TOML side declarations alone.
- [x] Once validated, archive Cute Cats/Kingdom Cats Replacer in ignored retired packs and remove its active JAR. Keep the user's Enhanced Cats bytes unless a specific compatibility fix is necessary and verified. Do not remove GeckoLib/YACL merely because one consumer is retired.
- [x] Record identities, before/after hashes, exact side placement and observed results. Recompute counts instead of assuming old baselines.

## Task2 — Battle Banner gameplay and visuals

- [x] Add native failing acceptance cases for following/radii6/8, lifetimes20/25s, fixed0.5s pulses at differing haste,3s buff duration, every stat's gain/cap and Intense's+5-point caps.
- [x] Implement one following cast state with caster snapshots and source-separated recipient accumulation. General and additional magic reduction follow the spec's independent layers; flat healing remains immediate rather than an accumulating stat.
- [x] Test one2s rescue per unique ally per cast, caster inclusion, departure/re-entry, overlapping casts and no charge expenditure on unpreventable self-cost.
- [x] Add the shoulder-carried banner, restrained following boundary, outward pulse and a distinct rescue flash/chime. Distinguish damage reduction, healing and invulnerability; preserve first-person readability.
- [x] Confirm actual received percentages, rescue availability and expiry in native client tooltips. Run the Banner acceptance cases to completion before extending the next subsystem.

## Task3 — Purgatory stages, endings and visuals

- [x] Add failing native cases: first pulse0.5s,60 deliveries through30s,10/20s boundaries, per-pulse2/3 HP costs, fixed timing under haste, damage/pull/support radii and caster exclusion from ally regeneration/shields.
- [x] Replace the charged Immolation release/old self-heal/Consecration trigger with the30s staged channel and the spec's cooldown rules. Keep the stance, movement and interruption policy explicit.
- [x] Verify hybrid weighting, burn duration, taunt/pull eligibility, stage regeneration and0.7s maximum ordinary immunity. Prove direct HP costs still land through immunity, armor and absorption.
- [x] Implement single terminal-state arbitration. Test partial releases,700% completion/death bursts, Damnation's900% endpoint,1s completion stun,8s ally shields and anti-refill behavior.
- [x] Implement Final Resolute's final-phase low-health/lethal exit, earned damage, ally shield and3s caster protection without an unrequested heal. Test simultaneous fatal self-cost/final delivery and competing rescue effects.
- [x] Add gold/orange/deep-red stage presentation, fire/spark particles, orange weapon glint, separate damage/pull boundaries and an earned-strength fiery-gold blast. Reuse native RPG resources; verify rings match real geometry and every end path clears loops/pose/glint.

## Task4 — TierIII passives and Paladin-wide presentation

- [x] Add failing cases for Ardent's6-block player-only observation, one recipient,90s shared haste-affected cooldown, Resistance I/+10 armor/+5% reduction/40% maximum-HP healing and benefits-only fatal behavior.
- [x] Replace the old Ardent effects without retaining its obsolete maximum-health bonus. Verify huge hits can still kill and do not silently gain immunity.
- [x] Add Reinforced checks at70/60/50/40% health bands for5/10/15/20% base Healing Power and armor, proposed0.25s checking, downward adjustment after healing and cleanup on death/respec.
- [x] Remove the old Divine Hammer projectile/proc. Install Reinforced/Ardent status names, actual values and restrained gain/activation cues.
- [x] Retouch existing Paladin tiersI/II and their passives without changing installed mechanics: Purge/Absolution impacts, prepared Blessed waves/charge consumption, Protection/Oath boundaries and target impacts, Vanguard/Guardian stack gains, Sinful readiness/retaliation and Break Restriction cleanse.
- [x] Verify selected upgrade visuals communicate only actual effects, including Wrath's real hit count, capped protection and Oath shield anti-refill. Leave other classes' visuals alone.
- [x] Draft and review a before/after wording table for every affected Paladin skill, upgrade, passive and status using `mod fixing/RPG Skills - description rewrite.md`. Keep Purgatory's self-damage, interruption/end consequences and Ardent's lack of guaranteed survival clear without transcribing the raw request. Check names across tree titles, scrolls, spellbooks and effects.
- [x] Inspect actual native tooltips: player-visible `%` signs, no `{impact}`/floor/formula placeholders, no duplicated cooldown/range/trigger/haste boilerplate, and no stale description claims. Confirm native yellow statistics/trigger rows still provide their values and custom effects show actual received amounts.

## Task5 — Integrated verification, installation and documentation

- [x] Run focused fresh-config and stale-config server fixtures, plus an isolated actual client. Record exact mod versions and tested archive hashes.
- [ ] Exercise a party fight with allies/pets, a nonparty player, moving/flying targets, simultaneous fatal events, overlapping Banner casters and reduced-particle settings. Review representative screenshots and the actual channel/ending motion.
- [x] Check archive entry preservation, native metadata, Java25 bytecode, unchanged IDs/tree exclusivity and absence of probe code. Confirm all final archives are exactly those tested.
- [x] Review inline, install matching same-name active/fixed repacks and preserve rollback archives. Retire only the intended Cute Cats file after replacement validation.
- [x] Update Paladin raw references and current player documentation; replace the Kingdom Cats/Cute Cats catalog entry and its indexes/source/icon references with verified Enhanced Cats information. Keep deferred class proposals out of installed-behavior documentation.
- [x] Follow `docs/HANDBOOK_MAINTENANCE.md`; rebuild both handbook PDF copies, validate TOC destinations and artifact equality, render/review all pages and changed pages at reading resolution.
- [x] Run `git diff --check`, check installed/fixed/tested hashes, report observed results and limitations. No GitHub push or personal installation edits without a separate request.

## Deferred next-class backlog

1. Rogue: requested Vanish mark cleanse and stealth-safe visual pass.
2. Archer, Warrior and Priest visual passes.
3. Arcane, Fire and Frost visual passes.

The user chooses the next class after Paladin; the numbering above records the pending work, not permission to execute it automatically.

## Execution evidence ?29 October2026

Implemented/installed from pinned current inputs:196 fresh and196 stale native checks,36 real-client checks,3736 unchanged archive entries. The one final review?s interruption, dispel and magic-source findings have RED?GREEN regressions. See [implementation/evidence](../../patches/paladin-tier-three/README.md). Dedicated server includes Enhanced Cats without client YACL; its actual client renderer is confirmed. Cute Cats is archived and removed.

Coverage limit: native Better Party fixtures exercise support/hostile eligibility and damage outcomes, while the real client exercises model following, staged channel motion and tooltips. A full live multiplayer fight, every airborne/moving target and every reduced-particle/shader combination have not been manually exercised; the broader optional acceptance line remains unchecked rather than claimed.

Handbook finished:79 pages,51 correct TOC destinations/numbers,184 alphabetical entries, no broken HTML anchors or blank pages; all pages rendered and reviewed, changed pages3/4/17/18/68 reviewed at reading resolution. Root/output PDFs match.
