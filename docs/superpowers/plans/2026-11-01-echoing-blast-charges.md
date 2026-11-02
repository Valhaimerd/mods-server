# Echoing Blast Charges Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan inline after implementation is requested. No delegation is needed.

**Status:** Phase 1 implemented and installed on 2 November 2026; Phase 2 spatial changes remain pending. [Completion and verification](../../patches/combat-phase-one/README.md) are authoritative; the detailed design below is retained for provenance.

**Execution split:** Use the [two-phase index and next-chat handoff](2026-11-01-combat-rework-two-phase-handoff.md) to determine batch boundaries. Phase 1 covers balance, charges and Frenzy impacts; Phase 2 covers reach, targeting, homing and Purgatory containment. This file remains the detailed specification, not a completion record.

**Goal:** Replace Echoing Blast's repeatable on-hit cooldown reset with two banked casts, restoring one charge at a time.

**Architecture:** Keep the existing spell/node IDs, native casting and impact paths. Add narrowly scoped, server-authoritative charge handling for Arcane Explosion when Echoing Blast is selected, with client charge/recharge display and persisted recovery state. Ordinary Arcane Explosion remains a normal cooldown spell.

**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90+, Java 25; Wizards 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5.

**Subsequent damage pass:** [Mage offensive scaling](2026-11-01-mage-offensive-damage-scaling.md) applies approximately ×0.8 to active skills, weapon spells and summon attacks, excluding the separate school passive nodes. Its rounded targets supersede earlier damage values here: ordinary Explosion is now planned at **210%**, Echoing at **130%**. This damage pass is installed in Phase 1.

## Confirmed design

| Property | Ordinary / Chain Detonation | Echoing Blast |
| --- | --- | --- |
| Cast time | 2s | 2s |
| Main explosion damage after the planned mage damage pass | 210% Arcane Power | 130% Arcane Power |
| Base cooldown / charge recovery | **12s**, increased from 10s | **One charge every 12s** |
| Maximum banked uses | Normal single cooldown | **2 charges** |
| Recovery | Normal cooldown | Sequential, not parallel; empty to full takes **24s before haste** |
| Haste | Reduces cooldown | Reduces each charge's recovery time |
| Hit-based resets | None | **Removed completely** |

Improved Arcane Explosion still increases the radius from 7 to 9 blocks. Chain Detonation remains the alternative final node, with its existing delayed secondary blast. Arcane Explosion targeting is now 40 blocks as specified below; cast time, rune/durability costs, damage rules and party protection remain. This is a burst-capacity rework, not an instant double-cast: the second use still requires its own normal cast.

## Additional confirmed spell and passive adjustments

These requests are included in the installed Phase 1 implementation. The three Ward changes apply across Arcane, Fire and Frost, not only the Arcane tree.

| Skill | Planned change | Retained behavior |
| --- | --- | --- |
| Arcane Missiles | Base cooldown **12s**, reduced from 16s. **No proportional cooldown:** releasing early still incurs the full haste-adjusted cooldown. Targeting range **40 blocks**, increased from 32. | 4s channel, 12 base missiles and upgrade routes; damage follows the linked mage pass |
| Arcane Explosion | Targeting range **40 blocks**, reduced from 64. Base cooldown **12s**, including the ordinary/Chain version and Echoing's recharge interval. | 7-block splash / 9 with Improved; existing damage branches |
| Arcane Trap | Base cooldown **4s**, replacing zero. Applies **Weakness I**, reduced from II. | Combat-roll trigger; 5s trap, 1.5-block radius, 40% Arcane Power damage and 6s Weakness duration |
| Flame Trap | Base cooldown **4s**, affected by haste. | Existing trigger, damage, burning, radius and lifetime |
| Frost Trap | Base cooldown **4s**, affected by haste. | Existing trigger, damage, debuffs, radius and lifetime |
| Phase Shift | Base cooldown **4s**, replacing zero. | Combat-roll trigger and 0.3s damage immunity |
| Arcane Ward | **1 absorption heart (2 HP) per 8 Arcane Power**; base cooldown **15s**. | Existing trigger and 8s shield duration |
| Flame Ward | **1 absorption heart (2 HP) per 8 Fire Power**; base cooldown **15s**. | Existing trigger, 8s shield duration and school-specific secondary effect |
| Frost Ward | **1 absorption heart (2 HP) per 8 Frost Power**; base cooldown **15s**. | Existing trigger, 8s shield duration and school-specific secondary effect |

All listed cooldowns remain haste-affected. A non-proportional Missiles cooldown means channel completion no longer changes the cooldown; it does not disable haste. The 40-block changes affect targeting, not Explosion's splash radius or the range of separate weapon skills.

**Ward interpretation confirmed by the user:** the 8-power rule controls shield scaling, not an activation requirement or a power expenditure. Use the matching school's power, not combined magic/healing power. Recommended rounding preserves the existing whole-heart approach: each complete group of 8 power grants one heart, without the old extra base heart. Thus 8/16/24/32/40 power grants 1/2/3/4/5 absorption hearts; below 8 grants no shield. This rounding/no-base-heart detail is an implementation recommendation derived from the requested rate, not a separately confirmed numerical instruction. Preserve the pack's absorption safeguards: do not replenish spent absorption through a mere refresh or stack-cap reapplication, and do not add overlapping Wards together.

Suggested brief Ward description: **"Gain an absorption shield for 8s, with 1 heart for every 8 [Arcane/Fire/Frost] Power."** Keep cooldown values in the yellow cooldown detail, following the existing description rules.

## Brainstorming decision

Three interpretations were considered: sequential recovery, independent timers for each spent charge, or a shared timer that refills both. The user selected **sequential recovery**, with 12s rather than the initially suggested 10s. This permits two saved uses while limiting sustained casting to one recovered charge per interval. Independent or full-bank refills would allow substantially more repeated burst output.

Balance implication after the subsequent damage pass: two full-bank Echoing casts deal 260% Arcane Power across two casts, compared with an ordinary explosion's 210% in one cast. Echoing retains its lower per-cast damage and costs two casts/resources for that burst. Haste can still make sustained casting faster; no new minimum recharge or haste cap is proposed.

## Recommended edge rules

These are implementation recommendations, not additional numerical changes requested by the user.

- Begin recovery on the first successful release that spends a charge. A second release while recharging does **not** restart or extend the current timer.
- Example without haste: start at 2; release at time 0 leaves 1 and starts recovery; release again at time 2 leaves 0. One charge returns at time 12, the second at time 24 if unused. Times are measured from completed releases, not cast starts.
- Spending a charge, applying ordinary costs and releasing the spell must form one server-authoritative operation. An interrupted/cancelled cast before release spends no charge. A completed release spends one even if its impacts subsequently miss, are resisted or hit an invulnerable target.
- Area hits and secondary explosions never grant charges or spend extra ones. A charge belongs to one cast, not each entity struck.
- Snapshot the haste-adjusted recovery duration when an interval starts. Swapping haste gear cannot repeatedly restart or instantly complete an interval. When that interval completes, a further missing charge begins the next interval using current haste.
- Persist charges and remaining recovery through logout, restart, death and dimension changes. Swapping hands, weapons, scrolls or node availability cannot create a fresh bank. First-ever activation may start full; reacquisition must reuse existing state.
- Use a dimension-independent server/world-tick timebase. Offline time may advance recovery while the server's world ticks continue; a stopped/paused server does not advance it. Reconcile at most the two missing charges, without an unbounded catch-up loop.
- At full capacity, stop recovery and bank no extra elapsed time. On losing Echoing, ordinary Explosion follows the 12s rule, preserving an outstanding legitimate cooldown. Switching the node must not bypass a cooldown that started before it was selected.
- Preserve externally imposed cooldowns and hosting-item restrictions. Bypass only the normal single-use Explosion cooldown that conflicts with having an available Echoing charge; never clear every cooldown on the item or player.

## UI and descriptions

- Show **2/2**, **1/2** or **0/2** on Arcane Explosion's skill slot while Echoing is active.
- Show progress/time to the next recovered charge even at 1/2, while keeping the skill usable. Only 0/2 blocks casting for charge recovery.
- Sync authoritative state on login, release, recovery and node availability changes. Clients animate the received timer; they cannot award charges.
- Reuse native cooldown/skill-bar presentation where possible. Do not represent a partially available skill as fully locked just to draw a recharge bar.
- Follow `mod fixing/RPG Skills - description rewrite.md`: brief effects, numerical percentages, no formulas or internal terms.

Proposed Echoing Blast node text:

> Arcane Explosion deals 130% Arcane damage and stores up to 2 charges. Charges recover one at a time.

The yellow cooldown/recharge detail should show the 12s base and its normal haste-adjusted value. Avoid repeating cooldown/haste explanations in the gray effect paragraph. Remove the old reset wording from the skill tree, scroll/spell views and reference documents.

## Verified implementation considerations

The installed node `skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1` is a passive triggered by Arcane Explosion damage. Its COOLDOWN action sets Explosion's remaining duration multiplier to zero, with 100% chance and no passive cooldown. That is the existing repeatable reset mechanism, not a two-charge system.

Installed `Spell.Cost.Cooldown` and `SpellCooldownManager` expose ordinary cooldown fields/entries but no native maximum-charge field. Do not add an unsupported JSON property or approximate charges with another on-hit reset.

The existing `RevisionRules.echo()` finds this node through `SpellContainerSource.passiveSpellsOf`. If the node becomes a modifier instead of a passive, update that lookup together with the damage override; otherwise the 160% coefficient can silently revert to 260%. Preserve the ID and allocated-node compatibility regardless of representation.

## Implementation tasks — not executed

- [ ] **Reproduce and test first.** Add an isolated test proving the current node resets after hits and fails two-charge/recovery expectations. Cover multiple victims in one explosion.
- [ ] **Patch spell/node data.** Change Wizards' base `arcane_explosion` cooldown from 10 to 12. Remove the Echoing on-hit reset action/trigger and retain its node identity and branch exclusivity. Apply the later damage pass's 210% ordinary / 130% Echoing targets, replacing the former 260% / 160% values. Test ordinary and Chain Detonation cooldowns separately.
- [ ] **Apply the additional spell/passive changes.** Set Missiles cooldown to 12 with proportional scaling disabled; set both tier-I targeting ranges to 40; set Arcane Trap, Flame Trap, Frost Trap and Phase Shift cooldowns to 4, and Arcane Trap to Weakness I. Update all three Ward scaling implementations and cooldowns to the matching-power rate and 15s. Check runtime helpers as well as JSON so old formulas cannot override the new values.
- [ ] **Implement charge state and casting integration.** Use the existing cooldown/save lifecycle where practical. Keep the logic specific to this spell/node, with no new global entity scans, skill-tree scans every tick or background worker. Test interrupted casts, insufficient resources, consecutive requests, exact recovery boundaries, pending ordinary cooldowns and node changes.
- [ ] **Implement client display and synchronization.** Test usable 1/2, blocked 0/2, recovery countdown, stale/duplicate requests and reconnect. Test haste changes without resetting the running interval.
- [ ] **Verify persistence and combat.** Test release with no successful hit, multi-target impact, Chain Detonation, party protection, death/respawn and reconnect/restart while partially charged. Check that natural recovery alone changes the charge bank and that repeated hits cannot reset it.
- [ ] **Verify the added balance changes.** Compare early/full Missiles channels at identical haste and assert equal cooldowns. Test valid/invalid targets around the 40-block boundary for both spells, with upgraded Explosion radius unchanged. Test all three Traps and Phase Shift at their 4s cooldown boundaries, including haste, and Arcane Trap's Weakness I duration. Check Arcane/Fire/Frost Ward strength at 0/7/8/15/16/40 matching power, unrelated-school power isolation, 15s cooldowns, duration, existing secondary effects and absorption refresh safeguards.
- [ ] **Rebuild and distribute when authorized.** Preserve active filenames for same-version repacks; validate archive differences, metadata, Java 25 bytecode and absence of probe classes. Install final replacements in repository `mods/` and ignored fixed packs; preserve unrelated work and ignored backups. Do not modify personal/live servers without authorization.
- [ ] **Synchronize documentation.** Update the raw mage reference, description-rewrite entry and current magic patch notes. Update the Wizards gameplay guidance in `docs/MODLIST.md` where relevant, rebuild the handbook, verify contents destinations and visually review the changed page. Existing historical patch records may retain their old numbers if clearly marked historical. Push only if requested.

## Acceptance

Two banked Echoing casts work through normal casting. A third cannot release until one charge recovers. Recovery is one charge per haste-adjusted 12s interval, spending a second charge does not restart it, and no hit resets it. Non-Echoing Arcane Explosion has a 12s base cooldown. Missiles uses a non-proportional 12s cooldown, both tier-I skills target within 40 blocks, Arcane/Flame/Frost Trap and Phase Shift have 4s cooldowns, Arcane Trap applies Weakness I, and all three Wards use the new school-power scaling with 15s cooldowns. The UI accurately communicates available casts and recharge. No runtime fix is claimed until those checks pass.

## Additional investigation: character size and melee reach

**Status:** Reported bug and requested behavior; investigation/fix not implemented. Track this separately from mage balance so neither change is mistaken for the other.

### Report and intended behavior

- Another player using Warrior became very large but experienced unusually short melee attack range.
- A trinket was suspected. Removing it did not resolve the problem.
- The reporting user did not reproduce the issue by increasing their own size. The exact Warrior passives, equipment, size sources and client/server state of the affected player remain unknown.
- **Requested behavior:** larger effective character size increases melee reach; smaller effective size reduces melee reach. Reach must follow changes in size and return to the correct value after effects expire or equipment is removed.

### Investigation and implementation work

- [ ] Capture the affected player's effective scale, melee/entity reach, base values and modifier IDs/operations on the server and client, together with their weapon, race, active Warrior effects and accessories. Record actual hit distances, not only animation appearance or model size.
- [ ] Reproduce with Warrior's size-changing passives, particularly Enrage, and compare with a non-Warrior using the same size and weapon. Test Titan's Mark and shrinking accessories individually and in combination. These are suspects, not established causes; persistence after removal alone does not prove that the trinket was responsible.
- [ ] Trace vanilla/NeoForge scale and entity-interaction range, Better Combat weapon reach/target selection and RPG integration. Determine whether the same scale is being applied twice, inverted, overridden or retained as a stale modifier. Check whether the large player's hitbox prevents close-range targeting rather than assuming the numeric reach attribute is small.
- [ ] Define and document one authoritative size-to-melee-reach rule after tracing existing behavior. Preserve the weapon's normal reach at 1x size; larger/smaller size must increase/decrease usable reach respectively. Do not multiply an already scale-adjusted range again. Exact scaling curve and any limits remain to be established from the investigation, not assumed approved.
- [ ] Fix the responsible layer and reconcile stale owned modifiers on login, respawn, equip/unequip, size changes and passive expiry as appropriate. Preserve unrelated weapon/race/node reach bonuses. Do not change block interaction reach or ranged spell targeting as a side effect.
- [ ] Verify real server-approved melee hits at close and maximum distance with ordinary and Better Combat attacks, including held attacks, multiple weapon reaches, Warrior/non-Warrior comparisons and combined size effects. Test growth, shrinking, return to normal, reconnect, death/respawn and removal of the suspected accessory. Ensure visible/client reach agrees with server hit validation.

**Acceptance:** a large Warrior can hit at the expected larger melee distance without losing nearby targets; a smaller character has correspondingly shorter reach. Size/equipment/passive changes leave no stale reach penalty, and 1x size restores the correct weapon-specific baseline.

## Additional request: Vanish prevents skill target lock

**Status:** Planning only; no Vanish or targeting implementation changed.

**Requested behavior:** while Rogue/Assassin Vanish is active, skills that select and highlight an enemy cannot target that player. Faint visual visibility from the existing Vanish presentation must not make the player eligible for a skill lock.

- [ ] Identify the actual active Vanish status/state and the shared skill target-selection paths. Use that authoritative state, not ordinary invisibility alone, renderer opacity or a missing model.
- [ ] Exclude vanished enemies from targeted-skill acquisition, target outlines/highlights and sticky/maintained target locks on both client and server. Reject a manually supplied or stale target even if the client still sends its entity ID.
- [ ] Clear an existing selected/highlighted enemy when they activate Vanish; the skill must not keep tracking them through a previous lock. Recheck eligibility when releasing a targeted cast that began before Vanish.
- [ ] Audit nearest-enemy acquisition and homing/reacquisition for target-lock bypasses; apply the same eligibility rule where those skills select an enemy rather than allowing them to silently lock onto a vanished player.
- [ ] Preserve the existing Vanish mark-cleansing and breathing visibility/weapon presentation. This request does not by itself grant damage immunity: untargeted area attacks, ground effects, ordinary collisions and the treatment of already-flying projectiles need explicit separation from enemy selection. Do not cancel all incoming damage as a shortcut.
- [ ] Preserve friendly healing/buff eligibility unless a separate rule requires otherwise; this request concerns hostile skill targeting. Avoid changing every invisible entity or unrelated stealth effect globally.
- [ ] Test acquisition before/during/after Vanish, an existing highlight/lock, Vanish during a cast, stale client requests, homing target selection, multiplayer client/server agreement and normal targeting after expiry. Confirm Vanish grants no lingering untargetability after it ends.

**Acceptance:** a vanished enemy cannot be selected, highlighted or retained as a hostile skill target; server validation agrees with the client. This target-protection change does not implicitly make Vanish immune to untargeted damage.

## Additional request: Purge Heal targeting range

**Status:** Planning only; no installed spell changed.

- [ ] Increase Paladin's **Purge Heal targeting range to 30 blocks**, including its Improved, Divine Strength and Absolution variants.
- [ ] Preserve cast time, cooldown, damage, healing, stun and existing party/target eligibility rules. This changes target acquisition range only; Divine Strength's healing radius and Absolution's explosion radius remain unchanged.
- [ ] Update the native range field and any runtime targeting override, and verify client highlighting and server validation agree at the 30-block boundary. Ensure scroll and skill details show the new range through their normal range display.

**Acceptance:** Purge Heal and its upgraded variants can target eligible enemies within 30 blocks and reject targets beyond that range.

## Additional request: slight homing for Flame Slash

**Status:** Planning only; no installed projectile behavior changed. The user's “fire slash” refers to Wizards' **Flame Slash** (`wizards:fire_slash`).

**Requested behavior:** Flame Slash remains castable without selecting a target. After launch, the projectile detects the nearest eligible enemy and gently steers toward it, similar to Throw Weapon's slight homing.

- [ ] Inspect the installed Throw Weapon steering implementation and reuse its established vector/turn-limit approach where suitable. Recommended steering strength is **2 degrees per tick**, matching the previously specified Throw Weapon behavior; this is slight homing, not instant turning or guaranteed hits.
- [ ] Acquire the nearest eligible enemy around the projectile, not merely the enemy closest to the caster or a preselected crosshair target. Recommended detection radius is **8 blocks**, within Flame Slash's existing 16-block travel range; this search-radius value is a proposal, not a user-confirmed number.
- [ ] Preserve free-aim casting. With no eligible enemy detected, continue along the current trajectory rather than rejecting the cast, stopping the projectile or inventing a target.
- [ ] Preserve current projectile speed, lifetime/range, wave shape, collision/piercing, damage and burning except for the separately planned mage damage reduction. Homing must not reset distance traveled, extend range or grant wall penetration.
- [ ] Use server-authoritative enemy eligibility, protecting the caster, party allies and owned/party pets. Respect the planned Vanish targeting exclusion. Prefer candidates with a valid line of sight; do not steer toward enemies through walls as though the slash could phase through terrain.
- [ ] Bound acquisition work. Retain a valid acquired target rather than scanning every entity every tick; search locally at a modest interval and reacquire when the target becomes invalid or after piercing past it. Apply the angular turn limit every tick. Do not repeatedly pull a piercing wave backward toward a target it has already struck.
- [ ] Verify a cast with no initial target, nearby enemy acquisition, bounded steering, a moving target, no-target straight travel, target death/removal, multiple enemies, party/pet/Vanish exclusions, collision and range limits. Check visible wave movement agrees with server collision and damage.

**Acceptance:** a freely cast Flame Slash can gently curve toward the nearest eligible enemy it detects, while retaining ordinary misses, terrain collision and its existing travel limits.

## Additional request: melee and Rogue progression-node buffs

**Status:** Planning only; no installed node rewards or descriptions changed.

| Tree / node family | Current reward per node | Planned reward per node |
| --- | --- | --- |
| Adventurer Skills ordinary melee-damage nodes | +2% melee damage | **+3% melee damage** |
| RPG Skills Warrior path/root and ordinary damage progression | +2% attack/melee damage | **+4% attack/melee damage** |
| RPG Skills Rogue path/root and ordinary attack-speed progression, including Rogue Empowerment | +1% attack speed | **+2% attack speed** |

- [ ] Enumerate the actual rewards and node references in both installed trees. Apply these amounts consistently to the relevant root/path and ordinary progression definitions, without treating a shared definition as multiple separate grants.
- [ ] Preserve each reward's existing attribute and operation. These are percentage node bonuses, not flat attack damage or a change to the character's base attack-speed value. Keep existing allocations, connections and exclusivity.
- [ ] Update node titles/details where necessary so Path of the Warrior and Rogue Empowerment show their new values. Use concise percentage descriptions following the description-rewrite guidance.
- [ ] Leave unrelated major nodes, such as Martial Prowess, unchanged. Adventurer attack-speed nodes remain at their existing 2%; this Rogue change targets the RPG tree. Ranged and magic progression rewards do not change through this request.
- [ ] Verify one and multiple allocated nodes, existing-player reward refresh/relogin and reset/reallocation. Confirm bonuses update once, stack through the original operation, and remove correctly without leaving stale modifiers or requiring players to earn their nodes again.

**Acceptance:** relevant Adventurer melee nodes grant 3% each, RPG Warrior damage progression grants 4% each, and RPG Rogue attack-speed progression grants 2% each; displayed and effective rewards agree.

## Additional requests: faster preparation and casting

**Status:** Planning only; preserve damage, healing, cooldowns and upgrade benefits except where this plan explicitly changes them.

| Skill | Installed timing | Planned timing |
| --- | --- | --- |
| Blessed Strikes | 2.5s channel; five pulses 0.5s apart | **1.5s channel; five pulses 0.3s apart** |
| Divine Oath | 0.75s cast | **0.25s cast**, including Decree/Judgement variants; supersedes the earlier 0.5s proposal |
| Warrior Throw Weapon | Existing charged-cast timing | **Approximately 0.3s**; recommended exact full-charge time **0.3s / 6 ticks** |
| Warrior Throw Net | Existing charged-cast timing | **Approximately 0.3s**; recommended exact full-charge time **0.3s / 6 ticks** |

- [ ] Blessed Strikes delivers at 0.3/0.6/0.9/1.2/1.5s before native haste adjustments, with no extra time-zero pulse. Retain exactly five charges, existing per-pulse healing, stored-charge duration, wave behavior and final-pulse Seal of Light healing.
- [ ] Keep Divine Oath's normal release/target validation and hammer effects; shorten its cast to **0.25s / 5 ticks**. The user notes that the falling hammer adds further delay before the recipient receives the buff. Preserve that existing delivery behavior, base/Improved cooldowns and recipient buffs; test total cast-to-impact/buff timing rather than treating the shortened cast as an instant grant.
- [ ] For both Warrior throws, treat the requested shorter timing as the full-charge point while preserving quick-release charging behavior. Map their existing minimum-to-maximum damage/range curves onto the new timing, rather than accidentally reducing full-charge damage or making every release a minimum-strength hit. This interpretation is recommended for implementation; the user requested approximately 0.3s casting.
- [ ] Test rapid/held input, early cancellation, charge endpoints, native haste handling, actual pulse times, final-pulse effects and client/server animation synchronization. Do not confuse shorter preparation with shorter cooldowns or faster projectile travel.

## Additional request: Sinful Retaliation true-damage effect

**Status:** Planning only.

- [ ] Replace the retaliating attacker's **4s burning** with **1 HP true damage every 0.5s for 4s**. Recommended schedule: first tick at 0.5s, last at 4s; eight ticks, **8 nominal HP total**.
- [ ] Retain combat-roll activation, 6s readiness, the next qualifying attacker consuming readiness, marking and +15% damage taken for 8s, and the existing 12s haste-affected cooldown. Do not leave the replaced ignition action active as well.
- [ ] Use the pack's true-damage semantics: bypass armor/ordinary damage reduction without bypassing absorption hearts. Keep source attribution and normal player-source PvP/PvE policy; verify and document the resulting retained damage rather than introducing a new blanket exception silently.
- [ ] Do not scale this fixed damage with melee/healing power, criticals or haste. Haste does not accelerate the 0.5s ticks. Prevent retaliation recursion and accidental duplicate schedules from one qualifying hit; define refresh behavior explicitly if reapplied by another source rather than creating unlimited parallel copies.
- [ ] Give the effect a suitable visible debuff icon/detail and restrained periodic cue. Test all eight ticks, shields, defenses, party exclusions, death/removal cleanup and the retained mark/vulnerability duration.

## Additional request: Purgatory containment and initial pull

**Status:** Planning only. These additions supersede the installed 2s cooldown; other existing stage timings remain unless changed explicitly.

- [ ] Set Purgatory's base cooldown to **12s**, retaining haste scaling and cooldown beginning when the channel ends.
- [ ] Make the **first pulse** apply a very strong inward pull to eligible enemies within the pull/taunt radius. Later pulses retain their existing stage-based pull unless further adjusted. Recommended implementation is bounded movement/velocity respecting collision, not teleportation through terrain; tune the initial strength with runtime testing.
- [ ] Create an impassable living-entity boundary at the **pull/taunt radius**, approximately **5 blocks high**. Preserve the existing outer radius: **12 blocks**, or **14 with Improved Purgatory**. This does not enlarge the inner 6/7-block damage radius.
- [ ] The boundary must be visible: reuse the Priest Barrier's suitable shield presentation, tinted **orange**, matching Purgatory's fire effects. Reuse the visual resources without importing Priest Barrier's buffs, healing or unrelated gameplay rules.
- [ ] Keep the boundary active until Purgatory ends, including normal release/completion, early control interruption, death, Final Resolute, dispel, logout, dimension change, teleport or respec cleanup. No leftover collision wall or visual may persist after the channel.
- [ ] Preserve the counterplay: stun/Frozen can interrupt the first phase, before 10s, removing the barrier. Existing control protection from 10s onward remains; root behavior and manual release remain unchanged.
- [ ] Recommended collision policy is to contain hostile living entities in both directions while allowing protected party allies/caster through; this policy is not yet user-confirmed. Confirm the intended friendly collision before implementing. A five-block wall does not automatically imply a roof, anti-teleport or projectile blocking; do not add those mechanics silently.
- [ ] Implement server-authoritative collision with client presentation matching radius/height. Reuse established temporary-wall collision techniques where suitable. Avoid world-block placement, unnecessary full-world scans, or collision that remains after state cleanup.
- [ ] Test first-pulse pulling, the exact 12/14-block boundary and 5-block height, large/small entity hitboxes, inward/outward crossing, normal terrain collision, early interruption, manual release, all terminal paths and multiplayer visibility. Check whether enemies can cross during packet/tick delays and verify the barrier does not repeatedly snap or suffocate entities at its edge.

## Additional verification request: Archer Barrage homing

**Status:** Verification requested; inheritance has not been confirmed by this plan entry.

- [ ] Confirm that all arrows fired by Archer's **Barrage**, including extra arrows from its upgrades, receive the Archer path's existing **slight ordinary-arrow homing assist** when that benefit is allocated.
- [ ] Trace projectile creation/ownership through Barrage and its consecutive launches. If a bypass exists, connect the arrows to the same assist path instead of adding a second homing system or stronger steering.
- [ ] Preserve ordinary-assist activation conditions, search/cone/turn limits, duration and party/pet/Vanish exclusions. Do not give the benefit to players without the relevant node or confuse it with Magic Arrows' separate homing behavior.
- [ ] Verify every arrow in a volley, additional-arrow upgrades, node absent/present, nearby moving targets and unchanged spread, damage, cadence and projectile lifetime. Report observed behavior before claiming inheritance works.

## Additional request: Frenzy arrows bypass ordinary damage i-frames

**Status:** Planning only. Scope is Frenzy arrows, following the preceding discussion; ordinary non-Frenzy arrows are not included.

- [ ] Allow Frenzy arrows to bypass the target's ordinary post-hit damage cooldown, using the same scoped principle as Barrage's `arrow_perks.bypass_iframes`. This prevents that cooldown from rejecting successive legitimate impacts and making the arrows bounce off.
- [ ] Identify Frenzy projectiles through their existing projectile state/tag. Do not attach Barrage itself as a shortcut: that could apply Barrage's separate damage multiplier or trigger unrelated skill effects.
- [ ] Retain Frenzy's current damage multipliers: **20%** at zero through two heat bars, then **30% / 40% / 50%** at three/four/five bars. Preserve heat, spread, recoil, ammunition/Infinity, critical rules, burst exhaustion and manual loading.
- [ ] Preserve armor, resistance, absorption, shields, party protection, PvP/PvE damage policy and explicit skill invulnerability. This bypass concerns the ordinary hit cooldown, not true damage or immunity penetration.
- [ ] Apply the bypass only around the relevant impact and preserve/restore pre-existing damage-cooldown state appropriately. Do not permanently zero the target's timer or change global arrow/melee damage rules.
- [ ] Verify rapid consecutive Frenzy hits, both hands, ordinary and modded crossbows, multiple attackers, heat stages, piercing and rejection by legitimate defenses. Each projectile must damage a given target only as allowed by its normal piercing/hit rules; prevent duplicate damage or repeated contact loops. Keep ordinary-arrow behavior and Barrage unchanged.

**Acceptance:** the ordinary post-hit damage cooldown does not reject a legitimate Frenzy impact; other defensive and targeting rules remain effective.
