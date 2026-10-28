# Paladin tier-I rework ? brainstorming draft

Status: gameplay design confirmed through the user's range/swing revisions and final Seal of Light confirmation. Implemented and verified; see docs/patches/paladin-tier-one/README.md and the completed implementation plan. Requested28 October2026. No gameplay changes are applied by this document. Current working changes from the Priest doctrine task remain intact. The broader armor normalization discussion is separate and is not approved by this request.

## Intent and scope

Paladins heal through combat and preparation rather than unrestricted targeted healing. Replace Flash Heal with an enemy-targeted ranged attack, and turn Blessed Strikes charges into ranged waves triggered by melee swings. Retain class allocation, branch exclusivity, party safety and short readable node descriptions. Audit haste across all Paladin actives, upgrades, passives and custom trigger timers. Later tiers keep their existing mechanics except required haste integration.

## Confirmed requested values

### Equipment

Justicar flat Healing Power becomes1.50 per piece: four pieces give6, plus the existing2-point set bonus gives8. Lightbringer remains1.75 per piece. Other armor modifiers and set effects remain.

### Purge Heal (replaces Flash Heal)

- Enemy targeting only; range24 blocks, cast0.5s, cooldown5s.
- A successful damaging hit deals50% M and heals the caster25% P.
- Improved Purge Heal sets damage60% M and healing30% P. These are final coefficients, not additive60%/30% increases.
- Divine Strength sets healing40% P and also heals eligible allies within4 blocks of the caster for the same amount. Replaces the previous attack-damage buff.
- Cleanse becomes Absolution: sets damage80% M and adds a3-block impact explosion. The old cleanse action is removed.
- Divine Strength and Absolution retain their existing exclusive branch relationship.

### Blessed Strikes

- Channel2.5s, five deliveries, cooldown14s; prepared duration remains15s, refreshed by pulses.
- Every delivery adds one charge and heals the caster5% P. Maximum five charges.
- An actual timed melee weapon swing releases one wave and consumes one charge, even if the ordinary melee attack misses. Holding left-click must release waves on the legal repeated melee swing cadence, not merely the initial tap/press event. Replace the old impact-triggered bonus damage entirely.
- Wave range20 blocks. Base waves have no enemy piercing: stop after the first eligible enemy hit. The skill must reach distant and flying enemies, so release direction follows aimed yaw and pitch rather than being locked to the horizontal ground plane.
- Wave deals30% M, projectile speed1.4. Requested width/visual size is1.2 times Hammer of Destiny's wave. Retain the prepared weapon glint.
- Improved Blessed Strikes reduces cooldown2s to12s; still five deliveries and five charges.
- Seal of Wrath sets wave damage40% M and allows a maximum of TWO enemy hits per wave in total (first target plus one additional target), not two additional enemies. No homing or ricochet is requested.
- Seal of Light sets self-healing10% P per channel delivery; the fifth delivery also heals eligible allies within4 blocks of the caster. The confirmed ally-heal amount is10% P.
- Seal branches remain exclusive. Charges do not grant the old extra hybrid hit or consumption-based Seal of Light healing.

## Inspection and reusable assets

Installed Arsenal1.5.1 names arsenal:unique_hammer_1 Hammer of Destiny. Its available melee wave resource is data/arsenal/spell/shockwave_melee.json, with model arsenal:spell_projectile/shockwave_large. That resource normally triggers on melee impact, has10-block range,3.5 hitbox width, model scale4, speed0.75,0.5 knockback and pierce999. Confirm the hammer's native assignment during implementation before cloning its visual. Do not change Arsenal's original weapon passive.

A1.2 width multiplier would give4.2-block wave width and model scale4.8. Use a separate golden visual/texture variant so recoloring does not affect Hammer of Destiny. Reuse suitable RPG casting/impact particles and existing icons where possible; inspect and visually verify the result rather than promise recoloring from JSON alone.

The current Flash Heal has an ally fallback and direct heal; both must disappear. Blessed Strikes currently uses a melee-impact stash and resolves mixed-school power at consumption. Its replacement needs a server-authoritative hook for actual melee attack execution, including repeated Better Combat swings while holding attack. A raw mouse press/attack packet or damage-on-hit trigger alone cannot satisfy the requested timing. Inspect the installed combat event/API and verify its air-swing/held-input path before choosing the hook. The Arsenal reference explicitly disables shooter pitch inheritance; do not copy that setting into the new aimed wave. Current Conviction references Flash Heal and Blessed Strikes; its reset must follow Purge Heal under the preserved ID or updated references.

## Approaches for discussion

1. Recommended: retain existing spell/status/node IDs for save compatibility; rename visible content, replace tier-I actions, and add only the swing-release/server-impact logic absent from native spell delivery. Reuse native power, cooldown, party and projectile paths. Old allocated nodes keep their meaning without orphaning saves.
2. New spell IDs with explicit migration: clearer new identifiers but requires moving saved bindings, scrolls, loot, spellbook entries, cooldown reset references and existing allocations. More migration risk without a player-visible advantage here.
3. Pure resource rework: smallest code footprint, but existing impact triggers do not provide reliable miss/air-swing releases or once-per-cast success-gated healing. Not sufficient unless native swing APIs prove complete during inspection.

## Implementation decisions from the accepted proposal

- M scales offensive impacts only; P scales healing only. Remove old mixed-school bonuses from these two spells and their nodes.
- Purge heals once per successful cast, only after an eligible enemy actually loses HP. Misses, invulnerability, allies and blocked zero-damage hits do not heal. Absolution splash cannot multiply the caster heal per enemy; direct target is not damaged twice.
- Keep ordinary line-of-sight targeting for Purge. Implement the requested enemy attack with native delivery and validate its impact before healing; do not add an unrequested projectile travel speed.
- Confirmed revised wave range20 blocks. Base wave stops at the first eligible enemy; Seal of Wrath deals40% M and stops after at most two distinct enemy hits. Each enemy can be damaged only once per wave. Proposal retained: straight aimed flight, terrain collision,0.5 knockback, no homing or distance falloff. Allies and protected pets do not consume the enemy-hit budget. Pitch must permit attacks above and below the caster, including flying enemies.
- Release exactly once per actual legal melee swing execution, including repeated timed swings while left-click is held with Better Combat. Neither the initial button press alone nor an ordinary hit is the trigger. Reject packet spam and duplicate main/offhand/event callbacks; packets alone must not burn all five charges. Shield use, mining, empty-hand gestures and wave hits do not trigger extra releases. No separate wave cooldown beyond legal melee attack cadence.
- Allies/pets stay safe under current Better Party integration; caster is not counted twice in area healing.
- Haste shortens cast/channel duration, delivery spacing and existing cooldown/stack-gain intervals, including custom timers. It does not increase coefficients, pulse count, charge cap, buff duration or projectile speed. Zero-cooldown passives remain zero; do not invent new cooldowns. Use the resolved school for each skill and avoid applying haste twice. Confirm the desired scope is Paladin-wide rather than a fresh all-class overhaul.
- On death clear prepared waves/glint and any related transient state; preserve normal binding/allocated skill data.

## Synchronization and validation plan

After design approval: produce a concrete implementation plan, snapshot current JARs, preserve unrelated edits, implement native-compatible replacements and install same-name final archives with ignored backups. Update class and weapon-tree names/descriptions, scrolls, spellbook/binding display, root/upgrade icons, Conviction references, class raw reference, description rewrite ledger, patch notes and player handbook/PDF. Text uses percentages and concise effects; native yellow rows show cooldown/trigger where supported, without duplicated formula or haste prose.

Verify Justicar6 item power plus2 set bonus, existing config override, other sets unchanged; native Purge damage/healing variants and once-only blast healing; five exact Blessed pulses/charges, early channel release, air swings and continuous held-left-click swing intervals, firing between targets, vertical aiming/flying targets at20 blocks, base one-hit and Wrath two-hit maximum, shield/mining/no-weapon rejection, Better Combat duplicates, target once per wave, party/pet safety, damage routing80%/35% without reducing healing, haste at baseline and elevated values, expiration/death/respec/save compatibility. Use isolated native server and client for swing behavior, golden wave dimensions, prepared glint, real names/tooltips and channel visuals. Compilation alone is not runtime proof. Rebuild/review handbook after the installed behavior is final. No push requested.

## Open discussion

The user accepts the overall direction and overrides the proposed10-block piercing baseline with20 blocks, no base piercing, Wrath40% M and two total enemy hits. Preserve existing IDs as the recommended implementation approach. Seal of Light's fifth pulse heals eligible allies10% P, confirmed by the user. No remaining gameplay question blocks implementation planning.
