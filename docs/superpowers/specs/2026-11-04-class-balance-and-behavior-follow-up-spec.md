# Class Balance and Behavior Follow-up - Detailed Specification

> Delivery update: phases 1-3 are implemented and installed; see [the combined execution record](../../patches/class-balance-phases-1-3/README.md). Remaining sections are requirements for later phases, not shipped behavior.

**Consolidated:** 4 November 2026. **Status:** requirements recorded; implementation not started by this planning operation.

**Execution entry point:** [phase index](../plans/2026-11-03-class-balance-and-behavior-follow-up.md). Read only the sections assigned to the phase being executed, plus shared constraints. The phase documents own execution order; the detailed sections below preserve the full requests and checks, including historical baselines and explicit later overrides. Do not execute the old chronological task list as a second work queue.

**Latest Magistrate decision:** **15% Magic Resistance total from Magistrate**, replacing the planned 25%. All other approved values, including Bulwark's 8% full-path general Resistance, remain.

**Precedence:** newest explicit final value beats an older proposal or current-value audit. In particular: mage traps 5s; Frost Splinters 20% P total; Ascension 50% reduction (not immunity), 24% offensive cap and fixed pulses; Deflection direct physical/magic windows; Warrior 4s channels and 40/45/50/55/60s cooldowns. A proposed implementation assumption or unresolved question is not user approval.

---

# Class Balance and Behavior Follow-up Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task. Work inline; do not spawn workers without user authorization. Steps use checkbox (`- [ ]`) syntax for tracking.

**Status:** Requested changes recorded 3 November 2026. Planning only; no gameplay changes implemented by this document.

**Goal:** Revise Rogue pressure and control, large Fire Hydra pursuit/visuals, Paladin defenses and ultimates, Archer attack-block handling, and armor mitigation of ordinary RPG magic.

**Architecture:** Overlay the current installed class and engine archives, including Wizards where required, preserving earlier combat and visual fixes. Use existing impact, status, relationship and combat-roll hooks; keep saved skill IDs stable while changing visible names. Reuse current anti-roll enforcement for Bloody Strikes.

**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90+, Java 25; existing RPG Series, Better Combat and Combat Roll integration; JSON resources and focused Java patches where data cannot express the behavior.

**Spec:** The requested-change table below is the authoritative specification. Unresolved implementation details are explicitly separated from requested values.

## Rogue requested changes

D means the engine?s combined dual-wield power. Cooldowns are base values before haste.

| Skill / node | Requested behavior |
| --- | --- |
| Shock Powder | Reduce cooldown from **14s to 12s**. |
| Slice & Dice | Strengthen its existing successful-hit pull enough that ordinary-speed enemies struggle to escape; speed-boosted enemies should retain an escape advantage. Do not turn it into an unconditional root. |
| Smoke Screen | Increase evasion from **50 to 65 percentage points**. Retain the 5-block cloud, 8s lifetime, 0.5s pulses and short refreshed recipient buff. |
| Explosive Powder | Retain 40% D splash and add **Weakness and Blindness for 3s** to affected enemies. No new effect levels requested. |
| Coated Blades | Retain Poison and additionally prevent the **affected target** from regenerating for **8s**. This is an enemy debuff, not a penalty applied to the Rogue. Preserve current proc conditions unless separately changed. |
| Fracture ? Vital Hit | Rename the visible skill and effect. Armor reduction becomes **1 + floor(D / 13) armor per stack**. A qualifying proc also deals **5% of the target?s maximum HP as physical damage**. |
| Shadowstep | The base skill additionally **roots its targeted enemy for 1s**. Existing arrival disruption, kill reset and upgrades remain. |
| Bear Trap | Increase placed-trap lifetime from **20s to 25s**. |
| Serrated Traps | Increase Bleed duration from **2.5s to 5s**. |
| Extensive Coverage | Reduce upgraded **hit radius from 2.25 to 2 blocks**. Keep upgraded activation radius at **1.5 blocks**. |
| Bloody Strikes | Reduce Bleed from **10s to 5s**; additionally prevent affected enemies from **combat rolling for 5s**. |
| Base Vanish | Allow **sprinting and combat rolling without ending Stealth**. This does not require Shadow Stealth. Existing movement-speed penalties remain unless explicitly revised. |

## Fire Mage: large Ancient Hydra movement

**Requested:** The single large/buffed Hydra should move slowly toward its current enemy, at approximately **1 block per second** on unobstructed level ground. Apply this to the large Ancient Hydra upgrade only; ordinary multi-head Hydra behavior is unchanged.

- Preserve the installed targeting system exactly: target selection, acquisition distance, line-of-sight checks, refresh timing, party/pet exclusions, Vanish rejection and target dropping remain owned by the current target resolver.
- Movement consumes the existing resolved target. Do not add a separate pursuit target, extend the search range or retain an enemy after the targeting system drops it. With no eligible current target, stop pursuit.
- Treat **1 block/second** as the intended observed ground speed, approximately **0.05 blocks/server tick at 20 TPS**, not a literal `movement_speed=1` attribute. Use normal server-tick timing; no wall-clock catch-up or teleporting under lag.
- Preserve the Hydra?s current buffs, model size, damage coefficients, breath timing, mouth-origin aiming, caster ownership and summon lifetime. Movement must not restart attacks or add damage deliveries.
- Prefer existing entity navigation/movement controls. Investigate whether the summon is currently anchored or has zero movement speed before choosing the smallest responsible change. Respect terrain collisions, obstacles, world borders and hostile movement boundaries; do not slide through walls or add teleport pursuit.
- Approach without overlapping or pushing through the victim. Use a collision-safe stopping distance suitable for the large model and its existing breath attack; tune this during verification without changing who it targets.

**Additional visual request:** Enlarge the existing ground pulse/ring on the single large Hydra to match its large body. Reuse its current visual effect and pulse timing; scale only this variant's presentation. Calibrate footprint against the model in a connected client. This does not enlarge targeting or damage range, create extra pulses, or change ordinary Hydra visuals.

### Hydra implementation and verification

- [ ] Inspect the current installed large-Hydra marker, target resolver, movement/navigation restrictions and breath controller. Record exact responsible classes and archive hashes before patching.
- [ ] Add slow pursuit of the existing target, limited to the single large Ancient Hydra variant.
- [ ] On level ground, measure approximately 1 block/second while approaching a sufficiently distant valid target; verify it stops short of contact and retains valid breath delivery while moving.
- [ ] Verify the same enemy is selected before/after the change. Target expiry, leaving range, obstructed line of sight, party membership and Vanish must stop pursuit according to existing target rules.
- [ ] Verify no-target idle behavior, obstacles, slopes, world borders and any applicable hostile movement boundaries. No clipping, remote damage or teleport catch-up.
- [ ] Confirm ordinary Hydras remain unchanged and the large Hydra retains its current damage, size, effects and lifetime.
- [ ] Scale the large Hydra ground-pulse visual to its body; verify alignment during movement, no extra damage/range, and no lingering effect after despawn.
- [ ] Verify final candidate on an isolated server and connected client, then update the Mage raw reference and relevant handbook guidance only after implementation.

## Paladin and shared defense additions - 3 November 2026

These are additional planned changes, not installed values. They supersede the corresponding current values only when implemented.

| Skill / system | Requested behavior |
| --- | --- |
| Vanguard | **Add**, rather than replace existing empowered-hit damage/stun: **15% physical and magic damage reduction for 2s**. Equipping a shield raises this to **25% total**. Activates after the Vanguard stun succeeds; no permanent/base reduction buff. |
| Guardian | **5% base armor and Healing Power**. Taking qualifying combat damage adds **2% per stack**, maximum **five stacks**; equipped shield makes earned stacks **3% each**. Total **15% without shield / 20% with shield**. Retain 5s refreshed duration and existing haste-affected 0.25s gain interval. |
| Divine Protection | Replace individual-hit charge depletion with the shared **0.25s qualifying-damage block windows** below. Base cooldown **20s**, down from 28s. Existing percentage reduction, scaling, duration, branches and block-count calculation remain unless explicitly changed. |
| Divine Oath | Base cooldown **18s**; Improved reduces it to **15s**. |
| Break Restriction | Existing cleanse/controlled-roll behavior additionally grants **knockback immunity for 3s**. |
| Purgatory | Base cooldown **16s**, up from 12s. Full closed dome containment, including bottom/top; hostile targets cannot escape with Ender Pearls or Chorus Fruit. Add **+30% armor for the first 3s** of each channel. |
| Reinforced -> Being of Purelight | Replace threshold-based armor/healing buffs. A fatal incoming hit activates a **full heal**, followed by **1 HP true damage every 0.5s**, bypassing absorption. It continues until the player enters a supported knocked-down/revival state or dies. No timed expiry was requested. |
| Armor against RPG magic | Ordinary RPG spell damage should receive armor mitigation if currently bypassing it. Explicit true-damage/direct-cost exceptions must remain. This is a shared combat-policy change, not a Paladin-only advantage. |

### Vanguard and Guardian integration

- Keep Vanguard's existing 5s haste-affected empowered-hit cooldown, 20% M bonus, and 1s stun / 2s shield stun. Confirmed defense trigger: successfully stunning the enemy with Vanguard grants the caster the 2s defense buff, 15% without a shield and 25% with one. An unsuccessful stun does not grant it; there is no permanent/base reduction bonus.
- Use one general 15% incoming-damage reduction contribution covering ordinary physical and magic damage, not a 15% general layer plus another 15% magic layer. Do not approximate 15% with Resistance I, which supplies 20% by itself.
- A new self buff should have a visible Vanguard icon, 2s timer and actual percentage in Effect Insights. Preserve unrelated Resistance buffs and avoid removing somebody else's effect on expiry.
- Guardian base 5% is constant while selected. Shield affects earned stacks only. Five stacks give 5+5x2=15%, or 5+5x3=20%; do not multiply the base by shield equipment. Equipment changes recalculate existing stack amounts without refreshing their time or generating new stacks.

### Shared attack-block windows: Divine Protection and Archer Deflection

**Installed Archer match:** `skill_tree_rpgs:archer_tier_3_passive_2`, **Deflection**, currently grants three physical-attack blocks for 10s, with a 40s haste-affected cooldown and the existing low-health trigger. Preserve activation and 10s parent duration. The later Archer revision below explicitly changes cooldown to 30s and includes qualifying physical and magic attacks.

1. While an eligible block effect has charges and is within its parent duration, the next qualifying damaging attack consumes **one charge**, cancels that damage before absorption/health loss, and opens a **5-server-tick (0.25s) qualifying-damage immunity window**.
2. More qualifying hits inside those five ticks are prevented **without consuming more charges** and **without extending the window**. Damage-over-time ticks cannot open, refresh or spend a window.
3. At or after the exact window end, the next qualifying hit spends the next charge and opens another five-tick window. Continue until all charges are spent or the original skill duration ends. The final charge still grants its complete window, bounded by the original skill duration.
4. Divine Protection and Deflection cover qualifying attributed direct physical and magic attacks, per the later explicit Archer revision. Ongoing damage, source-less/environmental damage and deliberate health costs retain the exclusions below.
5. Explicitly exclude Poison, Bleed, Wither, ongoing burning, Frostbite, environmental/source-less damage, true/direct HP costs, void and administrator kills. An attacker recorded on a periodic debuff does not make it a qualifying direct hit. Direct Fire/Frost spell impacts are distinct from their lingering damage-over-time effects.
6. Use server time, never wall-clock catch-up. A visible block-count/window detail must reflect real remaining charges; preserve the original duration and avoid natural i-frame side effects preventing a later legitimate activation.
7. Coordinate overlapping charge defenses so one hit does not unnecessarily spend a charge from both. Settle priority using the existing damage pipeline; do not create additive invulnerability durations or double-consumption through reentrant events.
8. Save/reload, death, respec, dispel and skill removal must not leave a free persistent immunity window. No replenishment of charges from merely refreshing a duration unless the skill explicitly grants a fresh activation.

### Purgatory: closed containment and opening armor

- Retain 24s channel, 0.5s damage pulses, stages at 8/16s, damage/scaling, continuous pull, ally exclusions and existing 12/14-block outer radius.
- Replace the current open vertical enclosure with a fully closed dome/volume: roof and underside prevent hostile entities escaping by jumping, flying, falling or digging underneath. Exact shape/height should fit current outer-radius effects and be reviewed visually; do not equate a decorative hemisphere with complete collision containment.
- Enforce containment server-side for entity motion and disallowed escape teleports. Ender Pearl use/teleport and Chorus Fruit use/teleport must be rejected for trapped hostiles, including stale client requests. Preserve caster, party and pet exemptions; prevent escape via teleport endpoint/path edge cases. Do not consume resources for a rejected use without a deliberate policy.
- **Confirmed:** keep late-stage control immunity. Stun/Frozen can interrupt only before 8s; from 8s onward, they cannot release the dome. Killing the caster remains hostile counterplay in later stages. Do not describe stun as a late-stage escape option.
- Preserve manual channel release and mandatory cleanup on death/downing, logout, dimension change, respec and administrative removal; these are lifecycle exits, not hostile escape exploits. Dispel/other control handling must be explicit rather than silently changing it.
- Opening +30% armor starts with channel activation and expires after exactly 3s, or immediately on earlier channel cleanup. It grants no ongoing benefit afterward. Recasts do not stack; no attribute-base writes. Show a short-lived Purgatory icon/details rather than leaving an invisible stale modifier.

### Being of Purelight: fatal-hit conversion and unending cost

- Preserve the passive's allocation/resource ID while renaming visible node/effect to **Being of Purelight**. Retire Reinforced's low-health armor/Healing Power application and clear stale modifiers.
- On a qualifying fatal incoming hit, fully heal the caster. **Pending execution ruling:** cancel/replace the original fatal hit so it cannot immediately erase the full heal. Merely healing before an uncancelled arbitrarily large hit does not guarantee the stated rescue.
- Start one irreversible-by-ordinary-refresh active cost state, first tick after 0.5s, then every 10 ticks: subtract exactly **1 HP from health**, leaving absorption unchanged. This intentionally differs from current absorption-first true-damage costs. Damage ticks do not crit, scale with power/haste/PvP retention, or trigger more copies of this passive.
- Stop on actual death or the installed PlayerRevive/Better Revive knockdown transition. Detect the real downed-state API/event; do not use low health as a substitute. Singleplayer and multiplayer must both be exercised.
- The user did not specify a cooldown/rearm rule, effect of respec/cleansing, interaction with totems/other fatal rescues, or whether logout pauses and resumes the cost. Record these as unresolved; do not invent a cooldown or allow self-cost to repeatedly full-heal the caster forever. Block reentry while already active; persist active state across ordinary saves so reconnecting cannot silently cleanse it.
- Give the state its passive icon, a clear recurring-cost tooltip and visible start cue. Do not grant new damage immunity, armor or Healing Power as an implied part of the rename.

### Verified current armor and Resistance behavior

Archive inspection of the installed `spell_power-neoforge-1.6.1+26.2.jar` finds `data/minecraft/tags/damage_type/bypasses_armor.json` adds `#spell_power:all`. That includes ordinary Arcane, Fire, Frost, Lightning, Soul and generic spell damage. Thus ordinary armor currently does **not** mitigate those sources through Minecraft's armor calculation.

Native Resistance is independent of armor. Its normal reduction is **20% per level**, including ordinary magic, except damage types bypassing Resistance/effects (and custom direct-health/true-damage paths). Spell Power does not add its ordinary damage types to those bypass tags. Therefore Resistance can cover ordinary physical and RPG magic, but is not an exact 15% substitute.

**Planned correction:** allow ordinary RPG spell attacks to use the normal armor/toughness mitigation stage while preserving spell-school resistances, general reductions, penetration rules and final player-source scaling exactly once. Remove only the responsible Spell Power armor-bypass contribution if final pipeline inspection proves that sufficient; do not replace the whole vanilla bypass tag or make fall, drowning, Poison, Bleed, Wither and intentional true damage armor-mitigated by accident. Apply the policy consistently to players and mobs. Native damage tests must prove the resulting behavior before the handbook claims armor protects against RPG magic.

### Added implementation and verification tasks

- [x] Confirm Vanguard 25% reduction with shield, triggered after a successful Vanguard stun with no permanent reduction; preserve Purgatory control immunity from 8s onward.
- [ ] Settle regeneration scope from the Rogue section independently.
- [ ] Record Being of Purelight fatal-hit outcome, rearming and cleanse/respec policies before implementing its state transitions.
- [ ] Add tests for Vanguard's retained damage/stun plus 2s physical/magic reduction; shield/no-shield values, expiry and unrelated-buff preservation.
- [ ] Verify Guardian base, five stack cap, 15/20% totals, shield swap, haste interval, refresh and death/respec cleanup.
- [ ] Implement shared attack-block windows; test burst hits at tick 0/1/4/5/10, final charge, parent expiry, overlapping defenses, direct magic versus lingering Fire/Frost damage, source-less damage and attributed periodic damage.
- [ ] Confirm Divine Protection 20s, Oath 18/15s and Purgatory 16s in native cooldowns and yellow tooltip fields; preserve haste.
- [ ] Test Break Restriction's 3s knockback immunity with ordinary and modded knockback, timer expiry, cleanse and existing controlled-roll rules.
- [ ] Test dome roof/underside, flying/falling/jumping, pearl and Chorus attempts, party exceptions, boundary desync, caster death/stun/release and every cleanup path on a connected client/server.
- [ ] Test Purgatory opening armor at start, just before/at 3s, early release, recast, death and respec; ensure no stack or stale modifier.
- [ ] Exercise Purelight fatal rescue and 1 HP/0.5s absorption-bypassing cost; verify one active loop, no recursive rescue, save/reload and actual singleplayer/multiplayer downed/death stops.
- [ ] Prove armor mitigation for direct Arcane/Fire/Frost and physical damage with/without armor, toughness, penetration and Resistance. Verify intentional true damage and excluded periodic/environmental sources stay correct and no extra reduction layer is applied.
- [ ] Update Paladin/Archer/Mage raw references, effect descriptions/icons and player handbook after implementation; keep this file labeled planned until those checks pass.

## Preserved behavior and exact interpretations

- Vital Hit retains Fracture?s **25% proc chance, 1s haste-affected cooldown, three-stack cap and 6s refreshed armor debuff**. Its new physical damage occurs once per qualifying proc, not per stack and not on each damage-over-time tick. Prevent its own damage from recursively triggering itself.
- Vital Hit?s formula uses complete groups of 13 D: D=12 ? 1 armor/stack; D=13 ? 2; D=26 ? 3; D=39 ? 4. Three stacks at D=39 remove 12 armor. Keep the existing snapshot/refresh rules unless implementation investigation finds they conflict with this formula.
- Target maximum HP means maximum health at the time of the proc, excluding absorption. A 100-max-HP target receives 5 raw physical damage before mitigation. The user has not approved a boss cap or conversion to true damage. Armor, other applicable defenses and the pack?s damage-retention rules must remain operative.
- Coated Blades retains **25% proc chance, 1s haste-affected cooldown, 8s refreshed Poison and its existing D-scaled Poison cap**. The regeneration lock refreshes with successful procs and expires independently through a proper harmful status.
- Weakness and Blindness use **level I** as the minimal default; this is an implementation assumption, not a requested amplifier increase.
- Shadowstep roots the selected enemy only. It does not root every enemy near arrival. No root on failed or invalid teleport; existing line-of-sight, collision, party and boundary checks remain authoritative.
- Trap arming remains **1s**; extend the existing lifetime field to 25s without introducing another arming-time subtraction or extra detonation.
- Bloody Strikes keeps the current Bleed damage: **1 raw HP stationary / 4 raw HP horizontally moving, every 25 ticks (1.25s)**. Its roll lock is a separate timed effect and must not inadvertently stop walking, sprinting or ordinary attacks.
- Vanish retains **7s duration and 8s base cooldown**, its existing movement penalty/upgrades, mark clearing, 0?10% breathing visibility and hostile target-lock protection. Base attacks, other casts, item use and incoming attributed hits keep their existing break rules. Shadow Stealth continues to end on outgoing damage while surviving incoming hits and other actions.

## Details to settle before dependent implementation

1. **Regeneration scope:** ?unable to regen? does not specify whether it blocks only natural regeneration and the Regeneration effect, or also direct spell/item/passive healing. Record the intended scope before implementing that hook. Do not silently suppress all healing or absorption. The 8s target debuff and retained Poison are already settled.
2. **Pull tuning:** the request specifies an outcome, not an impulse. Start from the existing horizontal impulse cap of 0.2 and compare stronger candidates in an isolated client/server fixture. Select a value only after normal-speed versus speed-boosted escape checks. Respect knockback resistance, avoid vertical launch or pulling through walls, and stop short of crossing the caster. Do not add constant between-hit dragging without approval.
3. **Maximum-HP proc risk:** 5% maximum HP is powerful against bosses and high-health players, especially with a haste-shortened cooldown. Implement the requested value with normal mitigation; present any proposed cap separately rather than silently nerfing it.
4. Sprint/roll permission means keeping Vanish active during those actions, not removing its speed penalty. If ?run? instead means full normal movement speed, that is a separate balance decision.

## Global constraints

- Use current installed archives, not historical build snapshots; preserve unrelated user changes.
- Repack same-version JARs under their existing active filenames; back up originals to ignored retired packs.
- Preserve skill/resource IDs, allocations, learned scrolls and saved effects where possible. Fracture?s rename must not revoke existing unlocks.
- Protect party members and player-owned pets at selection and final impact; recheck relationships for delayed effects.
- Keep client/server effect registries synchronized. Missing buff/debuff icons use the corresponding existing skill icon.
- Follow `mod fixing/RPG Skills - description rewrite.md`: brief player-facing descriptions, numeric values in effect details, and native yellow trigger/cooldown/range fields instead of redundant prose.
- No personal installation/world edits. No commit or push unless requested.

## Implementation map

- Create focused `docs/patches/ancient-hydra-pursuit/` build/test evidence for the Hydra change; do not rebuild unrelated mage mechanics.
- Create `docs/patches/rogue-control-rework/` for reproducible build scripts, focused patch sources, tests, archive hashes and verification notes.
- Rogues resources: `data/rogues/spell/{shock_powder,shadow_step,bear_trap,vanish}.json` and English descriptions; inspect runtime delivery helpers before changing control or stealth behavior.
- Skill Tree resources: `data/skill_tree_rpgs/spell/rogue_tier_1_passive_{1,2}.json`; tier-2 Shock Powder modifiers; tier-3 trap modifiers; tier-4 Bloody Strikes modifier; corresponding names, effect details and icons.
- Spell Engine: existing Slice pull handling, Fracture armor snapshot/cleanup, healing-source interception, stealth action interruption and anti-roll validation. Trace installed hooks first; exact class edits belong in the patch manifest after inspection.
- Documentation after implementation: Rogue raw reference, concise patch README, relevant `README.md` / `docs/MODLIST.md` mechanics, rebuilt handbook with required rendering review.

## Review focus

- Fast multi-hit attacks and proc-generated damage must not multiply Vital Hit damage through recursion.
- Expired/dispelled regeneration or roll locks must restore eligibility without leaving persistent modifiers.
- Enemy-to-party changes must prevent delayed hostile effects.
- Sprinting/rolling must preserve base Vanish without accidentally exempting attacks or other casts.
- Trap radii and lifetime changes must preserve one activation and avoid missing airborne contacts.

## Tasks

### 1. Record installed baselines and resource changes

- [ ] Inspect installed JAR metadata, hashes and active hooks; preserve previous fixes.
- [ ] Add focused checks for cooldown 12s, cloud evasion 65 points, trap lifetime 25s, trap hit radius 2, and both Bleed durations 5s.
- [ ] Patch the requested resources and localization; preserve activation radius, trigger chances and unrelated coefficients.
- [ ] Verify baseline fails and candidate passes; inspect archive diff for unrelated entries.

### 2. Implement offensive procs and target debuffs

- [ ] Resolve regeneration-block scope before writing its dependent behavior.
- [ ] Test Coated Blades proc failure/success, refreshed 8s lock, retained Poison, expiry, dispel and each approved healing source.
- [ ] Test Vital Hit at D=12/13/25/26/39, three-stack cap, snapshot refresh and expiry cleanup.
- [ ] Test 5% maximum-HP physical damage against armored and unarmored targets, with absorption present, high-health bosses, party exclusions and PvP retention. Confirm no recursive procs.
- [ ] Add Explosive Powder?s 3s Weakness I and Blindness I; test every eligible splash target and protection exclusions.
- [ ] Reuse synchronized anti-roll enforcement for Bloody Strikes; test both client input and server request rejection, refresh, expiry and dispel.

### 3. Implement movement and stealth interactions

- [ ] Test successful Shadowstep roots only its selected enemy for 1s; failed teleport produces no root or remote impact.
- [ ] Calibrate stronger Slice pull using normal-speed and speed-boosted targets, low/high attack speeds, knockback resistance and walls. Record final impulse and observed escape behavior.
- [ ] Test base Vanish walking, sprinting and repeated rolls without breaking it; retain duration and movement penalties.
- [ ] Test all pre-existing base/Shadow Stealth damage/action break paths, mark cleanup, visibility, target lock and party protections.

### 4. Package, verify and synchronize documentation

- [ ] Run isolated native server checks and connected client/server movement, roll-lock and status-icon checks on final candidates.
- [ ] Install only tested production artifacts in `mods/` and fixed packs under existing filenames; verify hashes match tested builds.
- [ ] Update Rogue reference tables so historical Fracture, 50-point smoke and 10s Bloody Strikes text no longer appears as current behavior.
- [ ] Refresh handbook if player guidance changes; verify links, counts and rendered changed pages.
- [ ] Report implemented versus deferred items and remaining limitations. Do not describe this plan as an installed change.


## Frost Mage, trap and Warrior additions - 4 November 2026

**Status:** Planning only. This section extends the shared class plan; it does not authorize claiming that these values are already installed. Later explicit values here supersede earlier current-value tables for their named mechanics only.

### Shared traps and Warrior projectiles

| Skill | Requested value |
| --- | --- |
| Arcane Trap | Retain placed trap lifetime **5s**. Preserve existing trigger, cooldown, area, damage/debuffs and one-activation policy. |
| Flame Trap | Retain placed trap lifetime **5s**, with other mechanics unchanged. |
| Frost Trap | Retain placed trap lifetime **5s**, with other mechanics unchanged. |
| Warrior Throw Net | Projectile launch speed **1.4**, reduced from 1.6. |
| Warrior Throw Weapon | Projectile launch speed **1.8**, increased from 1.6. |

The earlier requested 10s mage-trap increase is withdrawn: all three mage traps remain 5s; their cooldowns are separate. Rogue Bear Trap retains the separately planned **25s** lifetime. Warrior projectile speed changes do not change charge time, charge damage/range curves, hit counts or upgrade control effects.

### Frost damage status: Frostbite versus Frozen

- Add **5s Frostbite** on successful damaging enemy-facing Frost Mage skill impacts, using Minecraft's powder-snow freezing damage mechanic at maximum freezing rather than starting the slow buildup from zero.
- If that skill successfully applies **Frozen** control to the enemy, Frostbite lasts **10s** instead. Only extend after a successful control application; a failed/rejected Frozen attempt must not silently count as successful.
- Keep the names/mechanics distinct: **Frostbite** is damaging cold exposure; **Frozen** is the existing movement-control effect. Rename the old Frostbite passive to **Near Zero** so its node is not confused with the new general damage status.
- A one-time `ticksFrozen` assignment can thaw early after leaving powder snow. Implement/test a finite 5s/10s timer that keeps the target at the native damage threshold for its duration, then releases only this source's contribution. Do not leave permanent freezing, reset unrelated longer exposure, or add a second damage-over-time loop alongside native freezing damage.
- Preserve native freezing-damage cadence and source, not a new invented damage coefficient. Check species-specific freezing behavior, freeze immunity and protective equipment. The request does not expressly remove these immunity policies.
- Shorter repeated hits should refresh remaining duration without truncating an active longer 10s application. Party safeguards apply on impact and ongoing damage; avoid ticking a newly allied target.
- Scope to confirm before implementation: damaging Frost class skills are required; whether basic Frost weapon spells and owned Elemental attacks share the new class-wide status must be explicitly settled. Do not apply it to unrelated Fire/Arcane attacks merely because the caster selected Frost.

### Frost skill and upgrade changes

P in this section means Frost school power. Cooldowns and effects not explicitly changed remain at their current values.

| Skill / node | Requested new behavior |
| --- | --- |
| Frost Nova: Deep Freeze | Replace the extra Frozen-stack upgrade with **+1s Frozen duration**. Base Nova 2s + Improved 0.5s + Deep Freeze 1s = **3.5s**. This extends Frozen, not a separate Stun effect. |
| Frost Nova: Frost Splinters | Blast damage is **20% P total**, not an additive increase to the previous coefficient. Blast hits apply **Slowness III for 4s**. Preserve existing blast delivery/area/falloff unless separately changed. |
| Frost Spikes | Replace the row with **one spike**, visually **3x the size of one original spike**. Requires an enemy target within **24 blocks**; **0.2s cast**. Telegraph the target location, then erect the spike after **0.25s**, dealing **80% P** within a **2-block radius** and applying **Frozen for 1s**. Retain/reuse native ice visuals. |
| Glacial Ridge | Damage becomes **100% P**; cast is **instant** and spike erection has **no delay**. Retain the new aimed single-spike structure, not the old 20-spike row. |
| Frost Fan | Replace side rows with a second explosion **1s after the spike**, centered on the ice, **3-block radius**, dealing **20% P** and **Frozen for 0.5s**, including the primary target if still in the area. |
| Winter's Chill | Replace critical-vulnerability stacking. Casting a skill readies the **next basic staff spell (right-click spell)** for **10s**; its hit applies **Frozen for 0.5s**. **No cooldown**. |
| Frostbite -> Near Zero | Preserve current trigger/chance/cooldown. Its new effect **overrides a spell's Frozen duration to 5s** rather than adding time to the spell's own duration. |
| Frost Shield | Add **30% P damage** to its nearby-enemy burst and reduce that burst's Frozen from 2s to **1s**. Preserve the existing expiry-burst timing unless explicitly revised. |
| Ice Lance | Full charge becomes **0.75s**. Projectile speed scales from **1.6 to 2.4** across charging. Add **slight homing**, requiring meaningful player aim; no automatic guaranteed lock. |
| Colossal Lance | Remove its **15% critical-chance bonus**. Its projectile gains **30% armor penetration**, ignoring 30% of the struck target's armor for this attack only. This is not a timed armor-reduction debuff and does not remove HP. Retain the existing size increase. |
| Arctic Reflex | Required damage behavior is **existing spell damage x 1.2**: 140 becomes **168**, not 140 + 20. Audit the current coefficient multiplier before changing anything; prior documented behavior already claims multiplication. |
| Blizzard | Damaging hits additionally apply **Frozen for 0.25s**. Each enemy has its own **1.25s interval between freeze applications**; intervening hits still deal ordinary damage. |

### Delivery and integration rulings

- **Frost Spikes dodge window:** snapshot the target's location when scheduling erection; do not track the target throughout the 0.25s warning, which would defeat dodging. Validate the aimed target and legal terrain before scheduling. Hit all eligible enemies in the 2-block area at execution, not only the original target. Do not deal row damage as well.
- Spike visual size and hit radius are separate. A 3x model must still use the explicit 2-block damage radius, not accidentally triple collision/damage range. Inspect native ground-spike placement, lifetime and particle ownership before choosing the runtime seam.
- Frost Fan's second blast is centered on the fixed spike location. Moving targets may escape it. One spike schedules one follow-up blast; do not create an explosion for every victim, recursively trigger Frost Fan from its explosion, or let the old side-row modifier remain active.
- Improved Frost Spikes currently grants +10% spell power. The user has not removed it. Keep that node reward, but resolve how its modifier combines with the requested 80/100% coefficients before implementing; avoid silently changing exact stated final damage by double-applying a bonus.
- Winter's Chill readiness is one nonstacking charge, refreshed by a qualifying skill cast, not a permanent Frozen attribute on every attack. Resolve qualifying cast scope, whether non-Frost base staff spells qualify, and whether the consuming spell may rearm itself. Capture consumption through projectile launch/impact so one basic spell cannot gain indefinite repeated freezes. Existing party and target eligibility remain.
- Near Zero keeps the current **100% damaging-active-Frost trigger and 12s haste-affected cooldown**. Clarify whether its 5s override applies only to the triggering spell/target or creates an ongoing override state; no new duration for such a state was specified. Do not silently make it an always-on global five-second freeze. When it applies, 5s replaces the spell's ordinary Frozen duration rather than adding five seconds.
- Arctic Reflex must multiply the selected spell's damage exactly once, including channel/delayed deliveries and Frost Fan explosions as appropriate to the existing snapshot policy. It must not multiply status duration, healing, spell-power attributes, or summon inheritance. If current native behavior already gives 140 -> 168, preserve it and report verification rather than introducing another 1.2 multiplier.
- Blizzard freeze throttling is independent per target. Proposed shared per-target interval across overlapping Blizzard sources avoids bypassing the 1.25s limit with multiple casters; record that scope before implementing. Frozen control can still end early under existing eligible-direct-hit rules. Do not cancel damage, delay shards or imply an uninterrupted 0.25s freeze if later hits break it.
- Colossal penetration applies only during damage resolution for that projectile: use 70% of target armor in the ordinary armor calculation, retaining toughness and other independent defenses. Do not set a permanent/global caster penetration attribute or alter the victim's armor modifier. Coordinate with the planned armor-against-RPG-magic policy, since penetration cannot provide its intended benefit while the spell bypasses all armor. Audit interactions with existing penetration to avoid applying this 30% twice; do not extend it to unrelated Frost spells.
- Slight Ice Lance homing should reuse a suitable existing gentle-steering policy with a narrow forward acquisition cone, line of sight, party exclusions and Vanish rejection. Exact steering strength remains a calibration task; do not copy Magic Arrows' strong wall-passing seeker behavior. Preserve charge damage/range behavior except the explicitly changed charge time and speed endpoints.

### Frost implementation and verification tasks

- [ ] Inspect current Wizards/Skill Tree/Spell Engine and Warrior projectile resources and runtime overrides. Pin current hashes; reuse current effects and existing naming/description rules.
- [ ] Confirm the three mage trap lifetimes remain 5s without altering their cooldowns; verify expiration, activation-at-boundary and one-use behavior. Confirm Rogue Bear Trap remains separately 25s in this plan.
- [ ] Verify Throw Net speed 1.4 and Throw Weapon speed 1.8 through actual launch vectors; retain charge scaling, upgrades, party protection and own-Weakness recast repair.
- [x] Colossal Lance clarification: projectile-specific **30% armor penetration**, replacing its critical-chance bonus; no timed debuff.
- [ ] Resolve Frostbite/Winter's Chill/Near Zero scope questions before their dependent code.
- [ ] Exercise native full-threshold cold damage on players/mobs for 5s/10s, repeated short/long applications, expiry, immunity/equipment, party changes and target death. Prove Frozen and damaging Frostbite are separate statuses.
- [ ] Verify Nova 2s / Improved 2.5s / Deep Freeze with Improved 3.5s; Splinters 20% P total and Slowness III for 4s on each eligible blast victim.
- [ ] Verify targeted spike validation at 24 blocks, 0.2s cast plus 0.25s erection warning, fixed-location dodging, 3x visual, 2-block hit radius, 80% P and 1s Frozen. No old row impacts.
- [ ] Verify Glacial instant/no-delay behavior and 100% P; verify Frost Fan one delayed blast at 1s, 3-block radius, 20% P and 0.5s Frozen. Test multiple victims, moved targets, geometry and no recursive blasts.
- [ ] Verify Winter's Chill 10s readiness, no cooldown, one-use consumption and refresh; Near Zero 5s override and unchanged proc cooldown; verify their interaction with Frost Fan and Blizzard throttling.
- [ ] Verify Frost Shield's expiry burst deals 30% P and freezes 1s; retain shield immunity, duration, movement penalty and Nimble/Durable branches.
- [ ] Verify Ice Lance charge at 0/partial/full fractions, 0.75s full time, 1.6/2.4 speed endpoints, gentle homing and no party/vanished/through-wall acquisition; test upgraded size, removal of critical chance, and 30% projectile-specific armor penetration. Confirm the target's armor attribute is unchanged and subsequent unrelated hits receive no penetration benefit.
- [ ] Use actual impact tests to prove Arctic Reflex multiplies by 1.2 exactly once, including a 140 -> 168 fixture, delayed impacts and following-cast isolation.
- [ ] Verify Blizzard independent target freeze timers at tick 0/24/25 and with overlapping sources; ordinary damage must continue between freeze opportunities. Preserve existing channel/cooldown and other nodes.
- [ ] Add actual received debuff durations/effects to icons/details, update raw class references, install only final tested matching client/server JARs, and regenerate/review affected handbook pages after implementation.


## Shared Resistance coverage and mage coefficient restoration - 4 November 2026

**Status:** Planned only. These changes complement the planned removal of ordinary RPG magic's armor bypass. Do not claim they are installed or replace active JARs while merely editing this plan.

### Resistance must protect against ordinary physical and magic damage

- Audit **all RPG class skills and passives** whose text says Resistance, damage resistance or damage reduction. When a stated resistance benefit protects ordinary physical attacks but omits ordinary magic, extend that same benefit to magic at the **same percentage**. Example: a stated 10% resistance benefit must reduce either ordinary physical or ordinary magic damage by 10% before other independent layers.
- Native Resistance and existing general incoming-damage modifiers already cover ordinary magic unless a source explicitly bypasses them. **Do not add a second magic reduction layer to these effects.** A single 10% general effect should produce 90 damage from either 100-damage input, not 81 magic damage from duplicated 10% layers.
- Where an effect is truly physical-only, use the shared general-reduction mechanism or an equivalent same-sized magic contribution with clear ownership. Preserve amount, duration, stacks, caps, proc rules, cooldowns, haste and party policy unless this plan explicitly changes them elsewhere.
- Retain explicitly specialized bonuses as specialized: projectile/ranged resistance, fire-only protection, knockback resistance, and named additional magic reduction are not automatically general Resistance. Battle Banner's separately described additional magic layer remains intentional; do not erase it or accidentally duplicate it through this audit.
- Preserve deliberate true-damage/direct-health exceptions, including Purelight's newly requested absorption-bypassing cost. Adding magic coverage must not convert unknown damage into a normal spell attack or make administrative/void damage resistible.
- Show the real received percentage and its physical/magic coverage in status details. Keep skill descriptions brief and accurate; do not call a physical-only implementation general Resistance.

### Restore the previous mage offensive coefficients

**User decision:** revert the additional approximately 20% mage coefficient nerf introduced because armor/weapon enchantments increase spell power. Restore the values **before that specific reduction**, retaining the new armor-mitigation policy.

**Authoritative rollback ledger:** `docs/superpowers/plans/2026-11-01-mage-offensive-damage-scaling.md`, original **Current effective percentage** column, plus pinned original resources/runtime overrides for exact normalization. The reduction was implemented in `docs/patches/combat-phase-one/`. Those files are historical evidence, not replacement whole-JAR baselines.

- Restore Arcane, Fire and Frost class active damage, offensive upgrade overrides, wand/staff basic spells, and owned summon attacks covered by that pass.
- Restore **exact old coefficients**, not `current / 0.8`: the pass rounded values, so dividing rounded values cannot recover them accurately. For example Blizzard returns to **26 2/3% per normalized hit**, not 26.25%; Nova returns to **180%**, not 181.25%.
- Preserve current cooldowns, charge banks, targeting, mobility, visuals, ward changes, summon behavior, party safeguards and all subsequent fixes. Do not replace current JARs with older complete archives.
- Do not modify enchantment power bonuses, current power attributes, or the separately configured global player-source retention layer (default 80% PvE / 35% PvP). This is the specific mage skill-coefficient rollback, not a reversal of all earlier balance changes.
- Preserve multiplicative upgrade benefits such as Arctic Reflex x1.2. Restore the underlying coefficient once, without adding a compensating global multiplier or changing inheritance as a second buff.

| Arcane damage path | Restore effective damage |
| --- | --- |
| Missiles, each base missile | **26% P** |
| Explosion ordinary main blast | **260% P** |
| Echoing Blast main blast | **160% P**, keeping the current two banked casts |
| Chain Detonation follow-up | **30% P** |
| Beam, ordinary hit | **20% P** |
| Charged Beam start -> cap | **10% -> 15% P per hit** |
| Barrage emitter bolt | **40% of emitter power** |
| Burst Blink, each end | **50% P** |
| Arcane Bolt / Arcane Blast | **70% / 80% P** |

| Fire damage path | Restore effective damage |
| --- | --- |
| Fire Breath, each hit | **13% P** |
| Flame Slash | **100% P** |
| Meteor main / ground-cloud impact | **150% / 10% P** |
| Explosive Breath follow-up | **30% P** |
| Firestorm, normalized hit | **40% P** |
| Wall of Flames impact | **20% P** |
| Hydra Fireball / Ancient breath delivery | **55% / 24% of own summon power** |
| Scorch / Fireball / Pyroblast | **60% / 80% / 100% P** |

| Frost damage path | Restore effective damage, subject to new explicit rework below |
| --- | --- |
| Frost Nova | **180% P** |
| Ice Lance / Shattering splash | **200% / 50% P** |
| Blizzard normalized hit | **26 2/3% P** |
| Elemental shard / Super Nova | **100% / 180% of own elemental power** |
| Frostbolt / player Frost Shard | **80% / 60% P** |
| Elemental melee, base / each Legion / Colossus | **50% / 40% / 100% of caster Frost power** |

### Precedence with this plan's new Frost rework

- First remove the historical coefficient nerf; then apply the user's newer explicit skill designs. The rollback must not undo the new targeted spike, freeze durations, cold damage, homing or passive reworks.
- New Frost Spikes remains explicitly **80% P**, Glacial **100% P**, and the replacement Frost Fan delayed explosion **20% P**. Do not restore the obsolete side-row Fan attack or its old 25% coefficient.
- Frost Shield's newly added burst remains **30% P**. This is new requested damage, not part of the historical rollback.
- **Frost Splinters confirmed:** final blast damage is **20% P total**, with **Slowness III for 4s**. Neither the old 16% coefficient nor the mage coefficient restoration adds damage on top of this explicit final value.
- Colossal Lance remains projectile-specific **30% armor penetration** with its critical-chance bonus removed. It is meaningful under the planned normal armor mitigation for RPG spells.

### Implementation and verification tasks

- [ ] Inventory RPG class skill/passive Resistance definitions and runtime handlers; record source, actual damage coverage and whether it is already general. Separate native Resistance, custom general reduction, true physical-only reduction and explicit specialized layers.
- [ ] Extend only missing magic coverage; prove a representative 10% effect alone gives 100 -> 90 for both physical and direct magic, and prove already-general effects remain 100 -> 90 rather than 81.
- [ ] Test physical/magic reduction alongside armor, toughness, penetration, vulnerability, Banner's intentional extra magic layer, native Resistance and player-source retention. Preserve layer ordering and avoid duplicate reductions.
- [ ] Compare every reachable mage active/upgrade/weapon/summon damage path against the original ledger and pre-pass resources. Restore exact effective coefficients through current runtime branches, channel normalization, charge ramps and summon-only clones.
- [x] Confirm Frost Splinters final damage: **20% P total**, with Slowness III for 4s; no additive +20 percentage-point increase.
- [ ] Prove ordinary Explosion 260%, Echoing 160%, Nova 180%, Ice Lance 200%, normalized Blizzard 26 2/3%, and representative weapon/summon attacks through native impact tests. Test with enchantments as well as without; no removal of their power contributions.
- [ ] Verify this plan's new explicit Frost coefficients win over historical restoration, Arctic Reflex still multiplies once, summon power inheritance is unchanged and elemental melee is restored once.
- [ ] Run native/client checks against final combined candidates, install under existing filenames only after verification, and synchronize all current-value tables/tooltips/handbook. Keep historical rollback records clearly labeled.


## Adventurer Utilities layout, wording and Fitness - 4 November 2026

**Status:** Plan only. Defense was investigated separately in `docs/patches/adventurer-defense-audit/README.md` and `tree.json`; no Defense balance or graph change is authorized by this Utilities request.

### Confirmed requested presentation

- Remove the chaotic long-distance links from every ordinary Utilities node to its final mastery. Connect **only the immediately preceding layer** to each final node. When two penultimate nodes exist, **both connect to the final node**, following the Combat Archer/Magistrate presentation; do not arbitrarily choose only one.
- Keep final mastery node size **1**, equal to ordinary nodes; replace the current size 1.5. Distinguish final nodes using their unique item icons and existing appropriate frame, not enlargement.
- **Fitness:** `minecraft:glistering_melon_slice` (the glistening melon item).
- **Adaptability:** `minecraft:diamond_boots`.
- **Ingenuity:** `minecraft:writable_book` (Book and Quill).
- Fix the strange LP/control-glyph text and shorten long tooltips. The installed climbing/sneaking node titles contain real embedded newline characters; inspect the client renderer to prove how they appear. Prefer compact single-line titles and valid description components instead of control characters inside titles. Do not globally replace every letter sequence LP.
- Suggested ordinary label: **+12% Climb & Sneak Speed**; describe ladder/vine scope briefly in details so it is not mistaken for ordinary movement speed. Keep all existing numeric rewards intact.

### Fitness buff

- At the existing **three drumsticks or less** trigger, refill **hunger to its maximum** and **saturation to its maximum allowed by that hunger level**, instead of adding four drumsticks without saturation.
- Under ordinary native caps this means food **20/20** and saturation **20/20**. Use the actual supported cap if another installed system changes it; do not write invalid saturation above hunger.
- The full golden outlined drumsticks must result from real synchronized saturation in the installed hunger HUD, not a cosmetic fake refill. Check which installed HUD renders it and ensure the client displays both full hunger and full saturation after activation.
- Preserve the existing **180s cooldown**, low-hunger trigger, mastery requirement, saved cooldown and dead/downed/spectator exclusions. This is not a continuous full-hunger lock or direct health heal.
- Suggested short Fitness text: **At 3 hunger drumsticks or fewer, fully restore hunger and saturation. 3min cooldown.** Adapt wording to the established description style; preserve exact mechanic details in an appropriate detail field.
- Suggested concise Adaptability text: **Halve a damaging fall and resist powder snow for 5s. 20s cooldown.** Ingenuity: **+1 Luck. Beneficial food/drink effects last 35% longer.** These are wording drafts, not new mechanics.

### Root cause and allocation safeguards

- Installed Utilities builder `docs/patches/utilities-paths/build.py` connects all seven ordinary nodes directly to each mastery and sets `required_skills: 7`. That neighbor-count mechanism explains the web. All three masteries currently have seven incoming ordinary links; they are not connected only to the penultimate layer.
- Replace the visual/prerequisite graph coherently. Removing six incoming edges while keeping a native seven-neighbor requirement would make a mastery unobtainable. Preserve the **existing intended complete-path requirement** through supported nonvisual prerequisites or the existing Utilities server/client validation, rather than keeping unwanted visible edges.
- Retain exclusive path entrances, point cost, earned XP, saved allocation IDs, ordinary rewards and current migration semantics. Do not create a new refund/migration merely for icon/text changes. If prerequisite validation needs a change, reconcile existing legitimate allocations without inventing free mastery unlocks.
- Final nodes must not bypass intermediate nodes, create cross-path shortcuts or accept a forged client unlock. A visual two-node merge is not itself proof that both predecessors are required; test the intended full-path condition independently.

### Implementation and verification tasks

- [ ] Inspect current `skills.json`, `definitions.json`, `connections.json`, Utilities contract/runtime validator and client unlock UI; pin installed hashes and expected node allocations.
- [ ] Lay out each branch without crisscrossing mastery links; connect all nodes in its penultimate layer to its final node and remove earlier-to-final edges.
- [ ] Replace seven-visible-neighbor gating with a valid equivalent complete-path requirement. Test legitimate unlock, missing nodes, refund/respec, existing saves, path exclusivity and server rejection of forged unlocks.
- [ ] Set three mastery sizes to 1; use glistering melon slice / diamond boots / writable book icons. Verify icons resolve in the connected client and are not replaced by an existing metadata icon override.
- [ ] Trace the LP/control-glyph display to its source; render concise single-line titles and brief descriptions with no replacement glyphs, escaped-newline text or misleading omitted mechanics.
- [ ] Update Fitness activation to full real hunger/saturation; verify threshold boundary, both values, 180s cooldown persistence, normal depletion afterward and no trigger while downed/dead/spectating.
- [ ] Capture connected-client Utilities overview and all three final-node tooltips; verify normal node size, clean final-layer links and full golden saturation drumsticks after Fitness activation.
- [ ] Install only tested same-filename candidates; update Utilities documentation and handbook only after implementation. Defense inspection remains read-only unless the user requests a Defense change.


## Approved Adventurer Defense redesign, Magistrate and Nullstone - 4 November 2026

**Status:** Approved design, awaiting implementation. This replaces the earlier tentative Defense proposal and supersedes the read-only-only scope restriction for future Defense work. The installed Defense audit remains a record of the old graph, not the new design.

### Shared progression and balance rules

- Three mutually exclusive paths: **Aegis** (reactive burst protection), **Colossus** (health/recovery), **Bulwark** (sustained mitigation). All are available regardless of offensive class; no weapon, shield, armor-type or spell-school requirement.
- Each path has **seven ordinary nodes plus one specialty**, with the same point cost. Defensive root remains a shared entrance with no free attribute reward.
- Five sequential ordinary nodes lead into two penultimate nodes, both visibly connected to the final specialty. Require all seven ordinary allocations using correct server/client prerequisites without webbing earlier nodes directly into the specialty.
- Remove dead ends and unequal branch lengths. Keep the existing node item icons and final specialty icons: Aegis enchanted golden apple, Colossus totem of undying, Bulwark netherite chestplate. Preserve normal size **1** for all final nodes; do not enlarge them.
- Percentages below are specified path contributions. Accumulate a path's listed general Resistance contribution consistently so its promised total is exact; preserve independent defensive layers and do not incorrectly add external reductions. Physical and ordinary magic receive the same general Resistance contribution.
- Healing received is not outgoing Healing Power. Do not retain old spell-school-power bonuses under renamed healing-received nodes. No offensive-power scaling or haste scaling of specialty cooldowns.

### Approved full-path totals

| Permanent path benefit | Aegis | Colossus | Bulwark |
| --- | ---: | ---: | ---: |
| Maximum HP | +4 | +6 | +2 |
| Armor | +2 | +2 | +4 |
| Physical and magic Resistance | 4% | 2% | **8%** |
| Healing received | 4% | 8% | None |
| Knockback resistance | None | None | 20 percentage points |
| Specialty mechanic | Reactive 15% reduction for 3s | Low-health recovery | Always-active contribution to totals above |

**User tweak:** Bulwark is **8%**, replacing the recommendation's 6%. These are full-path totals, not values added again on top of every ordinary node and specialty reward.

### Ordinary-node allocation

The following distribution realizes those approved totals. Keep the existing icons while assigning the new rewards and clear names.

| Position | Aegis | Colossus | Bulwark |
| --- | --- | --- | --- |
| 1 | +2 maximum HP | +2 maximum HP | +2 maximum HP |
| 2 | +1 armor | +2 armor | +1 armor |
| 3 | +2% general Resistance | +2% general Resistance | +2% general Resistance |
| 4 | +1 armor | +2 maximum HP | +1 armor |
| 5 | +2% general Resistance | +4% healing received | +2% general Resistance |
| Penultimate A | +2 maximum HP | +2 maximum HP | +5 points knockback resistance |
| Penultimate B | +4% healing received | +4% healing received | +5 points knockback resistance |
| Specialty permanent rewards | None | None | +2 armor, +4% general Resistance, +10 points knockback resistance |

The node-by-node distribution is an implementation allocation of approved full-path totals; preserve the totals if layout inspection requires reassignment. Old Bulwark has nine ordinary placements; retire/refund redundant placements safely instead of leaving hidden rewards. Keep stable IDs wherever possible and audit former effect registrations so old bonuses cannot survive beside the new ones.

### Aegis specialty

**After taking direct enemy damage, gain 15% general damage reduction for 3s. Fixed 20s cooldown.**

- Replaces the old specialized magic/ranged/speed specialty and the earlier unapproved absorption-shield proposal. No additional shield system.
- Triggering hit still deals damage; no retroactive mitigation, healing, immunity or fatal rescue. Only a surviving eligible player activates it. A fully negated hit, evaded attack or ongoing periodic damage must not trigger it; determine and document how absorption-only damage is classified before implementation.
- Does not stack or continuously refresh from later hits. Keep the 20s cooldown fixed and saved across ordinary lifecycle changes, so respec/reconnect cannot reset it.
- Covers ordinary physical and magic once. Its 15% temporary layer combined with the path's 4% permanent layer gives **18.4% reduction**, before independent defenses, not 19% or two magic copies.
- Show the existing Aegis icon, actual amount and timer. Preserve unrelated damage-reduction effects on expiry.

### Colossus specialty

**After direct enemy damage leaves you below 40% HP, recover 10% maximum HP over 5s. Fixed 30s cooldown.**

- Five pulses, **2% maximum HP each**, normal healing-received modifiers apply. No immediate full heal, immunity, fatal-hit cancellation or separate +4 HP specialty reward; the full branch's permanent HP increase is exactly +6.
- Requires surviving the triggering damage. Cannot repeatedly restart or queue multiple recovery sequences while active. Damage-over-time, self costs and environment do not trigger it.
- Healing prevention applies. A suppressed healing pulse does not extend the five-second recovery or refund the cooldown. Define maximum-HP sampling at activation so temporary changes do not multiply the total mid-recovery.
- Retain a fixed, persistent cooldown across respec/logout/death according to the pack's mastery cooldown policy; no heal ticks while dead/downed. Do not resurrect from a delayed pulse.
- Give the recovery a Colossus icon and remaining-time detail. This complements class recovery rather than creating another death-prevention mechanic.

### Bulwark specialty

Always-active **+2 armor, +4% physical/magic Resistance and +10 points knockback resistance**, while allocated.

With its seven ordinary nodes, the **full path** gives **+2 HP, +4 armor, 8% general Resistance and 20 points knockback resistance**. No proc, shield requirement, healing, immunity or cooldown. The old +5% Resistance / +4% ranged resistance / +2 armor / +20 knockback specialty is replaced, not retained underneath the new rewards.

### Magistrate

Increase **Magistrate's own Magic Resistance reward from 5% to 15%**. This is **15% total from the node**, not +15 points on top of the previous 5%. The earlier planned 25% is superseded by the user's final 15% decision.

Keep +10% Magic Damage, +5% Spell Haste, the existing +10% Healing school reward, current attribute operation, icon, allocation ID and offensive-path exclusivity. Update the visible description to **+15% Magic Resistance**. It remains an explicitly magic-specialized offensive mastery; do not reinterpret it as 15% physical/general Resistance through the separate generic-Resistance coverage audit.

### Remove Fractured Nullstone

Exact target: **`nameless_trinkets:fractured_nullstone`**, currently an item in Nameless Trinkets giving **15% recognized magic-damage reduction**. Remove the item/accessory and its acquisition rather than removing the whole Nameless Trinkets mod or leaving the current magic-reduction behavior active under a hidden listing.

- Retire its item registration/effect handler and references in loot, recipes, trades, creative lists and recipe viewers as required by the installed implementation. Audit external mod references before unregistering the ID.
- Plan safe handling of saved inventory/Curios entries so retirement does not corrupt a player or equipped-slot record. Do not delete unrelated accessories, retain a stale magic-resistance modifier, or silently edit personal worlds.
- Inspect whether its current mitigation is event-driven rather than an attribute; removing only a tooltip or recipe is insufficient. Stop its old equipped effect and avoid crashes from callbacks referencing a retired registry holder.
- Update `docs/patches/accessory-rpg-balance/inventory.md`, current Nameless effects/reference pages and handbook to mark it removed only after installation. Preserve original active JAR bytes in ignored backups.

### Implementation and verification

- [ ] Pin current Adventurer/compatibility/Nameless archives and inspect actual Defense unlock logic, runtime attribute ownership and Nullstone registration/acquisition references.
- [ ] Build equal seven-node branches plus specialty, clean penultimate merges, preserved icons/size, correct exclusivity and server-validated full-path prerequisites.
- [ ] Design one scoped Defense-only allocation migration if needed: preserve category XP/earned points and offensive, Utilities and Companion allocations; refund retired/rebalanced Defense placements fairly and prevent stale reward stacking. Do not reuse the previous global Utilities migration flag.
- [ ] Verify each path's exact permanent HP/armor/Resistance/healing/knockback totals and removal on refund. Confirm healing received does not grant outgoing spell power and physical/magic reduction is not doubled.
- [ ] Exercise Aegis triggering hit, follow-up physical/magic damage, 3s expiry, fixed 20s cooldown, evasion, absorption classification, immunity and periodic-damage exclusions.
- [ ] Exercise Colossus exact 40% boundary, five recovery pulses, healing modifiers/prevention, fixed 30s cooldown, no overlapping activations, max-HP snapshot and death/downed cleanup.
- [ ] Verify Bulwark gives precisely 8% general Resistance full-path; old specialized/ranged bonuses are gone. Test armor interactions with Warrior/Paladin stacks and the new magic armor policy.
- [ ] Verify Magistrate changes only Magic Resistance 5 -> 15%, existing allocated saves refresh correctly, and physical mitigation stays unchanged. Preserve other rewards and icon.
- [ ] Remove Nullstone from all obtainable/displayed sources; test existing saved/equipped entries and no remaining 15% magic mitigation, while other Nameless items remain functional.
- [ ] Test representative Warrior percentage healing, Paladin Guardian/Oath/Protection, Priest healing/shields/Pain Suppression, Rogue evasion/Mutilate/Cheat Death, Archer Deflection/Blood Barrage and Mage Wards/Frost Shield. Avoid granting defensive bonuses from blocked/friendly/self-generated events or triggering recovery on a downed player.
- [ ] Run connected client graph/tooltip/icon checks and native final-artifact checks; install matching same-filename JARs only after verification. Update current Defense audit/reference, accessory inventory, handbook and plan completion status; no GitHub synchronization unless requested.


## Basic weapon spells and Fire Mage adjustments - 4 November 2026

**Status:** Requested plan additions only; no gameplay changes implemented. These explicit final values override the earlier mage coefficient restoration for the named damage paths.

### Mage traps: correction

Arcane Trap, Flame Trap and Frost Trap **remain 5s lifetime**. The planned increase to 10s is cancelled in all earlier rows/checks. Preserve their individual cooldowns. Rogue Bear Trap's separately requested 25s lifetime is not cancelled by this school-trap correction.

### Staff/wand right-click spells

- Fire and Frost projectile-based basic staff/wand spells gain **slight homing** and **1.3x their current projectile launch speed** (30% faster, not +130%). Measure against current installed resolved launch velocity; respect engine defaults when the field is omitted.
- Apply only to the actual weapon right-click spell bindings. Do not multiply class-spell, Hydra, Elemental or Arcane Emitter projectile speed, and do not multiply a shared spell twice if several weapons bind it.
- Preserve existing projectile perks, damage, charge behavior, spell cost, weapon cadence and intended damage restoration. Add gentle steering requiring player aim, with line of sight, narrow forward eligibility, party/pet exclusions and Vanish rejection. Preserve velocity magnitude during steering; no through-wall seeker behavior.
- Arcane and Holy basic staff/wand **targeting range becomes 24 blocks**. This is weapon-spell scope, not a reduction of class-active ranges such as Arcane Missiles/Explosion or Purge Heal.
- **Installed delivery mismatch to resolve:** Arcane Blast is direct-impact, but Arcane Bolt is currently a projectile; Fire Scorch is also direct-impact despite the user's broad Fire/Frost projectile description. Inspect actual held-item bindings before implementation. Record which weapon spells receive range/homing/speed changes, and do not silently convert their delivery type based on a general description. The user has not explicitly requested a new projectile model or conversion of existing direct spells.
- Example for an existing 1.2 launch speed: new speed **1.56**. Existing 1.25 becomes **1.625**. Do not apply these examples to a spell without verifying its baseline.

### Fire changes

| Skill / node | Requested final behavior |
| --- | --- |
| Fire Breath | **14% P per normalized delivery**. Each successful delivery gently pulls affected enemies toward the centerline of the flame fan. Walking-speed escape should be difficult; sprinting should remain a practical escape. |
| Flame Throwing | Preserve +4-block reach (12 total) and extend the same Fire Breath damage/pull behavior correctly through that added range. No separate unchanged far-range damage/pull path. |
| Firestorm / Flame Whirlpool | Retune the existing pull so walking-speed enemies struggle to leave. Reduce its applied slow from Slowness II to **Slowness I for 1s**, refreshed. |
| Hot Impact | Increase proc chance **20% -> 100%**. Preserve **2s stun** and **8s haste-affected cooldown**, trigger scope and party exclusions. |
| Eruption | **30% P explosion centered on the attacker**, replacing the caster-centered blast. Strong **upward knockback**; **8s base cooldown**. |
| Flame Ward | While its absorption remains active, retaliate for **20% P**, gated by a **0.5s interval** so rapid beam hits do not cause retaliation on every delivery. |

### Exact integration rules and calibration

- Fire Breath's 14% is the **final effective per-delivery coefficient**, not a raw JSON coefficient that the channel normalizes again. At 25 full-channel base deliveries, damage is 350% P before crits, branches and defenses. This replaces the historical 10% installed / 13% restored values for the player spell only. Ancient Hydra breath retains its separate planned 24% own-power coefficient.
- Pull toward the fan's centerline, not automatic full-distance teleporting toward the caster. Define a gentle collision-aware horizontal correction per eligible hit; respect knockback resistance, walls and the caster's aim. Do not yank targets behind the caster or apply it to enemies outside the actual hit volume.
- Test Fire Breath walking versus sprinting escape at near/mid/far cone positions, especially the 8-to-12-block Flame Throwing extension. Account for haste increasing delivery frequency: avoid allowing faster channels to turn gentle pull into a hard immobilization.
- Firestorm's existing pull/slow belongs to **Flame Whirlpool**. Preserve that branch requirement unless the user explicitly moves pull to the base skill. Keep base/Raging knockback behavior separate. Calibrate against walking escape with only Slowness I; no invented extra root or immunity bypass.
- Eruption resolves the real hostile attacker through melee/projectile ownership and existing relationship rules, then centers one explosion there. Missing/dead/cross-dimension/protected attackers produce no remote explosion. Keep the existing 5-block area/falloff unless separately changed; **30% P is the configured maximum coefficient**, not a request to remove falloff. Its upward impulse should launch rather than horizontally scatter targets; exact strength requires client/server calibration and must respect applicable knockback defenses.
- Do not let Eruption or Ward retaliation recursively trigger each other or the originating attack. No fake attacker for environmental damage or ongoing periodic effects. Clarify whether periodic attributed damage is eligible before changing current triggers; the requested behavior is attacker-based retaliation, not source-less area damage.
- Flame Ward activation remains direct enemy damage, **12s haste-affected activation cooldown**, **6s shield**, **2 HP per complete 6 Fire power**, no refill on refresh and shared Ward-capacity handling. The new **0.5s retaliation interval is distinct from activation cooldown** and should be fixed, not shortened by haste.
- Use one retaliation interval **per ward recipient across all attackers**; do not let multiple beam sources each obtain a separate 0.5s budget. Only a qualifying hostile hit during an active, unspent Flame Ward can retaliate; extra hits during the gate do not queue later bursts or reset the gate.
- Verify shield ownership instead of treating unrelated remaining absorption as a permanent Flame Ward. At depletion, expiry, dispel, death or respec, retaliation stops. Define whether a hit that consumes the final shield HP can receive one last retaliation using pre-hit shield eligibility; record that boundary explicitly before implementation. Refreshing must not reset the retaliation interval or restore spent HP.

### Verification tasks

- [ ] Assert all three mage traps stay at 5s and prior 10s-plan references are removed; keep their cooldowns and Rogue Bear Trap lifetime separate.
- [ ] Audit actual Fire/Frost/Arcane/Holy weapon item bindings and delivery types. Resolve the Arcane Bolt/Fire Scorch mismatch before changing delivery behavior; test the 24-block target boundary only on intended weapon spells.
- [ ] Measure actual Fire/Frost basic projectile launch speeds before/after (1.3x), gentle homing, speed retention, existing bounce/ricochet/perks, party/Vanish rejection and no duplicate application through shared bindings.
- [ ] Verify Fire Breath actual 14% P hits and full-channel normalization; test Flame Throwing extended range and walking/sprinting escape under different haste and knockback resistance values.
- [ ] Verify Flame Whirlpool Slowness I for 1s and retuned pull; confirm unchanged base Firestorm/Raging behavior without that branch.
- [ ] Verify Hot Impact guaranteed proc when eligible, retained 8s cooldown/haste, 2s stun and no repeated stun during cooldown.
- [ ] Verify Eruption damage and position on a distant attacker, projectile owner, nearby splash victims, upward launch, 8s cooldown, invalid/protected source exclusions and no recursion.
- [ ] Verify Ward 20% P retaliation at tick 0/9/10, a continuous beam, multiple attackers, shield depletion/expiry/dispel and unrelated absorption. No queued procs, no per-attacker bypass and no extra response per beam delivery.
- [ ] Synchronize brief descriptions, effect icons/details, raw class references and handbook after final artifacts pass native/client checks. Keep this section marked planned until installed.


## Arcane Mage follow-up - 4 November 2026

**Status:** Requested changes recorded; plan only. These explicit final values supersede prior current-value tables and the general coefficient-restoration ledger for the named mechanics, without undoing unrelated Arcane fixes.

| Skill / node | Requested behavior |
| --- | --- |
| Fissile Magic | **100% chance**, **30% Arcane P explosion**, **6s base cooldown**. The explosion now **includes the original struck target**, rather than excluding it. |
| Evocation | Remove its increased-damage-received drawback entirely. Keep its critical-chance and haste benefits, duration, stack limits and existing upgrade roles. |
| Evocation channel | **3s**, ten base pulses at **0.3s intervals** before haste. |
| Burst Blink | May be cast while crowd-controlled; removes Slowness and other movement-impairing effects. Preserve normal cast eligibility beyond that control exception. |
| Purge -> Blitz | Replace Speed I with **two banked Burst Blink casts**, like Echoing Blast's charge system. Each blink travels **7.5 blocks**, each departure/arrival explosion has **2.5-block radius** and deals **20% Arcane P**. |
| Presence of Mind | Base cooldown becomes **15s**. Casting **any Arcane class skill** can ready it, instead of only Burst Blink/Evocation. Existing next-spell vulnerability mechanic remains. |
| Arcane Ward | On activation, additionally teleport the caster **4 blocks away from the hostile attacker** responsible for the triggering damage. |

### Fissile Magic

- Keep its existing 5-block area and distance falloff unless separately changed. **30% P is the base/max explosion coefficient**, not an extra hit added to the original 40%. Resolve the original target through the normal blast impact path once, with the same eligible-target safeguards as surrounding enemies.
- Apply the 6s haste-affected cooldown globally for that passive/caster, not one budget per victim. A multi-target attack must not generate a guaranteed explosion per enemy while bypassing cooldown.
- Passive-generated Fissile damage must not recursively trigger Fissile Magic or arm Presence of Mind. Preserve base spell damage, target relationships and current event/proc classification.

### Evocation timing and drawback removal

- Ten base pulses occur at 0.3, 0.6, ... 3.0 seconds before haste; do not add an eleventh pulse at time zero. Keep the established channel-haste behavior rather than making a new fixed unscaled timer.
- Remove only Evocation's +10% damage-taken contribution per stack, including stale generated defaults and saved modifiers that would restore the drawback. Do not remove unrelated vulnerability effects such as Warrior Berserker penalties.
- Base remains ten stacks, lasting 10s; Improved retains its cooldown reduction, Lasting retains 15s duration. Rapid Evocation retains twelve pulses/stacks. With a 3s Rapid channel its interval is 0.25s before haste; this is a derived implementation choice, not a separate increase to channel duration.
- Preserve proportional cooldown behavior and current full-channel 35s / Improved 30s base cooldowns. No extra damage-received penalty remains at ten or twelve stacks.

### Burst Blink control escape

- Bypass cast rejection specifically for stun, Frozen, roots, Slowness and recognized movement impairments when executing this skill, and cleanse those impairments through the established shared control-removal policy.
- Do not clear unrelated damage-over-time, marks or harmful debuffs under a blanket cleanse. No additional post-cleanse immunity duration was requested.
- Keep cooldown/charge availability, doctrine/equipment eligibility, death/downed/spectator restrictions, collision, world border, party policy and hostile Purgatory boundary checks. Casting while controlled is not permission to teleport through a forbidden boundary.
- A failed/unavailable cast must not cleanse control or spend/create a free charge. Establish cleanse/teleport ordering so immobility cannot block a valid escape while invalid casts remain side-effect free.

### Blitz: two shorter blinks

- Keep the existing Purge node allocation/resource ID, rename its visible node/effect to **Blitz**, and remove its old Speed I application. Preserve exclusivity with Slipstream; do not grant both branches.
- **7.5 blocks, 2.5-block explosions and 20% P per departure/arrival blast are explicit final values** for this branch. They are not halved a second time by the mage-damage restoration. Normal Burst Blink outside Blitz retains its own planned restored damage and range.
- The 7.5-block branch range overrides the normal Improved Blink range for this branch; do not add Improved's +5 on top and produce 12.5 blocks. Preserve the required Improved node allocation and cost.
- Reuse Echoing Blast's proven two-charge behavior: one charge regenerates at a time; spending the second does not reset the first recovery; no impact refunds. Use the skill's existing haste-adjusted 12s cooldown as the per-charge recovery unless separately changed.
- Reuse saved/native charge records, separate HUD charge count and recovery bar, death/respawn synchronization, node-removal handling and ordinary pending-cooldown protections. Repeated item swaps or reselecting the node must not grant free charges.
- Each successful charge produces its normal departure and arrival effects once at the reduced radius/damage. Preserve the fixed arrival-impact delay and shared target protection.

### Presence of Mind activation scope and ordering

- Expand the class-spell trigger to all Arcane active class skills: Missiles, Explosion, Beam, Barrage, Burst Blink and Evocation. Passive damage, summon AI casts and the readiness effect itself do not arm it.
- Whether Arcane staff/wand basic spells should also arm it is not explicitly settled by the phrase "any Arcane skills"; record that scope before dependent implementation. Do not let generic PASSIVE or summon spells arm it via a broad school-only match.
- Retain 5s readiness and the next Arcane spell's nonstacking **20% Arcane damage vulnerability for 6s**. It remains a vulnerability effect, not instant casting or a raw coefficient increase.
- Preserve cast-start consumption and delayed-impact snapshots. A spell must not consume readiness it just created on the same cast. Existing readiness can empower a cast, while a new activation can prepare the following cast only under the 15s haste-affected cooldown. No per-channel-delivery rearming.

### Arcane Ward displacement

- Treat the requested teleport as **additional to its current shield**, not replacement: 2 absorption HP per complete 6 Arcane power, 6s duration, 12s haste-affected activation cooldown and no-refill/shared-capacity policy remain.
- Teleport once per successful Ward activation, **not once per absorbed hit**. Resolve the real attacker, including projectile owner, through current hostile relationship checks.
- Move a further four blocks horizontally away from that attacker from the caster's current position; do not set their final separation to four blocks or teleport toward a distant attacker. Use a well-defined fallback when both occupy the same horizontal location.
- Validate supported landing, clear space, world border, collision and hostile movement boundaries. Do not teleport through walls, into hazards, across dimensions or out of a hostile Purgatory dome. If no valid landing exists, keep the shield behavior without an unsafe forced teleport.
- Source-less/self/environmental/ongoing damage remains excluded by the Ward's current activation policy. Protected allies and invalid/dead/cross-dimension attackers do not cause displacement.
- Coordinate with active channels: no duplicate delayed impact, stuck hotbar, channel state or movement lock after the reactive teleport. Verify established teleport-interruption rules rather than letting the Ward resume a cancelled channel silently.

### Implementation and verification tasks

- [ ] Verify Fissile 100% chance, original-target inclusion exactly once, 30% P coefficient, retained area/falloff, 6s cooldown/haste and no recursive or multi-target cooldown bypass.
- [ ] Verify Evocation ten pulses over 3s, Rapid twelve over 3s, correct haste scaling, no time-zero extra pulse and unchanged buff stack/duration/cooldown benefits. Prove damage received is unchanged with Evocation alone at all stacks, including stale saved/config states.
- [ ] Test Burst Blink under stun/Frozen/root/Slowness: valid escape cleanses, cooldown/charge failure does not, other debuffs remain, forbidden boundaries stay enforced.
- [ ] Verify Blitz two charges, sequential recovery, native HUD/recharge, 7.5-block range, both 2.5-block blasts at 20% P, no Speed I, no Improved-range double-add, no double coefficient reduction and no free charge from respec/equipment/death/reload.
- [ ] Verify every Arcane class cast can arm Presence, 15s cooldown, no same-cast self-consumption, no passive/summon/channel rearming and correct delayed vulnerability; settle weapon-cast scope separately.
- [ ] Verify Arcane Ward four-block away displacement once per activation, hostile projectile ownership, valid/invalid landing, overlap/shared absorption, zero-power/no-shield edge cases, channel cleanup and hostile dome enforcement.
- [ ] Update brief descriptions, icons/status details, raw class references and handbook after final installed-candidate native/client checks. Record changed archive hashes and preserve unrelated earlier patches.


## Arcane Emitter and Priest follow-up - 4 November 2026

**Status:** Plan additions only; not implemented. Preserve unrelated Priest tuning, party targeting, no-refill absorption rules and current visual fixes.

| Skill / mechanic | Requested final behavior |
| --- | --- |
| Arcane Emitter bolts | Launch at **0.8x current original projectile speed** (20% slower). Apply to summon-only emitter bolts, not the player's Arcane Bolt or another summon. |
| Circle of Healing | Base healing becomes **60% P**; Improved becomes **75% P**. |
| Ascension | Its support buffs also apply to the caster. Replace the withdrawn immunity proposal with **50% general damage reduction for 6s**, refreshed per pulse. Four pulses grant **6% offensive power each**, maximum **24%**; fixed pulse timing unaffected by haste. |
| Uplift | Lifted eligible allies receive **50% general damage reduction for 6s**, refreshed per pulse, instead of the withdrawn immunity. Preserve the 3-block ally-lift radius unless explicitly changed. |
| Serenity | Grant its absorption benefits to eligible allies throughout the **8-block support radius**, not just the caster or 3-block Uplift area. |
| Fade | Full invisibility, including equipment, becomes **2s**, instead of 1s. |
| Divine Favor | Counter stun becomes **3s**, instead of 1s, for both the identified attacker and eligible enemies in the 2-block secondary radius. |
| Lightwell | Base cooldown becomes **60s**, instead of 80s. |
| Barrier | Base cooldown becomes **40s**, instead of 60s. |

### Integration details

- Resolve actual installed emitter launch velocity, including default values and runtime clones, then multiply it once by 0.8. Do not undo its planned damage restoration, target tracking, lifetime, emitter count or Attuned Emitters haste. Shared launch definitions must not slow player wand bolts as a side effect.
- Circle's new 60/75% are **final healing coefficients**, not +60/+75 points or a modifier to source Healing Power. Sanctuary's rounded regeneration strength and Mass Dispel remain as configured; do not increase those simply by modifying caster power.
- Ascension grants **+6% melee/ranged/offensive-magic power per pulse**, up to **four stacks / 24%**, to eligible allies within 8 blocks and the caster (retaining the already requested self inclusion). One self application per pulse; no duplicate self-plus-area grant. Keep the existing 6s / Improved 8s offensive buff duration and refresh policy unless separately changed.
- Pulse timing is fixed: **0.5 / 1 / 1.5 / 2s**, four deliveries over **2s**, regardless of haste. Ensure the channel cannot end early through cast haste and drop scheduled deliveries. Cooldown haste remains enabled; this fixed-pulse rule applies only to Ascension.
- Defense is **50% general damage reduction for 6s**, refreshed per pulse, not damage immunity. It covers ordinary melee, ranged and magic once through one general layer, not three overlapping 50% layers. At the final 2s pulse, a full refreshed effect can last until 8s from channel start. Improved levitation does not automatically increase each defensive application beyond 6s.
- Apply the defensive effect to the caster and, through Uplift, actually lifted allied recipients within the established 3-block lift area. Broader offensive/Serenity support remains 8 blocks. Reconcile levitation removal, dispel, death/downing and effect expiry; do not leave an unintended permanent modifier.
- The former all-damage immunity proposal is **cancelled**. Keep deliberate true/direct-health costs and void/admin exceptions under the shared damage policy. Do not implement a hidden immunity alongside the 50% reduction or globally alter unrelated immunity skills.
- Serenity retains **1 + round(10% P) absorption HP per pulse**, four stacks, 6s / Improved 8s refreshed duration and current no-refill handling. Each eligible ally inside 8 blocks tracks their own earned/current shield. Do not gate it on Uplift or require being within 3 blocks. Existing caster shield benefits remain.
- Fade preserves loaded-pursuer aggro clearing without a distance limit, 6s haste-affected cooldown, equipment hiding and existing visibility rules; only concealment duration changes to 2s. No reintroduction of the old 15-block range line.
- Divine Favor retains 15s readiness, 12s haste-affected base cooldown, next-qualifying-damage consumption, targeted-attacker handling and party exclusions; only stun duration changes to 3s. No source-less counter or multiple consumption from one event.
- Lightwell's firing interval, targeting, two-mote timing and upgrades remain unchanged; Barrier keeps 10s / Improved 12s duration, 10-block radius and Sacred Refuge matching lifetime. Cooldown changes remain haste affected.

### Verification tasks

- [ ] Measure emitter bolt speed at 0.8x baseline and confirm player Arcane Bolt/other summons unchanged; retain charge/damage/haste/targeting behavior.
- [ ] Prove Circle base 60% P and Improved 75% P via actual healing; preserve recipient modifiers, Sanctuary and Mass Dispel.
- [ ] Verify Ascension self/allied offensive buffs at 6% per pulse, four-stack/24% cap, fixed 0.5s deliveries unaffected by haste, 6s refreshed 50% reduction for caster/Uplift recipients, exact expiry, dispel and no double self application.
- [ ] Prove Ascension 50% general reduction applies once to ordinary physical/ranged/magic damage, respects intentional direct-cost/void/admin exceptions, and no withdrawn immunity path remains.
- [ ] Verify Serenity recipients at 3/8/just-over-8 blocks with/without Uplift, separate per-recipient stacks, full cap, refreshing spent shields, Improved duration and party/pet policy.
- [ ] Verify Fade full body/armor/held-item invisibility for 2s and unchanged aggro clearing/cooldown; verify Divine Favor 3s primary and secondary stuns, readiness consumption and existing exclusions.
- [ ] Verify Lightwell 60s and Barrier 40s cooldowns before haste, without changing lifetimes, upgrades or pulses; update descriptions, native tooltip fields, raw references and handbook after final native/client checks.


## Shield/magic parity, Multi Roll and Warrior follow-up - 4 November 2026

**Status:** Plan additions only. These later explicit Warrior values supersede earlier current-value references when implemented. All percentages, cooldowns and pulse timing below must be reflected in actual behavior, not descriptions alone.

### Explicit shield and Priest protection coverage

- General skill/passive Resistance and damage reduction must cover ordinary physical **and magic** damage at the stated amount, as already required in the shared coverage audit. Add explicit regression coverage for **Priest Pain Suppression**: below 50/40/30/20% HP, its 6/12/18/24% reduction must apply to ordinary magic as well as physical damage, with no duplicated magic layer.
- Activated blocking shields must reduce **qualifying frontal ordinary magic damage by 80%**, just as physical hits: 100 damage becomes 20 at the shield layer before/after independent layers according to the existing pipeline. Interpret the user's wording as **80% reduction / 20% retained**, not 80% retained.
- Identify and override magic-specific shield-bypass paths where necessary, preserving the installed independent shield multiplier and directional validation. Test direct Arcane/Fire/Frost/Holy/Lightning impacts and projectile-owner direction. Rear hits, source-less environmental/periodic damage and deliberate true/direct-health costs retain their intended exceptions.
- This does not globally turn full-immunity skills such as Priest Barrier or Frost Shield into 80% reduction, and does not add 80% to every Resistance node. Charge-based skill defenses retain their separately planned five-tick windows.

### Multi Roll enchantment

Exact installed resource: `data/combat_roll/enchantment/multi_roll.json` in `combat_roll-neoforge-3.0.1+26.2.jar`, currently max level III and +1 roll per level on head armor.

- Maximum enchantment contribution becomes **two additional rolls**, not three. This is +2 beyond the normal roll count, not a total capacity of two.
- Preserve the enchantment ID, equipment slot and other Combat Roll behavior. Keep normal I/II bonuses at +1/+2; level III or previously saved higher levels must never retain a +3-or-more contribution. Prefer a capped +1/+2/+2 progression if retaining level III avoids unnecessary saved-level migration; document the chosen presentation before implementation.
- Do not cap unrelated class-granted roll capacity or modify Longfooted/Acrobat, roll recharge, distance, readiness triggers, saved cooldowns or the utility path.

### Warrior changes

| Skill / node | Requested final behavior |
| --- | --- |
| Endured Vitality | **+1 armor per stack**, down from +2. Retain +2 maximum HP per stack, ten-stack cap, 8s refreshed duration, 0.25s haste-affected gain interval and healing only newly added HP. No old M-scaled armor. |
| Wild Charge: Endurance | **8 total armor while active**, replacing 6 total. Do not add eight on top of base Charge's four. Full knockback resistance remains. |
| Berserker cooldown | **40/45/50/55/60s** after 1/2/3/4/5 completed pulses, before haste. |
| Last Stand cooldown | Same **40/45/50/55/60s** pulse-dependent schedule. |
| Both ultimate channels | Five pulses, **0.8s apart**, requiring **4s** for full channel before haste. First pulse at 0.8s, fifth at 4s; no pulse at time zero. |
| Base Last Stand regeneration | Remove **Regeneration I** application from base pulses. Do not remove Sentinel's separately granted Regeneration II. |
| Base Last Stand knockback | Full knockback immunity remains active from the first successful pulse for the lifetime of Last Stand. It does not require completing all five pulses or selecting Juggernaut/Sentinel. |
| Base Last Stand healing | Every pulse **additionally heals 20% maximum HP**. Remove the separate fifth-pulse full-heal reward. Retain healing of newly earned maximum-health capacity. |
| Berserker fatal rescue | Rescue stun becomes **8s**, instead of 5s. Preserve full heal, channel/buff termination, 10s Nausea and existing early-skill cooldown handling. |

### Timing, healing and cleanup

- Preserve existing native haste effects on both channels/cooldowns. The stated 0.8s interval / 4s full channel are **before haste**; do not silently make channels fixed-duration. Existing stun/Frozen interruptions remain valid throughout these Warrior channels; roots retain their existing noninterruption policy.
- An interrupted channel retains only earned pulses and the corresponding new cooldown. Cancellation before the first pulse retains the existing no-pulse policy. Do not compute cooldown from the old 2.5s duration or charge a full 60s after only one pulse.
- Last Stand still grants 20% base maximum HP per pulse, up to 100%, and 2% general reduction per pulse, up to 10% (Juggernaut 4% total per pulse, up to 20%). The new heal is **additional to newly added HP healing**, using maximum HP resolved for that pulse after its health-capacity update. Never heal beyond the resulting maximum.
- The fifth pulse gets the same 20%-maximum-HP recovery as other pulses, not an extra full heal. Normal received-healing modifiers and approved healing-prevention rules apply to the new recovery. Do not reintroduce Regeneration I via a shared base-effect helper or stale effect configuration.
- Full knockback protection ends when Last Stand ends, is removed or is cleared on death/respec. It should not give knockback immunity permanently or leak into later channels.
- All unrelated Berserker benefits/drawbacks, durations/upgrades, Speed pulses, Brutality/Lethality and death cleanup remain. This change does not apply Purgatory's late-stage control immunity to Warrior ultimates.

### Verification tasks

- [ ] Prove shield-only 100 -> 20 damage for both direct physical and ordinary magic frontal hits. Test rear/source-less exceptions and combinations with armor/Resistance/absorption without a duplicate shield layer.
- [ ] Prove Pain Suppression 6/12/18/24% reduction for physical and direct magic at each strict threshold, including healing across boundaries and absence of doubled magic mitigation.
- [ ] Verify Multi Roll +1/+2 and maximum +2 from the enchantment at normal/high saved levels; unrelated roll bonuses, recharge, tooltip and existing equipment remain valid.
- [ ] Verify Vitality ten stacks give +10 armor/+20 maximum HP, no M scaling and no capped refresh healing; Endurance grants eight total armor rather than twelve.
- [ ] Verify actual ultimate deliveries at 0.8/1.6/2.4/3.2/4s before haste; interrupt at every boundary, verify earned stacks and 40/45/50/55/60s cooldowns, then repeat with positive/negative haste.
- [ ] Verify Last Stand no base Regeneration I, full first-pulse knockback protection, added 20%-maximum-HP recovery each pulse, no fifth full heal, retained max-HP grant healing and separate Sentinel Regeneration II.
- [ ] Verify Berserker fatal rescue 8s stun with other rescue effects/cooldowns unchanged; clean both ultimate states on death/downing/respec and verify no lingering knockback immunity.
- [ ] Update concise descriptions, yellow cooldown fields, raw Warrior/Archer/enchantment references and handbook after final native/client checks and tested same-filename installation.


## Archer rework and Ascension revision - 4 November 2026

**Status:** Planned only. The Ascension table and detailed rules above have been rewritten to remove the previous immunity proposal. This section supersedes earlier Archer current-value descriptions for the named changes.

### Ascension final revision

- **50% general damage reduction for 6s**, refreshed per pulse, replacing the planned full immunity. Covers ordinary melee, ranged and magic damage through one general layer.
- Allies in the 8-block support area and the already included caster gain **6% offensive melee/ranged/magic power per pulse**, maximum **four stacks / 24%**. Preserve no outgoing Healing Power bonus unless separately requested.
- Fixed **0.5s pulse spacing**, four pulses over 2s, **unaffected by haste**. Native cooldown haste remains.
- Uplift transfers the new reduction instead of immunity to lifted allies. Existing lift radius, offensive buff duration and Serenity rules remain as described in the corrected Priest section.

### Archer requested values

| Skill / node | Requested final behavior |
| --- | --- |
| Entangling Roots | Base cooldown **8s**; cloud lifetime **4.5s**. **Up to two active clouds** per caster (explicitly confirmed), not two slow stacks. Each base pulse prevents affected enemies from rolling for **1s**, refreshed per pulse. |
| Improved Entangling Roots | Cooldown becomes **6s**. |
| Rhythm | **4% ranged attack speed per stack**, five stacks / **20% maximum**, retaining 4s refreshed duration. |
| Concussive Shot | Base cooldown **4s**, retaining guaranteed 1s stun on an eligible arrow hit. |
| Deflection | Base cooldown **30s**. Blocks qualifying **physical or magic direct attacks**, using the same 0.25s block-window mechanism as Divine Protection. Opening a block window additionally grants **1.3x movement speed for 2s**. |
| Magic Arrows | Projectile launch speed **1.3x its current value**. Retune Conjured Arrow's delay before seeking to suit the faster travel. |
| Barrage | Each arrow deals **70% normal shot damage**, replacing 60%. |
| Onslaught (Archer roll passive) | Combat roll readies the next damage dealt; consuming readiness grants **30% ranged attack speed and 15% ranged damage for 4s**. Base cooldown **8s**. Replaces the old roll-granted two Rhythm stacks. |

### Entangling Roots: two-cloud ownership and roll lock

- Preserve current 6-block radius, 60% movement/jump reduction, 0.75s base pulse cadence, placement, vertical volume and party/pet protections. The confirmed two-cloud limit is a concurrent-instance limit, not a two-charge/banked-cast mechanic.
- Existing cooldown/haste still controls casting. Two 4.5s clouds may overlap only when legal cast timing allows it. Do not grant a free second immediate cast merely to make both clouds available.
- Each cloud keeps its own 4.5s expiry; casting again must not prolong an older cloud. Proposed over-limit handling: a legal third cast retires the oldest cloud so at most two survive; verify or confirm this policy before implementation.
- Overlapping clouds must not double the base slow to 120% or multiply roll-lock duration. A pulse refreshes one 1s lock per recipient. Do not add a second global roll cooldown or block unrelated walking/attacks.
- Reuse existing synchronized anti-roll validation on client and server. Preserve Nature's Grasp and Nettle's existing roles; the new roll lock belongs to the base skill and works without either branch.

### Deflection: shared windows and movement

- Retain the existing below-50%-above-1% trigger, three charges and 10s parent effect. Change the cooldown to 30s and extend qualification to direct magic attacks as explicitly requested.
- First qualifying hit spends one charge and opens a five-tick window; qualifying hits within that window are prevented without additional charge consumption or extending it. The next qualifying hit after expiry spends the next charge. Use the same implementation seam and damage classification as Divine Protection.
- Ongoing Poison/Bleed/Wither/burning/Frostbite, unknown/source-less environmental hits, direct HP costs, void/admin sources remain excluded even if a periodic effect records an attacker.
- Each newly opened charge window grants **1.3x movement speed** for 2s, refreshing rather than stacking. A dozen hits inside one window must not produce repeated speed stacks or renew immunity. Effective isolated movement multiplier is **1.30**, not +130% or 2.3x.
- Keep the movement bonus self-only and finite. Preserve unrelated speed effects on expiry; death/respec/dispel cannot leave a permanent modifier. Do not confuse this charge defense with the separate directional 80% blocking-item reduction.

### Magic Arrows and Conjured Arrow timing

- Installed normal Magic Arrows launch speed is 1.5, so the baseline requested new speed is **1.95**. Confirm resolved Conjured/Magic Punch launch values and apply the 1.3x factor once to their inherited speed as well.
- Preserve current damage/upgrades, arrow count, launch spacing, ten-second lifetime, party/Vanish exclusions, wall-passing and homing profiles unless explicitly changed. Do not apply the ordinary-arrow slight-assist cap to these separate magical seekers.
- Conjured currently waits 0.5s before medium seeking. A speed-compensated delay is **0.5 / 1.3 = about 0.385s**, approximately **eight ticks / 0.4s** on a 20-TPS server. This is the proposed calibration starting point; verify the visual upward fan and target acquisition before committing the final tick value.
- Adjust seeking activation timing, not launch spacing or a second homing damage effect. Faster travel should not send Conjured arrows too far past close targets before they can turn.

### Barrage and Onslaught

- Barrage's 70% scales the **normal weapon shot**, not a flat 70% of R. Retain arrow count/Extensive branch, accuracy, iframe policy, current speed and Blood Barrage healing unless separately changed.
- Onslaught's next-damage trigger replaces the two-Rhythm-stack grant. The new buff is **30% ranged attack speed plus 15% ranged damage**, not 30% multiplied into existing Rhythm per-stack values. Retain Rhythm as a separate passive; explicitly test their combination and normal expiry.
- A successful combat roll arms one nonstacking readiness. The next eligible outgoing enemy damage consumes it once and grants the 4s buff. Do not require an arrow if the request says next damage dealt, and prevent passive-generated recursion/multi-target duplicate grants.
- Readiness expiry and whether periodic/summon damage qualifies were not specified; resolve those details before dependent implementation. Default cooldown-start interpretation is the roll activation (existing passive model), not silently deferring it to consumption/expiry. The requested 8s cooldown remains haste affected unless revised.
- Preserve actual received amounts in status details, distinguish Onslaught readiness from the active 4s buff, and use the existing skill artwork. No refresh at every hit after readiness is consumed.

### Verification tasks

- [ ] Verify Ascension 50% reduction replaces all proposed immunity paths, self/Uplift recipient scope, 6s pulse refresh, four 6% offensive stacks/24% cap, and identical pulse times under positive/negative haste.
- [ ] Verify Roots 8s / Improved 6s cooldown, two independent active clouds maximum, 4.5s lifetime, over-limit policy, no doubled slow, refreshed 1s anti-roll and existing branches/party safeguards.
- [ ] Verify Rhythm five 4% stacks/20%, 4s refresh and refund cleanup; Concussive 4s base cooldown with haste and unchanged stun.
- [ ] Verify Deflection direct physical/magic charge windows, tick boundaries, three-charge cap, original trigger/duration, 30s cooldown, 1.3x speed for 2s on window opening, refresh/no stacking and periodic-damage exclusions.
- [ ] Measure Magic Arrows 1.95 baseline speed and inherited branches, Conjured seeking onset and close/far target success; preserve projectile perks, damage, arrow spacing and lifetime.
- [ ] Verify Barrage native shot multiplier 0.70 with different weapons/charge states and all branches; preserve healing/party rules.
- [ ] Resolve Onslaught readiness duration and damage-source scope; test roll -> readiness -> one successful damage -> 30% ranged haste/15% ranged damage for 4s, 8s cooldown, multi-target/periodic cases, Rhythm coexistence, expiry, death and respec.
- [ ] Update concise player descriptions, received-effect details, raw Archer/Priest references and handbook only after final native/client checks and matching same-filename installation. No claim of implementation from this plan update.
