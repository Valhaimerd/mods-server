# Combat follow-up: three-phase implementation plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task by task, inline without additional agents.

**Status:** Phases 1 and 2 implemented in repository pack folders on 2 November 2026. Phase 3 remains planned; no commit or push.

## Execution order — three phases

This document is the shared detailed specification. Execute the linked phase plans in order; their scope assignments supersede the original order of sections below. All requested values and clarifications remain applicable.

| Phase | Scope | Plan |
| --- | --- | --- |
| 1 | Adventurer/Warrior node values; all Warrior skill/passive changes; Assassin Mutilate; Berserker/Charge/Assassin attack movement | [Phase 1: class reworks](2026-11-02-combat-followup-phase-1-class-reworks.md) |
| 2 | Purgatory player wall enforcement and changing party membership; multiplayer size-scaled melee reach including Titan's Mark and Warrior passives | [Phase 2: multiplayer movement and reach](2026-11-02-combat-followup-phase-2-multiplayer.md) |
| 3 | Frenzy rapid-click reload investigation/fix; modest speed-aware Archer slight homing | [Phase 3: archery reliability](2026-11-02-combat-followup-phase-3-archery.md) |

Each phase ends with its own focused verification, matching same-filename replacements and required documentation/handbook updates. Start each phase from the actual installed outputs of the previous phase, never an older baseline. Completion of a phase does not authorize the next phase or a GitHub push unless the user requests them.

**Goal:** Make Purgatory's visible enclosure physically block non-party players, including players who leave the caster's party during the channel.

**Architecture:** Keep the existing circular enclosure and add or repair authoritative player movement enforcement. Resolve passage permission from current Better Party membership; never grant permanent passage from membership at cast time. Client collision provides responsive movement, while the server prevents bypasses.

**Tech stack:** Minecraft 26.2, NeoForge, Spell Engine compatibility patch, existing Paladins barrier entity and Better Party integration.

## Requested behavior and interpretation

- The caster and current party members can cross the wall freely.
- Non-party players cannot walk, sprint or jump through the wall in either direction where their bodies intersect its height.
- Former party members immediately lose passage permission on leaving or being removed. They cannot escape through the wall using stale party status.
- This treats the reported ability of former members to leave through the wall as a bug, not an intended exemption.
- Joining the caster's party restores passage while membership remains valid.
- Preserve the existing 12/14-block radius, five blocks below and five above the caster, visible orange surface, and cleanup when Purgatory ends. No floor or roof is added.
- Preserve existing pet, projectile, teleport and spectator policies unless investigation proves a separate change is necessary; this request concerns physical player passage.

## Existing implementation and approaches

`docs/patches/purgatory-continuous-pull/PurgatoryBoundary.java` already clips living-entity motion in `clip(Entity, Vec3)` and exempts `PartyRules.allies(owner, entity)`. Geometry alone passing tests does not prove remote player movement is constrained. The latest packaged implementation also includes subsequent 24-second timing changes; do not rebuild an older source snapshot over those changes.

**Recommended:** Trace and repair the existing collision hook, with server validation of ordinary player movement where necessary. This preserves the enclosure without terrain changes and handles multiplayer authority explicitly.

An actual solid world-block wall would alter terrain and obstruct allies. A client-only collision fix cannot guarantee server enforcement. Neither is suitable.

## Implementation tasks

### 1. Reproduce and identify the bypass

- [ ] Read `mod fixing/README.md`, relevant patch records, and inspect the current active Spell Engine/Paladins JARs and hashes.
- [ ] Inspect `SpatialMovementMixin.java`, `SpatialStateMixin.java`, current `PurgatoryBoundary`, `PartyRules.allies`, and their installed mixin/bytecode hooks.
- [ ] Trace remote-player position handling: determine whether client movement packets bypass the entity-motion clipping path, whether barrier ownership resolves on clients, and whether party membership is cached or incorrectly classified.
- [ ] Reproduce with a real remote player: outsider crossing, party member crossing, then the same member leaving while inside and attempting to exit. Record the actual failing path before selecting a fix.

### 2. Enforce current permission and player collision

- [ ] Reuse one current-membership predicate for wall passage. Avoid snapshotting allies at spawn or granting exemptions based solely on old cached membership.
- [ ] Repair the narrowest existing hook. If remote packet movement bypasses it, validate the swept movement from the last accepted position against active boundaries on the server and correct disallowed crossings using the native position synchronization mechanism.
- [ ] Check swept movement, not only the destination, so a single large step cannot tunnel through the wall. Preserve legal vertical movement and tangential motion where possible.
- [ ] Handle a player overlapping the wall when membership changes without trapping them in repeated corrections: resolve to the nearest valid side based on their last valid position. Do not eject an inside player outside.
- [ ] Keep client collision consistent with server rules; do not rely on the client having timely party information for enforcement.
- [ ] Limit checks to registered active boundaries in the player's level. Remove movement state on disconnect, dimension change and wall cleanup; avoid world-wide scans.

### 3. Focused verification and packaging

- [ ] Verify outsiders are blocked inward and outward with walking, sprinting and jumping; verify legal movement beside the wall remains responsive.
- [ ] Verify join, voluntary leave, kick and party dissolution update permission during the same channel, including an overlapping player and reconnecting former member.
- [ ] Verify two casters with different parties enforce each wall independently, and channel end/death removes restrictions immediately.
- [ ] Verify existing radius/height, friendly pets, projectiles, teleports and spectator behavior remain as documented.
- [ ] Repack only affected active JARs under their existing filenames, preserving all later Ward, retirement and Purgatory changes. Record hashes and focused multiplayer observations.
- [ ] Update the patch record and player documentation if needed; follow handbook maintenance requirements for any player-facing documentation change. Commit or push only when requested.

## Acceptance criteria

A non-party remote player cannot physically cross an active Purgatory wall. A player who was permitted while in the party loses that permission after leaving, without needing a recast or reconnect. Current party members retain free passage, and ending the skill removes the wall and all movement restrictions.

## Additional requested balance changes — implemented in phase 1

- [ ] Adventurer Skills: reduce ordinary melee-damage nodes from **3% to 2% per node**.
- [ ] RPG Skills Warrior path: increase ordinary melee-damage nodes from **4% to 5% per node**.
- [ ] Update the actual attribute modifiers and matching node tooltips together. Preserve named major nodes and other classes' bonuses unless separately requested.
- [ ] Verify all affected ordinary nodes use the new values and synchronize their balance documentation when implemented.

These values supersede the earlier 3% Adventurer / 4% Warrior allocation. This addition records the request only; it does not modify installed skill data.

## Multiplayer melee reach investigation — planned only

**Reported symptom:** Better Combat attacks on multiplayer require approximately one-block proximity to register damage, even when the attacker is enlarged. Equipping Titan's Mark does not appear to extend effective reach. Treat the distance as a player observation until measured; do not assume network delay is the cause.

**Known baseline:** Titan's Mark grants +25% size. The existing `docs/patches/combat-phase-two/ScaleReach.java` multiplies weapon range by Better Combat's configured scale multiplier using `player.getScale()`. `PatchPhaseTwo.java` inserts this into `getEntityInteractionRange_Wrapped_BetterCombat`. Earlier isolated checks passed, but they do not establish that the affected multiplayer server has the matching patched archive or that every remote attack validation path uses this result.

### Investigation checklist

- [ ] Compare actual client and affected server Better Combat, Nameless Trinkets and supporting patch JAR hashes, versions and effective configs. Do not alter the external live server without authorization; request its relevant evidence if unavailable.
- [ ] Inspect the currently packaged hook and follow the complete remote attack path: client target selection, attack packet, server distance/angle/line-of-sight checks, weapon attack range, target bounds, and final damage acceptance. Identify any second reach check that rejects an otherwise valid hit.
- [ ] Measure server and client effective scale, configured scale-to-reach multiplier, weapon range and calculated permitted distance with and without Titan's Mark. Check whether size is applied once, omitted or multiplied twice.
- [ ] Include Warrior Enrage and other size/reach effects, shrinking items, race size and equipped accessory combinations. Audit their cleanup after unequip, passive expiry, death and reconnect for stale or negative reach modifiers. Distinguish class/accessory passives from Better Combat's targeting and validation logic.
- [ ] Reproduce on an isolated dedicated server with a real remote client, a stationary target, and fixed measured distances. Compare ordinary size, Titan's Mark alone, Enrage alone and combined growth using the same weapon; also check shrinking reduces reach proportionally.
- [ ] Log each rejected attack's reason and accepted damage beside measured distance, both effective scales, configured range, ping, TPS/MSPT and synchronized positions. Compare stationary and moving targets to distinguish a deterministic reach cap from latency or tick backlog. Bound diagnostic logging and remove it from production.
- [ ] If the server accepts the hit but health does not change, separately check damage cancellation, invulnerability frames, party protection and cooldown timing; do not label all missing damage as a reach failure.
- [ ] Fix only the demonstrated failing layer. Preserve weapon-specific ranges, existing hit geometry and block reach; avoid an arbitrary global reach increase or another persistent size modifier.

**Acceptance:** With matching client/server builds, enlargement increases the actual damage-registration reach according to the configured Better Combat scale rule; shrinking reduces it. Remote players can land valid hits at that measured reach without needing one-block proximity, and removing size effects restores the baseline without reconnecting. Record any remaining latency-dependent limitations separately.

This is an investigation and future-fix scope, not a claim that the multiplayer issue has been reproduced or repaired.

## Assassin and Warrior rework additions — implemented in phase 1

Requested 2 November 2026. Values below supersede the corresponding installed behavior when implemented. Unmentioned cooldowns, effects and node benefits remain. M means melee power; HP means health points, with 2 HP per heart.

### Assassin: Mutilate

- [ ] Requires an eligible enemy target within **8 blocks**; cast time **0.25s**.
- [ ] Teleport the caster **in front of the target** before resolving Mutilate's existing attack. Preserve its damage, healing and branch effects unless separately changed.
- [ ] Recommended safety: use the target's facing to find a valid adjacent landing point; validate collision, support, dimension, target eligibility and range again at execution. Do not place the caster inside the target, solid terrain or a forbidden boundary. If no safe landing exists, fail cleanly without a remote melee strike. Exact placement offset is an implementation choice to verify against entity sizes.

### Warrior tier I

| Skill/node | Requested change |
| --- | --- |
| Throw Weapon / Throw Net | Projectile speed **1.6**, replacing 1.0. Preserve 0.3s charge and existing damage/range curves. |
| Multiple Nets | Root lasts **2s**, replacing 1.5s; retain its 4s cooldown. |
| Throw Weapon | Applies **Slowness IV for 4s**, replacing Slowness I for 1s. |
| Head Throw | Slowness IV lasts **5s**; stun increases to **0.5s**. Retain its charge-damage upgrade. |
| Strong Throw | Applies **Slowness V for 4s**. Retain two-enemy total hit limit. |
| Endured Vitality | **Exactly +2 flat armor per stack**, replacing both the old +1 armor and 6% M scaling. Retain ten stacks, added-heart healing, duration and interval. |
| Killing Spree | Successful qualifying attacks additionally heal **1 HP**, sharing the existing **0.25s haste-affected interval**. Retain damage stacks and 4% maximum-HP kill healing. |

**Confirmed clarifications:** Vitality has no remaining M-based armor scaling. Killing Spree does not heal separately for every enemy in one attack or bypass its shared interval through multiple targets.

### Warrior tier II

| Skill/node | Requested change |
| --- | --- |
| Demoralizing Shout | Enemies lose **20% melee, ranged and offensive magic power**, replacing 12%. Retain 6s duration and existing radius. |
| Battle Shout | You and eligible party allies gain **12% offensive power**, replacing 8%; retain matching radius and 6s duration. |
| Challenging Shout | **Deals 20% M damage**, replacing 10% M. Mark the caster for the debuff duration. Affected enemies cannot combat roll while the debuff lasts. Retain existing aggro and stun unless separately changed. |
| Wild Charge | Grants **+4 flat armor** for its duration. Negates the movement penalty imposed by melee attacking while active. It does not remove unrelated movement penalties globally. |
| Endurance | Armor becomes **+6 total**, replacing the base +4; retain full knockback resistance. |
| Concussion Blow | Replace next-hit readiness with a **1.5-block proximity tackle** during Wild Charge, stunning eligible enemies for **2s**. Each enemy can be stunned **once per cast**, confirmed by the user. |
| Warm Up | Combat roll heals **5% maximum HP**, replacing 1 HP + rounded 6% M; base cooldown **1s**, haste affected. Treat this as immediate recovery, not a new Regeneration potion effect. |
| Momentum | Each qualifying hit adds **3% attack speed and 3% movement speed**, up to five stacks (**15% each**). Buff lasts **5s**, refreshed. Five prepared hits must be used within **8s**. Retain 12s cooldown and combat-roll trigger. |

Challenging Shout's previous wording “adds 10% M damage” meant that selecting the node gave an otherwise nondamaging Shout a **10% M damage hit**. It was not +10% to another damage value. Use “Deals 20% Melee Damage” in the revised description.

### Warrior tier III

| Skill/node | Requested change |
| --- | --- |
| Last Stand | Each pulse grants **2% damage resistance**, up to **10%** at five pulses. The fifth pulse **fully heals** the caster. Retain maximum-HP growth, healing of newly added HP, Regeneration I and knockback immunity. |
| Juggernaut | Resistance becomes **4% per pulse, 20% total**, replacing the base 2% per pulse rather than adding to it. Retain existing size benefit. This supersedes its old 3%-per-pulse reduction. |
| Sentinel | **2% melee damage per stack**, up to ten stacks (**20%**); **Regeneration II**, refreshed for **5s**. Retain 0.25s haste-affected stack interval and direct-hit eligibility. |
| Enrage | Size becomes **+4% per threshold**, up to **+24%**. Retain +3% melee damage per threshold, up to +18%, and existing HP thresholds. |
| Tenacity | Base cooldown becomes **30s**, replacing 25s. Other effects unchanged. |

Last Stand/Juggernaut resistance must reduce both physical and offensive spell damage, including mage attacks. Use one applicable general incoming-damage modifier; do not double-reduce magic by applying the same benefit again as a separate layer. Preserve intentional true-damage exceptions and distinguish percentage resistance from vanilla Resistance potion levels. Last Stand still does not grant damage immunity.

### Implementation considerations and focused checks

- [ ] Use the existing short-description guidance in `docs/patches/skill-wording-review/RPG Skills - description rewrite.md`: `%` values, clear effects, no formulas/placeholders or repeated cooldown/haste prose. Update real effect details with current received amounts.
- [ ] Validate Slowness levels as IV/V (native amplifiers 3/4), separate duration changes from strength, and preserve target/party protections.
- [ ] Give new armor bonuses owned modifiers with cleanup on expiry, death, dispel and refresh. Vitality heals only newly added maximum HP; refreshing at cap must not heal.
- [ ] Add a dedicated source-owned anti-roll marker for Challenging Shout if existing effects cannot express it. Reject roll activation on client and server; clear on expiry/death/cleanse according to the debuff policy. The caster's self-mark is a reveal, not self-inflicted roll prevention.
- [ ] Identify Better Combat's actual attack-movement penalty and bypass only that penalty during Wild Charge; verify held attack sequences and both base/improved durations.
- [ ] Proximity tackle uses a per-cast set of hit entities, not repeated stuns every tick; check party protection and ensure despawn/death/end clears state. Limit search to local eligible entities.
- [ ] Test Killing Spree's shared hit-heal interval with multi-target and fast attacks; preserve its separately credited kill heal. Qualifying attacks should cause real successful enemy damage, not canceled hits or retaliation feedback.
- [ ] Verify Last Stand at each pulse, final full heal only on the fifth pulse, Juggernaut replacement totals, physical and magical mitigation, Sentinel stacks/Regeneration II, and complete cleanup.
- [ ] Recheck size-scaled multiplayer melee reach with reduced Enrage size as part of the investigation above.
- [ ] Update skill data, runtime hooks, titles/icons/effect tooltips, class references and required handbook artifacts together; preserve unrelated latest patches in same-filename repacks.

### Balance notes

These are user-requested buffs, not yet playtest conclusions. Vitality can grant **+20 flat armor** plus its existing +20 HP, and Killing Spree adds recurring sustain. Combined with Wild Charge armor and Last Stand defenses, armor caps and modifier operations materially affect the outcome. Record effective armor totals instead of assuming displayed bonuses stack without a cap. Slowness IV/V, the proximity stun and Shout's anti-roll debuff substantially increase control pressure; retain each skill's existing cooldowns and avoid repeated tackle stuns or unintended control refresh loops.

No implementation or push is authorized by this planning addition alone.

## Berserker and Assassin movement follow-up — implemented in phase 1

These additions supersede the corresponding earlier Berserker values; unmentioned benefits remain.

| Skill/node | Requested change |
| --- | --- |
| Berserker | While active, remove the movement-speed penalty caused by melee attacking and allow sprinting during melee attacks. |
| Wild Charge | Explicitly allow sprinting during melee attacks, in addition to removing the attack-movement penalty already requested above. |
| Brutality | Each pulse grants **+6 percentage points critical chance and +6 percentage points critical damage**, up to five stacks: **+30 points each**. Replace the previous +4 critical-chance points per pulse; use the native critical-stat units correctly. |
| Lethality | Remove its recurring self-HP cost entirely. Each qualifying healing proc restores **1 HP plus 1 HP per complete 10 Melee Power**. Examples: M below 10 heals 1 HP; M 10–19 heals 2 HP; M 20–29 heals 3 HP. Preserve the existing **0.2s haste-affected healing interval**, shared across targets. |
| Assassin path | Unlocking the Assassin path permanently permits **full-speed sprinting during melee attacks** while that path remains selected, without requiring an active buff. Remove only the melee attack movement penalty and attack-driven sprint cancellation. |

### Integration and checks

- [ ] Trace Better Combat's movement multiplier and sprint-cancellation paths separately: removing attack slowdown alone may still leave sprint disabled. Support both held attack sequences and individual attacks.
- [ ] Apply the movement exception when Berserker or Wild Charge is active, or the Assassin path is selected. Verify the exact Assassin unlock node from the installed tree; do not grant it to every Rogue by assuming class and path are identical.
- [ ] Full speed means the player's otherwise valid movement speed, including charge bonuses and unrelated effects. Preserve roots, stuns, Slowness, terrain restrictions and ordinary sprint prerequisites; the exception is not general movement-control immunity or free sprinting without hunger.
- [ ] Use one shared eligibility rule where practical so overlapping Berserker, Charge and path benefits do not multiply speed. Keep server and client sprint/movement decisions consistent.
- [ ] Remove obsolete Lethality self-damage timers and their fatal-rescue side effects, not merely the displayed cost. Sample current melee power per successful heal without critical-damage scaling or recursive healing procs.
- [ ] Verify Lethality's complete-ten thresholds, shared interval, actual successful enemy melee-hit eligibility, party safeguards, and no remaining self-damage after refresh/reconnect.
- [ ] Verify Brutality's five pulse levels and cleanup with native critical-chance/damage operations. Its critical bonuses last with Berserker and do not survive death or buff removal.
- [ ] Verify movement returns to normal when the active buff ends or the Assassin path is removed, except where another valid movement exception remains active.
- [ ] Use concise descriptions such as “Melee attacks no longer slow you or interrupt sprinting.” Keep Lethality's scaling examples in the technical reference, without exposing floor functions or placeholders in player tooltips.

**Balance note:** Lethality now provides uncapped character-scaled healing with no self-cost; high melee power combined with haste can produce substantial sustain. Preserve the requested scaling and interval, and record effective healing per second during focused verification rather than silently adding a cap.

## Frenzy rapid-click reload desynchronization — investigation planned

**Reported symptom:** Since the Frenzy iframe-bypass change, rapidly clicking can make the crossbow start reloading before its magazine is empty. After the magazine is emptied, it can become impossible to reload. The user corrected the report: this happened in multiplayer Survival. Server delay is suspected, not established as the cause.

**Goal:** Preserve Frenzy's requested damage-iframe bypass while keeping firing, ammunition, magazine counts and reload state consistent between server and client.

- [ ] Identify the affected weapon, game mode, client/server JAR hashes and effective Frenzy/reload configuration. Record whether switching slots, reconnecting or changing game mode clears the stuck state.
- [ ] Trace rapid-click input through use/start/stop, shot acceptance, magazine consumption, reload eligibility/completion and state synchronization. Check duplicate or stale input packets and differences between ammunition-consuming Survival and other modes.
- [ ] Audit the narrow Frenzy iframe-bypass changes for unintended firing/reload interactions. Compare matching builds with and without that hook in an isolated fixture; timing correlation alone does not prove causation.
- [ ] Reproduce with a real remote client using single clicks, held fire and rapid repeated clicks, both at normal latency and controlled delay/tick backlog. Compare Survival with the reported failing mode when identified.
- [ ] Capture bounded traces of authoritative loaded count, client-displayed count, shot/reload state transitions, accepted/rejected inputs, game mode, ping and server tick timing. Determine whether this is a display mismatch, premature server reload, duplicate shot consumption or a reload flag that never clears.
- [ ] Fix the demonstrated state transition: server authority must prevent invalid duplicate transitions and reconcile stale client state. An accepted shot consumes its correct ammunition once; a valid partial magazine must not enter an unintended reload; an empty magazine must remain reloadable when ammunition and normal prerequisites are satisfied.
- [ ] Preserve normal manual reload behavior, game-mode ammunition rules, enchantment compatibility, Frenzy damage/heat values and its projectile-only iframe bypass. Do not solve this by globally removing invulnerability frames or masking magazine counts client-side.
- [ ] Verify reload completion and interruption, empty and partial magazines, rapid input, slot changes and reconnects. Check that damage registration still bypasses ordinary post-hit iframes while armor, explicit immunity and party safeguards remain.

**Acceptance:** Rapid clicks and ordinary network delay do not produce a false nonempty-magazine reload or permanently block reloading an empty Frenzy crossbow. Client and server converge on the same authoritative ammunition and reload state. Record the actual game-mode distinction and root cause before claiming a fix.

## Archer slight homing at higher projectile speed — planned tuning

**Requested behavior:** Slight arrow autocorrection should remain useful with **+50% projectile speed**. Increase steering modestly so a near-miss within the small target box shown in the user's attached image can correct onto the enemy. Preserve the feel of slight assistance rather than strong target-seeking homing.

The image is a qualitative close-aim reference, not a calibrated angular or block-distance measurement. Use the enemy hitbox and measured near-miss trajectories to establish the tuning; do not invent an exact pixel-to-world conversion.

- [ ] Inspect the installed shared Archer arrow-assist implementation, its current turn limit, target acquisition cadence, eligibility and speed multipliers. Establish whether steering is limited per tick, per distance travelled, or by another curve.
- [ ] Compare baseline-speed and 1.5x-speed arrows aimed narrowly off the same target at measured combat distances. Record lateral miss distance relative to the target hitbox and the time available to correct before impact.
- [ ] Recommended starting approach: compensate the existing slight-steering turn budget for actual travel speed relative to its verified baseline, with a bounded multiplier. At 1.5x speed, start by testing about **1.5x the existing per-tick turn allowance**, then adjust only enough to cover the illustrated small near-miss. This is a tuning proposal, not a confirmed final angle or a second 50% projectile-speed buff.
- [ ] Keep acquisition narrow and forward-facing, prioritizing eligible near-crosshair targets. Do not widen it into tracking enemies far off aim, behind the shooter or through walls. Preserve party/pet/Vanish exclusions and existing visibility rules.
- [ ] Apply the correction once through the shared ordinary-arrow assist, including Barrage and Frenzy only where they already qualify. Do not layer a second steering system or alter separate Magic Arrows homing profiles.
- [ ] Preserve projectile speed magnitude while rotating its velocity; avoid unintended acceleration, drag or critical/damage changes. Check high-speed swept collision so missed intersections are not incorrectly diagnosed as weak homing.
- [ ] Verify the pictured close-aim correction with a real client at ordinary and +50% speed, including nearby competing targets and moving enemies. Larger intentional misses should still miss. Check both normal and rapid-fire arrows without increasing target scans unnecessarily.

**Acceptance:** Fast arrows receive visibly useful but modest near-target correction within the illustrated small aim region. Accuracy still depends on aiming; arrows do not make sharp turns or acquire unrelated enemies. Record the final measured turn limits and speed-scaling cap when implemented.

## Phase 2 completion

[Multiplayer wall/reach implementation and evidence](../../patches/combat-followup-phase-two/README.md): 61 dedicated-server/real-client checks and 51 geometry checks passed. The external affected server was not modified or audited. Remaining evidence limits are listed in the patch record. Phase 3 implementation and evidence are recorded in the archery follow-up.

## Phase 3 completion

[Archery implementation and evidence](../../patches/combat-followup-phase-three/README.md): native premature-crank and remote loaded-exhaustion regressions fixed. Multiplayer Survival confirmed as the reported mode. Speed-aware slight steering is installed; phase-one/two behavior is preserved. Evidence limitations are listed in the patch record.
