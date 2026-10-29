# Other Class Visuals and Effect Icons Implementation Plan

**29 October continuation installed:** [Paladin support and Vanish plan](2026-10-29-paladin-support-and-vanish.md). This supersedes the original Vanish full-hide/no-glint rows only: body, armor and held equipment now share the approved0-10% violet breathing shimmer, while later reveal effects remain effective. Paladin scaling/passive changes and225 fresh/stale checks are recorded in `docs/patches/paladin-support-balance/`; Rogue rendering passes74 client checks.

## Follow-up: Divine Protection attack blocks (2026-10-29)

**Installed:** same-name Paladins/Spell Engine repair; 27 fresh-config and 27 stale-config native checks pass. Descriptions, local class reference and reviewed 79-page handbook are synchronized. Evidence: `docs/patches/protection-hit-charges/`.

The owner clarified that mitigation means a finite number of qualifying attacks, not flat HP removed from each hit. Implement this correction first, independently of the pending Vanish and Paladin passive redesigns.

- Preserve the existing cast snapshot: one attack block plus one per complete 10 Melee Power. At 17 M, block two attacks.
- Block hostile living-attacker damage, including attributable projectiles and magic, before absorption is consumed. Environmental damage, self-costs, party damage and invulnerability-bypassing damage must not spend charges.
- Keep percentage damage reduction active after charges are spent; apply the approved 40% cap. Preserve duration, party eligibility, knockback resistance, caster power and Merciful Recovery.
- Show remaining attack blocks in status details and remove flat-HP wording from node descriptions and player documentation.
- Reproduce the defect using native dedicated-server damage calls; verify count scaling, melee/magic blocks, absorption preservation, exhausted charges, environmental hits, recast and dispel.
- Repack current JARs under their existing filenames, archive local rollbacks, synchronize the reference and handbook, rebuild/review the PDF. No GitHub push is requested in this follow-up.

> **Execution:** Primary agent inline using `superpowers:executing-plans`, class by class, preserving the user's preference to minimize workers. Implementation is installed; see the evidence below. No GitHub synchronization is included without a new request.

**Goal:** Repair missing buff/debuff icons and make remaining class effects readable, accurate and consistent with the completed Paladin pass.

**Architecture:** Repair exact native sprite paths first. Reuse existing class models, particles, sounds and current cast/impact/status hooks; introduce a small class-specific helper only where the installed implementation lacks an appropriate presentation hook. Bind visuals to actual events and state, without a new global visual framework or parallel gameplay timers.

**Tech Stack:** Minecraft26.2, NeoForge26.2.0.90, Java25; installed RPG Series, Spell Engine, Skill Tree, Effect Insights, Better Combat and player-animation integrations. Python archive/asset inspection and narrowly asserted ASM hooks follow existing patch records.

**Spec:** [Class visual direction and29 October audit](../specs/2026-10-28-class-visual-retouches-design.md). [Current icon evidence](../../patches/class-visual-audit/README.md). Wording uses `mod fixing/RPG Skills - description rewrite.md`; its historical Priest exclusion does not override the user's later instruction to use that style across classes.

## Installed result and acceptance record

See [the shipped patch](../../patches/class-visual-retouch/README.md): six matching active/fixed repacks,95 presentation resources, three repaired native status icons,360 unchanged gameplay spell documents,109 resolved/nonempty status sprites,229 fresh and229 stale native server checks,166 full-client and136 reduced-particle/display checks. Official control art and existing Paladin visuals remain. The source audit is historical and hash-pinned.

This implementation reuses existing native models, motion, sounds and counters; the tables below remain design/acceptance detail rather than a claim that every illustration was newly implemented. HUD/inventory and observed-RemotePlayer render checks supplement atlas checks. The latter is a native renderer fixture, not a second connected player. Unchecked acceptance cases are not silently claimed: crowded party/hostile network playback and all lifecycle/branch combinations remain unverified. No commit or push is included.

## Global constraints

- Preserve damage, counts, hitboxes, range, cooldowns, haste, charge scaling, party/pet eligibility, absorption accounting, PvE/PvP routing and all installed Paladin behavior.
- The visual size of a projectile/beam must communicate its actual collision/damage footprint. Never widen hitboxes as a cosmetic shortcut.
- Reuse native skill/effect artwork for missing icons. Preserve the supplied official Stun, Root and Frozen icons and their distinct meanings.
- Node text is brief and result-first, with `%` and useful durations. Native gold statistics/trigger rows carry cooldown/range/activation data. No floor expressions, impact placeholders or repeated haste boilerplate.
- Status details show actual received amounts, stacks, charges, shield HP or stage. Caster-scaled support uses the original caster snapshot, not the viewer's attributes.
- Stealth, Fade and hidden traps retain existing visibility. Cosmetic cues cannot expose a hidden player or reveal an armed hidden trap.
- Heal/shield/cleanse/rescue feedback occurs only when the corresponding event happens. Refreshing spent absorption cannot look like restored shield HP.
- Preserve same active JAR filenames for same-version repacks. Pin current inputs, test before installing, archive rollbacks in ignored folders and keep current unrelated changes.
- Planning alone needs no handbook/PDF rebuild. Each installed player-facing batch follows the repository documentation/PDF workflow. Never describe proposed visuals as installed behavior.

## Review focus

1. A valid PNG can still be absent in-game because the atlas path or show-icon flag differs; inspect native HUD/inventory rendering after a real proc, not merely an archive entry.
2. Reused stealth art/particles must not reveal a hidden player to an observer; test caster, party and hostile views plus first/third-person equipment.
3. Reapplying a capped stack or shield must not invent a new gain, heal or charge; test refresh, consumption and expiry separately.
4. Server-only event hooks must not load client renderer classes or duplicate cues on both sides; boot a dedicated server and compare observer counts with native deliveries.
5. Death, dispel, interruption, teleport, dimension change and respec must clear relevant presentation; normal effects may retain only their actual gameplay tail.

## Evidence and files

Already created for planning:

- `docs/patches/class-visual-audit/audit.py`: read-only archive inspection; validates registry-export hashes before using the109-effect baseline.
- `registered-effects.json`, `registry-baseline.json`, `icon-audit.json` in that folder: native registry data, exact archive identities and per-effect sprite findings.
- Existing class-specific proposals remain in the spec's active/passive tables; each batch below implements those rows.

For execution, use `docs/patches/class-visual-retouch/` with `icon-map.json`, `build.py`, `verify_assets.py`, and native `server/` / `client/` probes. Keep each class?s helper under its own subfolder when a hook is necessary. The builder records before/after hashes and changed entries; no new generic event bus, renderer registry or polling system.

Current archive responsibilities:

| Archive | Changes permitted in this pass |
|---|---|
| `rogues-neoforge-3.1.2+26.2.jar` |Missing Rogue icons and Rogue/Warrior native presentation resources |
| `archers-neoforge-3.1.2+26.2.jar` |Archer presentation resources |
| `wizards-neoforge-3.1.2+26.2.jar` |Arcane/Fire/Frost presentation resources |
| `paladins-neoforge-3.1.2+26.2.jar` |Priest presentation only; installed Paladin content is a regression baseline |
| `skill_tree-neoforge-1.6.1+26.2.jar` |Missing status art and necessary class effect names/details |
| `spell_engine-neoforge-1.10.5+26.2.jar` |Narrow event/state/detail hooks when resources alone cannot represent actual mechanics |
| Better Combat / Player Animation / Effect Insights |Read existing integration first; change only after reproducing a concrete integration defect |

## Task1 - Repair status icons before visual expansion

**Files:** create execution `icon-map.json`, `build.py`, `verify_assets.py` and native client icon probe; add only the diagnosed assets to Rogue and Skill Tree archives.

| Exact destination | Existing source |
|---|---|
|`assets/rogues/textures/mob_effect/improved_stealth.png` |`assets/rogues/textures/spell/vanish.png` |
|`assets/rogues/textures/mob_effect/tracking_disruption.png` |`assets/rogues/textures/spell/shadow_step.png` |
|`assets/skill_tree_rpgs/textures/mob_effect/cheat_death_evasion.png` |`assets/skill_tree_rpgs/textures/mob_effect/cheat_death.png` |

- [x] Pin current production archives. Refresh the native registry export if any baseline hash differs; the saved audit currently reports109 effects,3 missing files,0 empty files and0 missing names.
- [ ] Reproduce the three absent sprites in an isolated native client. Trigger Improved Vanish, Shadowstep disruption on a mob, and Cheat Death's six-second evasion tail. Inspect deliberate `showIcon`/particle flags before changing them.
- [x] Add a resource regression asserting every registered class/control ID resolves to valid nonempty RGBA artwork through the native effect atlas. It must fail for the three current paths before repair. Copy the matching native art, using native-sized pixel art with readable transparency.
- [x] Verify0 unresolved sprites against the refreshed registry and actual native rendering, including inventory and HUD where the status is intended to appear. A hidden internal status may remain hidden while its missing texture is still repaired.
- [x] Verify English names/descriptions and native stack counters; no raw translation keys, missing-texture checkerboards or temporary placeholders. Retain official control icons.
- [ ] Save icon mappings, provenance, current/tested hashes and before/after captures. Install this independently verifiable resource repair before layering cosmetic hooks.

## Task2 - Rogue visibility, impacts and readiness

**Files:** execution `rogue/` helper/probes only where needed; existing `RogueRules`, `TrapRules`, `RogueTierThree` and spell/effect resources are the integration points to inspect from the installed JARs.

- [ ] Apply the spec's Shock Powder/Slice & Dice, Shadowstep/Bear Trap and Vanish/Mutilate rows, plus all three passive pairs. Acknowledge actual hits/procs and remaining prepared attacks.
- [ ] Distinguish target-loss disruption from immobilization. Fracture shows armor loss; Bleed?s cue represents the actual debuff, without implying every moving target is stunned.
- [ ] Keep armed hidden traps unseen. Vanish and Shadow Stealth get no sustained observer-visible smoke, glint or equipment. Fade-out/arrival cues cannot continue following a hidden owner.
- [ ] Native checks: `stealth_observer_sees_no_trail_or_equipment`, `armed_trap_does_not_gain_visual_reveal`, `ready_hit_consumes_once`, `capped_refresh_does_not_flash_gain`, and actual proc/expiry cleanup.
- [ ] Separately track the already requested Vanish mark cleanse. If included at execution, test successful-cast removal of actual registered mark/reveal sources, preserve unrelated damage debuffs and allow later reapplication. Do not label that gameplay change an icon fix or let visual work implement it implicitly.
- [ ] Capture caster and observer views in first/third person, natural expiry, action break, damage break, Shadow Stealth and death. Preserve current damage and stealth rules.

## Task3 - Archer projectile cadence, nature zones and charges

**Files:** execution `archer/` helper/probes if needed; native Archer spells/projectiles and current `ArcherRules`/ranged integration hooks.

- [ ] Apply all Archer rows in the spec: amber prepared shots, nature/root boundaries, individual Barrage launches, wolf assembly/expiry, actual Rain scatter and Magic Arrows' fan/homing transition.
- [ ] Keep actual per-shot bow/crossbow animation delivery in sync, including held firing. No extra gameplay arrows, repeat damage or artificial firing delay to make an effect visible.
- [ ] Rain's cue follows its real center. Impact splashes match real hits; search/scatter area must not imply continuous full-area damage. Conjured reveal appears only on a real hit.
- [ ] Rhythm, Supercharge and Deflection show real stacks/readiness/remaining charges. Concussive Shots applies the shared stun treatment only on a real proc.
- [ ] Native checks: `one_release_cue_per_actual_arrow`, `rain_boundary_tracks_current_center`, `homing_delay_matches_gameplay`, `deflection_3_2_1_consumes_once`, `expired_wolf_leaves_no_marker`; compare first-person and observer playback against server deliveries.

## Task4 - Warrior weight, charge and defensive distinctions

**Files:** execution `warrior/` helper/probes if needed; installed Warrior rules, channel/ultimate lifecycle, native held-weapon rendering and existing armor/health effects.

- [ ] Apply all Warrior rows: real Throw Weapon trails/retargets, net pull, charge/shout boundaries, readied Concussion hit, Berserker/Last Stand pulses and all passive pairs.
- [ ] Strong Throw acknowledges its real maximum of two targets. Net visuals follow actual movement/collision. Do not create a damage-looking stationary range field for a traveling projectile.
- [ ] Preserve the installed50% Last Stand glint and sword-ground pose. Distinguish normal ending from fatal-hit rescue. Last Stand is not rendered as damage immunity.
- [ ] Endured Vitality/Sentinel acknowledge actual new stacks, not every refresh; Tenacity distinguishes new shield HP from timer refresh. Enrage follows real threshold/size changes.
- [ ] Native checks: `throw_trail_uses_actual_weapon_and_two_hits`, `channel_pose_and_glint_clear_on_all_exits`, `vitality_max_refresh_has_no_heal_flourish`, `last_stand_visual_does_not_claim_immunity`, `tenacity_remaining_hp_matches_spent_shield`.

## Task5 - Priest destinations, support and quiet protection

**Files:** execution `priest/` helper/probes if needed; installed `PriestRules`, Priest passive/absorption helpers, Lightwell targeting/delayed-mote integration and Barrier native volume.

- [ ] Apply all Priest rows: real Holy Light/Penance delivery impacts; Circle/Ascension pulses; Lightwell/Barrier destinations; and all passive pairs.
- [ ] Penance distinguishes enemy harm from ally support and acknowledges caster support outside the blast without drawing a false explosion at the caster. Serenity/shield cues only acknowledge genuinely new HP.
- [ ] Preserve Lightwell's lowest-raw-HP targeting and0.3s same-target second-mote offset. Different eligible recipients do not receive a new cosmetic delay. Do not show a heal impact before arrival.
- [ ] Barrier and Sacred Refuge use their own actual areas/lifetimes. Celestial Orbs represent five real charges; consume one on the actual eligible event. Pain Suppression changes only on health-band transitions.
- [ ] Native checks: `two_motes_one_target_remain_separate`, `full_health_target_gets_no_false_heal`, `shield_refresh_preserves_spent_hp_and_visual`, `fade_hides_equipment_and_trails`, `orbs_render_actual_remaining_count`; verify party/pet eligibility without changing it.
- [ ] Keep all installed Paladin tests/captures as shared-helper regression coverage.

## Task6 - Mage paths as three independently verified batches

**Files:** execution `mage/arcane/`, `mage/fire/`, `mage/frost/` only when needed; native Wizard resources, summon/projectile code and current received-effect helpers. Do not split existing gameplay helpers merely for this cosmetic pass.

- [ ] **Arcane:** apply all active/passive rows. Beam width/trails follow real geometry; Emitters fire from real origins; Exposure and Evocation show actual levels/risk. Ward absorption and Phase Shift immunity remain visually distinct. Test `beam_visual_matches_width`, `emitter_cue_matches_real_bolt`, `blink_arrival_cue_uses_fixed_arrival`, `ward_refresh_does_not_look_refilled`.
- [ ] **Fire:** apply all active/passive rows. Breath/hydra effects follow actual aim; Meteor and Firestorm telegraphs match actual landing/pulses; walls expose real placement/remaining cast allowance. Great Wall must not visually promise projectile blocking. Test `hydra_one_actual_cone`, `persistent_fire_stops_at_expiry`, `wall_counter_matches_casts`, `flame_ward_not_fire_immunity`.
- [ ] **Frost:** apply all active/passive rows. Chill/slow must not look like Frozen; Frost Shield?s immunity ends exactly with its real ending, while Frost Ward remains absorption. Preserve actual Elemental count/scale and Super Nova area. Test `frozen_icon_only_on_frozen`, `shield_expiry_removes_protective_look`, `legion_and_colossus_match_real_entities`, `nova_ring_matches_each_actual_blast`.
- [ ] For each path, capture base and branch variants, useful ready/consumed states, summon expiry, death/dispel and low-particle readability before proceeding to the next path.

## Task7 - Per-batch acceptance and final delivery

- [x] Each batch passes archive preservation checks, native server behavior checks and an actual client with matching archive hashes. Use a fresh status and a stale/saved status where relevant.
- [ ] Inspect native effect rows for useful received percentages, penalties, charges, remaining absorption and stages. Fix a missing detail through the existing synchronized detail path; do not compute support values from the recipient?s unrelated power.
- [ ] Dedicated server loads without client renderer dependencies. Compare caster, party member and hostile observer views, moving/airborne targets, overlapping supports and a crowded party scene. Test normal/reduced particle settings; essential icons and native animations remain readable even when decorative particles are reduced.
- [ ] Verify no stale aura, glint, icon, loop or phantom projectile after interruption, dispel, death, teleport, logout, dimension change or respec. Distinguish actual remaining gameplay tails from stale presentation.
- [x] Use one focused final read-only review after inline implementation according to the chosen execution workflow; keep implementation worker count at zero. Preserve earlier Paladin changes when shared helpers are touched.
- [x] Install exact tested same-name active/fixed JARs and retain ignored rollbacks. Update class raw references, patch evidence and concise handbook guidance only after behavior/presentation is installed. Follow handbook maintenance and rebuild/review the PDF for installed player-facing changes.
- [x] Verify final hashes, archive entry preservation, absence of probe classes and `git diff --check`. Do not push or modify personal installations unless explicitly requested.

## Planning self-review

- All seven remaining class paths? active/passive rows are assigned to Tasks2?6; icon diagnosis/repair is Task1.
- The five review-focus cases have explicit native/client checks in Tasks1?7. Confirmed missing files are distinguished from unverified rendering/visibility issues.
- There is no stat rebalancing, bespoke asset pipeline, new generic visual framework or implementation-worker dependency.
- The saved native registry snapshot is reusable only while its source hashes match. It is evidence for109 registered effects, not a claim that every status has already been visually inspected in game.
- This document is ready for review. Implementation remains the next step after the user's go signal; no installed JARs were changed during planning.
