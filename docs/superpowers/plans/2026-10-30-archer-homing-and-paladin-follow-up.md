# Archer arrow assistance and Paladin follow-up

Status: implemented and installed on 30 October 2026. The user authorized execution of this plan; the recorded conservative homing tuning was used. See [implementation and verification](../../patches/class-follow-up/README.md).

## Requested Paladin changes

- Vanguard: increase base proc cooldown from 4s to 5s. Preserve haste scaling, existing stun durations, shield recognition and the extra damage on the triggering hit only. Update native cooldown metadata and references together.
- Ardent Defender: heal 20% of the triggering player's maximum HP instead of 40%; reduce the shared owner cooldown from 90s to 80s, affected by haste. Preserve recipient eligibility, defensive benefits and the rule that the triggering hit is not cancelled.
- Purgatory: replace its final explosion presentation with the installed Fire Mage Meteor ground-impact effect. Preserve existing damage, radii, eligibility, absorption and end conditions.

## Requested Archer Supercharge change

- Supercharge: increase the next normal arrow's bonus damage from 25% to 35% (135% of normal damage total). Remove the associated draw-speed penalty entirely, including any ranged-haste/draw-speed modifier applied while the arrow is readied. Preserve the arrow-hit trigger, 5s prepared window, single-arrow consumption and existing haste-affected cooldown. Brief description: “Your next normal arrow deals 35% more damage. Use within 5s.” Keep trigger and cooldown in the native gold rows. Verify the actual shot gains the new multiplier, preparation does not slow drawing, and consumption/expiry/respec/death leave no old penalty behind, including legacy saved effects and stale configs.

## Archer Concussive Shot cooldown investigation

- User reports Concussive Shot is not using the expected 6s cooldown before haste; supplied tooltip shows 6.6s. Verify the installed spell definition, modifier nodes, runtime cooldown calculation and displayed haste before assigning a cause. A 6.6s effective cooldown alone does not establish the base value, since negative haste could increase it. Expected baseline for this check: 6s at neutral haste, with the normal haste adjustment applied once. Check fresh/stale configs and any residual draw-speed/ranged-haste penalty from Supercharge, especially given its requested removal above. Correct a mismatched base or duplicate modifier at its source and keep the native gold cooldown row accurate. Preserve the arrow-hit trigger and 1s stun.

## Requested Warrior changes

- Rename Momentum to Onslaught across its skill node, associated visible buff names and player references. Remove movement speed from the earned stacks, retaining their attack-speed benefit. Preserve the combat-roll trigger, five-hit preparation, stack limit, duration and existing haste-affected cooldown. Brief description: “Your next 5 melee hits build attack speed, up to 10% for 3s.” Keep trigger and cooldown in the native gold rows. Retain internal IDs for save compatibility; remove obsolete movement-speed modifiers from active/legacy stacks and verify consumption, refresh, expiry, death and respec cleanup.

- Warm Up: replace recovery of `1 HP + 6% M` with `1 HP + 1 HP per 14 Dual Wield power`, using the pack's actual combined Dual Wield power calculation rather than melee power alone. Treat this as discrete increments: `1 + floor(D / 14)` HP, where D is Dual Wield power. Thus D below 14 restores 1 HP, D14 restores 2 HP, and D28 restores 3 HP, before existing healing-received modifiers. Preserve its combat-roll trigger and existing timing rules. Update the brief description to “Rolling restores 1 HP, plus 1 HP per 14 Dual Wield power.” Keep the native gold trigger row and remove the obsolete 6% melee wording from skill/reference descriptions. Verify threshold boundaries and the actual two-weapon power source during implementation.

## Requested Rogue Opportunist rework

- Combat rolling readies Opportunist for 5s. The next successful basic melee hit consumes readiness and opens a 1s guaranteed-critical window for subsequent basic melee attacks, replacing the previous single guaranteed-critical hit. Interpret the 1s benefit as a timed window rather than another single-use charge; the hit that opens the window does not retroactively become a guaranteed critical.
- Start the existing haste-affected cooldown when the 5s readiness is consumed or expires, rather than when the roll occurs. The 1s critical window does not postpone that cooldown start. Prevent additional rolls from refreshing readiness indefinitely or starting overlapping windows while readiness/cooldown is active. Preserve the existing cooldown base unless separately changed; the supplied screenshot's 5s displayed value is not evidence of a neutral-haste base.
- Exclude spells, ranged hits, damage over time and secondary damage procs from consuming readiness or receiving guaranteed basic-melee criticals. Resolve dual-wield/multi-target attacks consistently, without rearming from secondary hits. Clear both states on death/respec; apply the normal cooldown if readiness otherwise expires or is removed during play.
- Brief description: “Rolling readies your next melee hit for 5s. Landing it guarantees critical melee attacks for 1s.” Show trigger and cooldown through native gold rows; distinguish readiness and critical-window status icons/durations. Verify readiness expiry, consumption timing, repeated attacks within the window and cooldown start under positive/negative haste.

## Requested Rogue Vanish / Shadow Stealth changes

- Shadow Stealth: dealing damage ends Vanish. Taking damage and other actions do not end it; normal duration expiry still applies. Update the former “Actions and incoming hits no longer end Vanish” wording to “Dealing damage ends Vanish. Incoming hits and other actions do not.” Check the actual successful outgoing-damage event rather than breaking merely on an attack swing or attempted cast. Audit attributed projectile, spell, periodic and secondary damage so outgoing damage does not bypass the break rule. Preserve mark cleansing and the existing breathing body/armor/weapon visibility effect, restoring normal rendering when stealth breaks.
- Vanish: requested base cooldown is 8s, haste-affected, replacing the user's reported 7s. Verify the installed base first: earlier patch notes already describe an 8s base, so a 7s effective tooltip may reflect haste. Ensure the final neutral-haste base is exactly 8s rather than adding another second to an already-correct definition. This changes cooldown, not Vanish's 7s duration. Synchronize native metadata, fresh/stale configs, node descriptions and class references.
- Verify Shadow Stealth survives incoming hits, empty swings, movement, item use and non-damaging casts; successful outgoing damage ends it immediately. Verify the base Vanish behavior without this upgrade remains independent.

## Requested Rogue Mutilate healing change

- Mutilate now heals the caster for 10% D on a successful damaging hit, where D is the pack's combined Dual Wield power. Crimson Strikes increases that same heal to 20% D total, replacing its previous healing coefficient; it does not add a separate 20% heal on top of the base 10%. Preserve Mutilate's damage and other upgrade behavior. Use the existing successful-hit healing path and normal healing-received modifiers; avoid duplicate healing from secondary damage events. Update the skill and upgrade descriptions to show 10% and 20% D respectively, and verify both variants use Dual Wield power rather than melee power alone.

## Requested Priest Incanter's Cadence change

- Increase Incanter's Cadence from 2% to 3% spell haste per stack. Keep the five-stack limit, giving 15% maximum spell haste instead of 10%. Preserve the existing trigger, chance, refreshed 6s duration and stack-gain interval. Update the actual attribute modifier, skill description and received-buff details together so the tooltip displays the real aggregate haste at each stack. Brief description: “Spell damage grants 3% spell haste per stack, up to 15% for 6s.” Keep trigger and cooldown/interval in the native gold rows. Verify all five stack values and cleanup on expiry/death/respec.

## Priest Divine Favor cooldown correction

- Set/verify Divine Favor's base cooldown to 12s before haste. This supersedes the earlier 15s cooldown specification; retain its separate 15s counter-readiness duration, combat-roll trigger, next-damaging-attack consumption and 1s attacker/nearby-enemy stun within 2 blocks. The supplied screenshot shows 14.2s effective cooldown; inspect actual haste, installed spell/modifier definitions and stale config overrides rather than treating the displayed value as the base. Apply normal haste scaling once and keep the native gold cooldown row accurate. Verify 12s at neutral haste and the expected result with positive/negative haste, without accidentally reducing readiness to 12s.

## Requested Priest Fade changes

- Remove Fade's 15-block aggro-clearing limit: clear pursuit/aggro from all currently loaded eligible enemies targeting the caster in the same dimension, regardless of distance. Do not force-load chunks or affect unrelated targets. Inspect the existing aggro and target-memory handling so the range is removed from the actual behavior as well as the tooltip.
- Reduce complete invisibility from 2s to 1s, including armor and held items. Preserve its combat-roll trigger and existing cooldown rules. Restore normal rendering on expiry, death and respec without leaving equipment hidden.
- Replace the description with “Lose your pursuers and vanish completely for 1s, hiding armor and held items.” Remove the obsolete gold “Range: 15 blocks” row through the appropriate spell/tooltip metadata; avoid leaving a finite range implied elsewhere. Verify an enemy targeting the caster beyond 15 blocks loses pursuit, nearby enemies behave consistently, and hiding lasts exactly 1s. Aggro clearing is an immediate disengage, not indefinite immunity to later detection after Fade ends.

## Requested Fire Mage Blazing Speed cooldown

- Set/verify Blazing Speed's base cooldown to 5s before haste. Preserve its combat-roll trigger, 50% movement-speed bonus and 2s duration. The supplied tooltip shows 3.8s effective cooldown; this may already reflect positive haste, so verify the installed base, modifiers and runtime calculation before changing it. Apply haste once, check neutral and positive/negative haste, and keep the native gold cooldown row accurate. Synchronize stale-config handling and class references if the underlying base needs correction.

## Purgatory effect source verified

Installed source: `mods/wizards-neoforge-3.1.2+26.2.jar`, `data/wizards/spell/fire_meteor.json`, `area_impact`.

Reuse the ground-impact lava circle, flame/smoke sphere, ground-facing `spell_engine:area_effect_473`, and `wizards:fire_meteor_impact` sound (volume 1.5). Anchor the presentation at Purgatory's explosion position; size its ground cue to the actual 12/14-block explosion radius. Do not spawn a damaging Meteor or copy its impact handler: that would add unrelated damage and knockback. Emit once per real explosion, including completion, qualifying death and Final Resolute, with no duplicate end-event burst. Verify the ring remains visible on uneven terrain and that recipient count does not multiply particles/sounds.

## Archer: every arrow gains slight homing

Confirmed intent: every eligible arrow receives only slight assistance. The player must aim close to the enemy themselves; assistance should correct a near miss, not select and chase an enemy from a poorly aimed shot. This applies to mobs and eligible enemy players, including flying targets. Preserve the need to aim and the identity of Magic Arrows' stronger seeking.

Approaches for discussion:

1. Recommended: gently steer toward one eligible target near the original flight direction. Keep that target, limit steering, and require line of sight. This makes near misses more forgiving without snapping to unrelated enemies.
2. Brief launch correction only: assist aim at launch, then use normal flight. More predictable but offers little help against moving targets.
3. Continuous nearest-target seeking: easiest to notice, but risks automatic target switching and undermining Magic Arrows. Not recommended for a slight buff.

Revised starting tuning (proposed numbers, pending runtime tuning): acquire only near the original aimed trajectory, within a 3-degree forward cone and 24 blocks, and only when the unassisted path would pass within 0.75 blocks of the target's hitbox. Use at most 0.25 degrees of steering per tick for at most 0.5 seconds, with an explicit 2-degree total steering budget per arrow. Both the narrow cone and near-miss test must pass; a broad cone alone becomes too forgiving at distance. Evaluate the near-miss path with normal arrow gravity. Preserve current speed, gravity, collision, damage and lifetime; no acceleration, wall passing, target switching, reacquisition or extended chasing. An arrow that was aimed clearly away from the enemy must remain a miss. This supersedes the earlier 6-degree/1-degree-per-tick/1-second proposal. Tune in an isolated client/server trial before finalizing numbers.

Eligibility proposal: require the Archer path on the shooter; include ordinary/tipped/spectral bow and crossbow arrows, including multishot children. Party members, owned pets and other protected entities are excluded. Avoid a second steering layer on Magic Arrows and other already-guided skill projectiles. Audit Barrage, Power Shot, Rain of Arrows and modded arrow creation paths explicitly so each is included once or has a documented reason for exclusion. Rain's area targeting may need distinct treatment to preserve its spread.

Confirmed targeting: assistance also targets eligible enemy players in PvP, as well as mobs. Party members remain excluded. Use the pack's actual hostility/party protection rules rather than treating every non-party player as automatically attackable. Revalidate eligibility while steering, including party membership changes.

## RPG Series loaded-crossbow holding pose

User screenshots show an undesirable downward-facing loaded-crossbow pose; the unloaded display is the preferred visual reference. Plan a correction so a loaded RPG crossbow is carried naturally rather than aimed into the ground. Keep intentional aiming while firing distinct from the resting loaded pose.

Investigate the charged-item model transforms, vanilla charged-crossbow arm pose, RPG/Better Combat animation overrides and their interaction before assigning a cause. The screenshots establish the symptom, not which mod supplies the incorrect transform. Compare loaded/unloaded states on the same weapon and player with matched camera and look direction. Cover vanilla and installed modded crossbows, both hands, first/third person, movement, charging, firing and Frenzy transitions; preserve firing mechanics.

## Paper doll must match other players' view

The top-left paper doll currently holds Hammer of Destiny like a vanilla sword, while the supplied third-person reference shows the RPG weapon held across the body. Make the doll use the same applicable third-person weapon pose, held-item transforms and animation state that a remote player sees.

Identify the installed mod supplying the doll and inspect its render path before selecting the integration. Reuse the world player animation/render state where possible; do not recreate the pose with a hardcoded hammer rotation. Punchy's local first-person animation is not the reference for this feature. Check whether the doll bypasses RPG/Better Combat animation setup or changes state during its GUI render, and prevent GUI rendering from altering the world player's pose.

Acceptance: capture a remote observer's view and the owner's paper doll at the same animation phase. Compare Hammer of Destiny, another two-handed weapon, a normal sword, shield/offhand combinations and loaded/unloaded crossbows. Check idle, walking, attacks and skill channels, with armor and either dominant arm. Account for the doll's camera angle, but require the same grip and relative arm/weapon orientation. Verify the corrected crossbow pose appears consistently in the doll and remote world view. Use an isolated two-client test when verifying what other players actually see; a local third-person screenshot alone cannot prove synchronization.

## Implementation and acceptance checklist after design agreement

- Inspect actual arrow creation/tick hooks and existing class/party APIs before choosing the smallest shared integration point. Confirm support for modded arrows from code, not filenames.
- Verify near-miss correction, deliberate misses, moving/flying targets, eligible enemy players, party exclusions and membership changes, wall occlusion, target death, class respec and existing seeker interactions. Check multiple arrows cannot recursively reapply assistance or gain damage/speed.
- Verify Vanguard's 5s and Ardent Defender's 80s cooldowns at zero and positive haste, Ardent's 20% heal with recipient modifiers, and fresh/stale configs.
- Review Purgatory ground-impact visuals in game on early final-stage release, full completion and Final Resolute. Preserve true-cost absorption behavior and channel/hotbar cleanup.
- Repack under current active filenames; synchronize concise descriptions, raw class references and player documentation using the dedicated description rewrite guidance. Build/review the handbook if its documented behavior changes.

Existing unrelated working-tree edits must be preserved. No commit or GitHub push requested.


## Completion ? 30 October 2026

All requested class, crossbow and paper-doll changes are installed in five same-filename repacks. Fresh/stale isolated servers each pass 80 native checks, including actual haste scaling, party-safe homing, readiness expiry, repeated basic-melee crits and saved legacy modifier cleanup. Ardent Defender required moving its inert metadata from Health to Healing school so the 80s cooldown actually responds to haste; its healing remains based on maximum HP. Native client checks, two-client remote hammer comparison and actual Purgatory release captures passed. Descriptions, available local raw references and the reviewed 79-page handbook are synchronized. This is implementation completion, not a GitHub push.


## Follow-up: Purgatory pulse ground visual

Reuse Meteor's burning-ground cloud upgrade (`fire_tier_3_spell_1_modifier_2`) during every 0.5s channel pulse, with a fixed six-block visual radius. Use a short-lived visual-only native cloud: no extra impacts, damage or repeated ignition sound. Keep the existing finishing explosion. See [the focused implementation](../../patches/purgatory-ground/README.md).
