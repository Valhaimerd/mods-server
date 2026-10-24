# Warrior tier II, scaling and roll passives

23 October 2026; Minecraft 26.2 / NeoForge 26.2.0.90. This overlays the installed [first Warrior revision](../warrior-tier-one/README.md), preserving its shield, throw, net, party and haste fixes. Rogues 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5 keep their active filenames; both client and server need the matching updates. Nothing is committed or pushed by this revision.

## Current values

- Throw Net: 6s base cooldown; Multiple Nets retains its 2s reduction, giving 4s and a 1.5s root. Other net properties remain.
- Endured Vitality: per stack, 1 flat armor plus 6% melee power rounded to the nearest 0.5 armor point. The scaling portion rounds 0.44 to 0.5 and 0.24 to zero; ties round upward. At 40 melee power, each stack gives 3.5 armor. The existing ten-stack cap, eight-second refresh, direct-hit filter, new-heart healing and native 30-armor attribute cap remain. Refresh recalculates armor from current melee power; it does not heal without adding maximum HP. Removal clears the extra scaling modifier.
- Wild Charge: instant, 16s cooldown, **2s base duration**, +50% base movement speed and +25 percentage points of knockback resistance. The final user clarification retains the original 2s base and makes Improved add 1s, for 3s total. It can be cast during control and immediately removes movement/control impairments, preventing new ones for 2s.
- Improved Wild Charge adds 1s to its speed/resistance buff; its separately timed protection remains 2s. Endurance grants full knockback resistance for the whole buff. Concussion Blow prepares the next melee hit for 4s, stunning for 2s without an additional cooldown.
- Demoralizing Shout: instant, 16s cooldown, 12-block radius; no base damage. Enemies lose 8% base melee, ranged and registered offensive magic power and are revealed for 6s. It refreshes without stacking or health/boss/type restrictions. It can be cast during control and gives the caster 3s of purification/protection, including when no enemies are nearby.
- Improved Shout adds 4 blocks, reaching 16. Battle Shout grants caster and party allies +8% base melee/ranged/offensive magic power for 6s at the same 12/16-block radius. Challenging Shout adds 10% melee damage, aggro and a universal 0.2s stun. Aggro respects native target eligibility; it does not control players or make mobs attack Creative/Spectator players.
- Warm Up replaces Intercept: every combat roll heals 1 HP plus rounded 6% melee power, with no additional cooldown. Only the scaling portion rounds to half-HP steps. At 40 power, healing is 3.5 HP. The captured power is noncritical; health-school/max-HP scaling is not used.
- Momentum replaces Second Wind: every roll readies the next melee hit for 5s. That hit grants 3s of +5% base movement speed, +5% base attack speed, +2% base melee damage and -2% base incoming-damage modifier. Base cooldown 10s, haste affected. This is an attribute reduction, not a vanilla Resistance potion level.

Both roll passives have 100% chance. Minecraft uses 2 HP per heart; 0.5 HP is a quarter heart. Party allies and owned pets keep the shared final-impact protection. Magic modifiers cover Arcane, Fire, Frost, Lightning and Soul attributes when registered, excluding Healing Power.

## Control implementation

Only Wild Charge and Demoralizing Shout bypass the client controller's initial spell-impairment check; other spells remain blocked. Their instant casts still use native cooldown, ammunition and source checks. A synchronized `Unhindered` effect removes existing Frozen/stun/root/Slowness/silence effects and statuses with negative movement, flying-speed or jump-strength modifiers. A native `canBeAffected` Mixin and the pack's forced-effect path reject incoming control while protection remains. Its tick hook also removes control inserted by direct effect-map updates. It does not erase unrelated Weakness, gear attributes or terrain collision.

Improved Charge's old cleansing passive becomes a duration modifier, preventing an unintended extension of control immunity. Battle Shout's radius receives the matching Improved Shout modifier. Effect IDs and saved node grants remain compatible. Revised effect configurations override stale generated settings. The original tier-I builder remains historical; this revision has its own updated WarriorRules source.

## Build and validation

Inputs are hash-pinned in `inputs.json` and ignored `tmp/warrior-tier-two/before/`. Build candidates under `tmp/warrior-tier-two/built/`:

```powershell
python docs/patches/warrior-tier-two/build.py
python docs/patches/warrior-tier-two/run_probe.py
python docs/patches/warrior-tier-two/verify.py
python docs/patches/warrior-reference/export.py
```

The isolated dedicated server passed **762 measurements** with `-Xverify:all`. Coverage includes earlier throw/net/shield tests, rounded Vitality armor and capped healing, native and forced-path control immunity, noncontrol-effect preservation, expiry, casting exceptions versus ordinary spells, default/improved charge duration, Endurance, Concussion readiness/stun, high-HP and dragon Shout debuffs/marking, no base Shout damage, matching Shout/Battle Shout range, offensive bonuses, Challenging damage/aggro/stun, actual roll healing, next-hit Momentum, Warrior haste, Rogue tier-II regressions and Better Party checks.

The shared casting-exception helper is exercised on the server and its client bytecode hook is verified statically. This is not a full client input, HUD, animation or multiplayer playtest. Personal Minecraft profiles, personal worlds and the separate desktop server are untouched. Previous active/fixed artifacts are retained under ignored `mod fixing/retired packs/warrior-tier-two-2026-10-23/`.

Latest installed overlay: [Warrior tier III](../warrior-tier-three/README.md) supersedes Momentum and the third-tier skills/passives. Historical values below describe this revision's own pinned inputs and output.
