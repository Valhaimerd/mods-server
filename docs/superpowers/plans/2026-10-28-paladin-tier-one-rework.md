# Paladin Tier-I Rework Implementation Plan

> Execution: inline by the primary agent using superpowers:executing-plans. No workers by user preference. No commit or push unless requested.

**Goal:** Give both tier-I Paladin choices ranged combat with conditional healing, make existing Paladin timers respond to haste, and set Justicar to8 total set Healing Power.

**Architecture:** Preserve existing spell/status/node identifiers and modify their visible names and behavior. Reuse native projectile/impact, power, party and cooldown systems. Add only the server combat-swing and successful-impact callbacks that native resources cannot express reliably.

**Tech stack:** Minecraft26.2, NeoForge26.2.0.90, Java25, Paladins3.1.2, Skill Tree1.6.1, Arsenal1.5.1, Better Combat3.2.2, installed Spell Engine APIs. Python ZIP repacking; same active filenames.

**Spec:** [Approved gameplay design](../specs/2026-10-28-paladin-tier-one-rework-design.md).

## Global constraints

- Current uncommitted Priest doctrine/armor work is the input, not a baseline to overwrite. Snapshot current archives before editing.
- Justicar1.5 per piece yields6 item Healing Power plus its existing2 set bonus =8. Other armor values remain.
- Preserve IDs, existing skill allocations, doctrine guards, scroll/loot references and unchanged tiers' coefficients/mechanics.
- Both new tier-I spells use M for damage and P for healing, without old mixed-school bonuses or double final-damage reduction.
- Use ordinary20-block straight aimed waves, speed1.4, width1.2x the verified Hammer of Destiny wave. Base maximum1 enemy; Wrath maximum2 distinct enemies,40% M. No homing or ricochet.
- Haste affects existing casts/channels/cooldowns/trigger intervals; preserve counts, buff durations and projectile speed. No invented cooldown on previously cooldown-free passives.
- Personal worlds and the separate desktop server remain untouched. Use isolated test instances, ignored backups and fixed packs.

## Review focus

1. Held attack in empty air, including aiming above flying targets: one wave per actual timed melee swing, not per mouse press or hit.
2. Multiple client/server/offhand callbacks: exactly one charge consumed and no duplicate projectile for a single swing.
3. Absolution with several enemies and protected allies nearby: one caster heal, no double damage on the primary target, no friendly fire.
4. Old generated configs, bindings and saved allocations: new values/visible names take effect without resetting the class or reviving old effects.
5. Channel interruption, death, respec, exhausted charges and glint expiration: no lingering free wave or obsolete bonus-hit heal.

## Files and responsibilities

Create under `docs/patches/paladin-tier-one/`:
- `build.py`: hash-pinned current inputs, compilation and entry-limited repacks.
- `PaladinTierOne.java`: narrowly scoped server delivery, once-per-cast successful-hit healing, legal swing wave consumption and lifecycle cleanup, if native APIs require custom callbacks.
- `PaladinTierOneProbe.java`, `run_server.py`: native isolated regression tests and hash manifests.
- `verify.py`, `README.md`: exact content scope, reproducible evidence and behavior notes.
- `client/`: isolated held-input/vertical aim/rendering probe and evidence, following the existing Paladin test setup.

Modify packaged resources in current same-name archives:
- Paladins `data/paladins/spell/flash_heal.json`, `blessed_strikes.json`; related retained status/impact resources and localization.
- Skill Tree `data/skill_tree_rpgs/spell/paladin_tier_2_spell_{1,2}_{root,modifier_1,modifier_2}.json`; class/weapon category definitions, localization and all Paladin timers discovered by audit.
- Existing Armory `net/armory_rpgs/item/HealingArmorBalance.class`: Justicar1.5 only; preserve Lightbringer1.75 and all other armor stats.
- A separate local golden wave model/texture under the owning modified mod; do not replace Arsenal's original shockwave texture or passive.
- Existing mixin/event entry points only where native delivery cannot supply required swing/impact callbacks; do not add a new mod dependency.

Synchronize `mod fixing/Paladin skills - raw attributes and nodes.md`, current description rewrite ledger, `README.md`, `docs/MODLIST.md`, patch notes and handbookPDF after tested installation.

### Task1: Lock inputs and validate native extension points

- [x] Snapshot current Skill Tree, Paladins, Armory and relevant Arsenal/Better Combat inputs under ignored `tmp/paladin-tier-one/before`; record hashes and metadata. Preserve unrelated edits.
- [x] Inspect installed Better Combat attack execution and server packet validation, native vanilla fallback, projectile custom delivery, successful damage callbacks, stash/glint behavior and power resolution. Confirm Hammer of Destiny actually binds the inspected Arsenal shockwave resource.
- [x] Record a per-skill/passive haste audit listing each timer, owning school and whether already scaled; inspect summon/internal cooldown paths too. Do not globally haste unrelated classes.
- [x] Add failing native expectations for exact requested resource values and Justicar8 full-set total; run against current inputs to prove failures are relevant.

### Task2: Justicar adjustment and Purge Heal

- [x] Extend existing armor helper with Justicar1.5 while preserving its2-point set bonus, Lightbringer1.75 and non-healing modifiers. Test fresh/stale equipment config and exact full-set totals.
- [x] Retain flash_heal ID but rename all user-facing skill/node/book/scroll text to Purge Heal and Absolution. Require enemy aim24 blocks,0.5s cast,5s cooldown; remove ally fallback and old cleanse/Divine Strength buffs.
- [x] Apply50% M damage and25% P caster healing only after successful enemy damage. Improved sets60%/30%; Divine Strength sets40% P caster/4-block ally healing; Absolution sets80% M with3-block damage radius, retaining Improved healing30% P.
- [x] Test direct hit, miss/no target, invulnerability/zero damage, multiple blast victims, primary-target deduplication, party/pet exclusions, caster/ally healing once, full-health targets and Conviction cooldown reset with retained IDs.

### Task3: Five prepared ranged swings

- [x] Set Blessed Strikes2.5s channel,5 pulses,14s cooldown,15s refreshed prepared duration and5-charge cap. Each delivery heals5% P and adds exactly one charge.
- [x] Replace old consumption-on-impact hybrid damage with a validated actual-melee-swing release. Holding left-click produces one release per legal scheduled weapon swing, even when no melee victim is in range. Use the native attack execution event/timing, not raw taps or unvalidated client claims.
- [x] Implement20-block pitched wave, speed1.4, width1.2x source,30% M, first enemy stops flight, terrain collision, no homing/falloff; preserve proposed0.5 knockback. Friendly entities do not consume the enemy-hit budget.
- [x] Improved reduces cooldown to12s without a sixth delivery. Wrath40% M and2 total unique enemy hits. Light heals10% P on every caster pulse and eligible allies within4 blocks for10% P on the fifth pulse only.
- [x] Keep weapon glint while charges remain. Clear charges/transient callbacks on death, expiration and class removal. Confirm existing status/modifier configs cannot revive old on-hit damage or old Seal of Light consumption healing.
- [x] Test held-input sequence, miss, quick tap, attack cooldown, empty-hand/mining/shield rejection, target flying above/below, two hands/duplicate callbacks, cancel channel before final pulse, charge cap, enemy hit budget and exact20-block lifespan/range.

### Task4: Haste, readable descriptions and integration

- [x] Enable missing native haste flags and scale only custom timers identified in Task1 using each skill's resolved school, once. Already-hasted cooldowns remain native; no double scaling.
- [x] Prove baseline and elevated haste: shorter casts/channel gaps/cooldowns, same5 pulses/5 charges, same15s charge duration, unchanged projectile speed and coefficients. Verify every Paladin passive with an interval/cooldown, including existing later tiers.
- [x] Use concise percentages in names/descriptions; native yellow rows carry cooldown and trigger where available. Remove stale renamed nodes/scroll descriptions and obsolete hybrid/cleanse effects. Preserve all non-Paladin wording.
- [x] Compile and run native tests with current doctrine guards, party integration and final player damage routing80% mobs/35% players; healing receives neither damage multiplier.

### Task5: Actual client verification and final delivery

- [x] Run the isolated client against hash-pinned candidates. Hold left-click with several melee weapon speeds and empty-air swings; verify five distinct waves, interval matching, diagonal/upward aim against flying enemies, terrain/target collision and no duplicate offhand release.
- [x] Inspect golden wave, dimensions, cast/channel effects, charge glint, icons, scroll/binding-table names and skill-node tooltips. Reuse existing suitable RPG icons; no missing placeholders.
- [x] Inline review of resource/bytecode entry diffs, changed scope, old configs, save/role transitions and absence of dedicated-server client-class references. Fix observed failures and rerun only affected checks.
- [x] Install final same-name JARs in `mods/` and fixed packs after preserving previous bytes in ignored retired packs. Assert installed hashes match tested artifacts and no duplicate active versions.
- [x] Update player/raw references and planned MD status; rebuild handbook, inspect all pages and affected pages, check TOC links and copies. Run `git diff --check` and exact artifact verifier. Record runtime results and any limits honestly; no commit/push.

Plan reviewed inline against all requested values and later clarifications. The armor-normalization idea remains outside this implementation. No additional worker is required.

## Completion evidence

Installed five same-name final archives.75 native checks pass fresh and75 with legacy equipment configs;24 actual client checks pass. Final artifact verifier confirms4674 preserved archive entries and exact tested/active/fixed hashes. Reviewed all79 handbook pages;51 TOC destinations match. No additional workers, commit or push. Concurrent class-armor registration changes preserved by rebasing disjoint entries before testing.

Ruling: reuse a separate copy of the native Arsenal shockwave geometry with Minecraft gold texture rather than recoloring the original texture ? yields a golden wave without changing the original weapon or introducing an image-generation dependency.
Ruling: server fixtures use flat isolated terrain ? random normal terrain correctly blocked Purge line of sight in an earlier test.
Fixture correction: load Better Combat dependency before overlaying candidate archives, so the final native manifest tests the patched callback rather than overwriting it with the old dependency. Both final server passes were repeated after this correction.
