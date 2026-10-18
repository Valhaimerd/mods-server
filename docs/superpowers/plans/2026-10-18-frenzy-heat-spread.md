# Frenzy Heat Spread Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Increase Frenzy projectile spread with the displayed heat stage, add a 26-shot burst/reload limit with a three-second idle reset, and settle whether overheating should bypass damage reductions.

**Architecture:** Extend the existing ProjectileWeaponItem spread mixin to read the firing entity's heat score and select a multiplier. Keep the existing six display thresholds. Track the Frenzy burst count, idle time and reload lock once per player, with a firing guard plus an auto-feed guard. If approved, replace heat self-damage with a dedicated damage type and explicit bypass tags; retain the native damage/death pipeline.

**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90, Java 25+, CrossArms 1.4, Ranged Weapon API 4.0.0, existing Python JAR builder and isolated probes.

**Spec:** The user's 18 October request: 0/1/2/3/4/5 bars use 115/125/140/160/185/220% normal spread; investigate true damage like the void; plan first. Follow-up: reload after the 26th shot, display an empty notice, and reset the shot counter after three seconds without firing. Implementation of spread, the manual-crank burst limit and 30/40/50/60% damage is authorized. A custom overheating damage type remains unapproved and is deferred.

## Global Constraints

- Implementation authorized by the latest user request; publication is outside this follow-up until requested.
- Preserve active same-version filenames and existing skill IDs/data.
- Spread means a multiplier on ordinary crossbow inaccuracy, not miss probability or cone angle.
- Keep shot damage at 30/30/30/40/50/60% for 0/1/2/3/4/5 bars.
- Keep heat self-damage at 0/0/0/1/2/4 HP per 20 ticks.
- Retain first-shot Slowness II for 20 ticks, Infinity behavior, normal durability wear and Capacity removal.
- Test only isolated instances; do not modify personal worlds or the separate desktop server.

## Agreed Spread Table

| Displayed bars | Existing heat score | Normal spread multiplier |
| --- | --- | --- |
| 0 | 0-10, or no score | 1.15x |
| 1 | 11-150 | 1.25x |
| 2 | 151-300 | 1.40x |
| 3 | 301-450 | 1.60x |
| 4 | 451-610 | 1.85x |
| 5 | 611+ | 2.20x |

Read heat at actual firing time from the firing entity, not the nearest player. Treat an absent heat objective/score or a negative score as cold. Cooling and weapon switching must use that same current player-owned score. Existing auto-feeding can add heat before a projectile fires; this plan preserves that behavior and follows the displayed stage rather than guaranteeing that every first shot is stage zero.

## Burst Limit and Idle Reset

Requested behavior: cap a Frenzy burst at 26 actual shots, announce that the crossbow is empty/reloading, and forget a partial burst after three seconds without a shot.

Interpretation stated to the user: the 26th arrow fires successfully, then reloading starts. This is a 26-shot burst, rather than making the 26th trigger pull an empty click after only 25 shots.

- Count successful firing actions, not ticks held, automatic feeds, attempted empty clicks or individual projectiles within one firing action.
- Immediately after shot 26, show `Empty - Reload` and stop automatic feeding. Require a full native crossbow crank; there is no automatic reload timeout. The manual charge uses normal weapon/haste behavior.
- Block further Frenzy firing and auto-feeding until a manual crank succeeds, including a preloaded crossbow in the other hand. Do not consume ammunition, add heat or wear durability on blocked attempts.
- Only a successful native projectile load clears the empty flag and resets the burst count to zero, allowing normal Infinity-aware auto-feeding. Interrupted cranks leave it locked. Do not charge extra arrows as a separate reload fee.
- After 60 consecutive world ticks without a successful Frenzy shot, reset the partial burst count to zero. Every successful shot restarts that idle timer; repeated blocked/empty clicks do not.
- Heat remains separate and cools under its existing rules. The idle reset does not erase heat, increase damage or refill inventory ammunition.
- **Proposed scope:** share the burst count and reload lock across all of a player's Frenzy crossbows and both hands, so swapping cannot bypass the limit. Ordinary crossbows remain unaffected.
- Advance timers once per world tick per player, including when Frenzy is unequipped, rather than once per enchanted item. Clear burst/reload state on death. Persist the exhausted lock across reconnects and server restarts; waiting or reconnecting must not replace the required crank.
- Empty/reload feedback must be integrated with the existing heat actionbar instead of immediately being overwritten by it. On completion, return to the normal heat display.
- This is an internal firing limit; it does not restore the removed Capacity enchantment or replace the existing real ammunition rules.

## Damage Decision: Proposed, Not Yet Approved

The installed 26.2.0.90 game data includes minecraft:out_of_world (void) and minecraft:generic_kill. Both bypass armor, Resistance and vanilla invulnerability; the latter property also affects Creative players and death protection. They are not a universal guarantee against mod interception. Native absorption processing is separate from armor/effect reduction.

Recommendation: add crossarms:frenzy_overheat instead of reusing a void/kill source. Configure no difficulty scaling, no knockback, and bypass armor, Protection enchantments and Resistance using damage-type tags. Exclude it from magic classification so magic-only resistance does not apply; inspect/test the pack's general resistance and trinket handlers as well. Keep Creative/Spectator immunity. Preserve ordinary death handling and absorption unless the user specifically chooses a direct-red-health penalty.

Pending choice: keep current magic self-damage, use the proposed custom unmitigated heat damage with absorption first, or explicitly require bypassing absorption too. Do not implement a damage-type change until this choice is settled.

## Review Focus

- Both hands and modded crossbows must use the firing player's heat and weapon.
- Exact heat boundaries and missing scores must agree with the actionbar stages.
- Ordinary crossbows must retain unchanged spread; original inaccuracy inputs must be multiplied rather than replaced.
- Claimed true damage must be tested against actual pack reductions, absorption and game modes.
- Existing Infinity, damage tiers, slowdown and heat intervals must survive the spread revision.
- Burst counters must count real shots once, reset after exactly 60 idle ticks, and resist swapping/dual-hand timer acceleration.

## Task 1: Heat-Dependent Spread

- [ ] Extend docs/patches/crossarms-firing/tests/FiringProbe.java with all six stages and boundary values 0, 10, 11, 150, 151, 300, 301, 450, 451, 610 and 611; cover a missing score, negative score, another nearby player's heat, both hands, vanilla/Arsenal crossbows, ordinary weapons, and non-1.0 input spread.
- [ ] Run against the current archive and confirm it fails because the hook still returns 0.15x.
- [ ] Update docs/patches/crossarms-firing/src/local/crossarms/mixin/FrenzyAccuracyMixin.java to multiply the incoming inaccuracy by the chosen stage value. Retain the existing Frenzy/crossbow gate and avoid creating scoreboard data for ordinary firing.
- [ ] Rebuild with python docs/patches/bows-balance/build.py --classpath-file tmp/bows-balance/classpath.txt and run the isolated server probe.
- [ ] Add a controlled trajectory sample at 15-20 blocks to inspect widening shot clusters; do not interpret the percentages as guaranteed hit rates. Recheck first-shot damage, heat timer, Infinity pickup/consumption, and slowdown.

## Task 2: Burst Counter, Reload Lock and Feedback

- [ ] Extend FiringProbe.java to verify 25 shots remain usable, shot 26 fires exactly once, the next attempted shot is blocked without ammunition/durability/heat changes, interrupted cranks remain locked, waiting beyond 60 ticks never clears exhaustion, and a full normal crank restores firing with the counter reset. Cover Infinity and non-Infinity ammunition.
- [ ] Add idle-boundary tests: a partial burst remains at 59 idle ticks and resets at 60; firing before tick 60 restarts the timer; blocked attempts do not reset it. Verify timers advance once per world tick with both hands equipped, including after unequipping.
- [ ] Test swapping weapons/hands and a preloaded second crossbow during the lock, two players with independent counters, ordinary crossbows, empty inventory, weapon breakage and death/reconnect cleanup.
- [ ] Inspect CrossbowItem.use/shoot and the current auto-feed order to place the firing guard before native loaded-ammo clearing or durability changes. Count one successful firing action only; avoid counting spawned arrows independently.
- [ ] Add minimal burst state and a once-per-world-tick timer/reset path. Update the load/reset/tick functions as needed and block Frenzy reload.mcfunction while locked. If adding a mixin, register it in the canonical builder's emitted crossarms-pack.mixins.json.
- [ ] Integrate localized empty/reloading text with heat/tittle.mcfunction (or its replacement) so the heat actionbar does not overwrite the message. Verify the notice is actually visible on the isolated client and disappears after reload.
- [ ] Rebuild and run both burst and existing Frenzy regression checks; record normal-crank behavior and the counter scope in player documentation.

## Task 3: Damage Policy, Only After the User Chooses It

- [ ] Add failing tests for the selected reduction and absorption policy at bars 3/4/5: exactly 1/2/4 HP per 20 ticks, alongside armor, Protection, Resistance, magic resistance, general resistance/trinkets, and Creative/Spectator behavior.
- [ ] If using the recommended custom type, add overrides/data/crossarms/damage_type/frenzy_overheat.json and append-only minecraft damage_type tags for bypasses_armor, bypasses_effects, bypasses_resistance, bypasses_enchantments and no_knockback. Do not replace vanilla tag entries or add bypasses_invulnerability. Add a clear localized overheat death message.
- [ ] Change only the heat/damage.mcfunction source type; retain amounts and timer. If pack handlers still reduce the custom type, identify the exact handler and add the narrowest source-specific exemption, with a regression test. Do not claim universal true damage from tags alone.
- [ ] If bypassing absorption is chosen, plan and test a source-specific native-pipeline adjustment before implementation; do not silently replace damage with a direct setHealth subtraction.
- [ ] Verify actual final health/absorption changes and ordinary unrelated damage, then repeat the focused Frenzy regressions.

## Task 4: Package, Document and Verify

- [ ] Extend docs/patches/bows-balance/verify.py for the finalized resources and policy.
- [ ] Replace only mods/crossarms-1.4.jar and its ignored fixed-pack copy after checks pass; update docs/patches/bows-balance/artifacts.json.
- [ ] Synchronize README.md, docs/MODLIST.md and docs/patches/crossarms-firing/README.md with the spread table and final damage policy. Follow docs/HANDBOOK_MAINTENANCE.md and rebuild/visually inspect modpack-player-handbook.pdf.
- [ ] Run structural checks, isolated server/client checks, handbook artifact checks and git diff --check. Record measured results and limits.
- [ ] Commit/push only when authorized for the implementation; no publication during this planning turn.

## Evidence

- Six heat ranges: docs/patches/crossarms-firing/overrides/data/crossarms/function/enchantment/frenzy/heat/tittle.mcfunction.
- Current spread hook and native damage data/code inspected locally from minecraft-server-patched-26.2.0.90.jar.
- NeoForge damage type documentation: https://docs.neoforged.net/docs/resources/server/damagetypes/
