# Rogue tier III rework

Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 production classes. Same active filenames for Rogues 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5. The pinned inputs contain the current Archer revision and Rogue tiers I/II; this layer preserves unrelated entries byte-for-byte.

## Player changes

- Vanish: 8-second haste-affected base cooldown, 7-second Stealth. Base movement penalty is 50%; Improved reduces it to 40%, and Stealth Speed reduces it by another 20 percentage points to 20%. The improved node no longer extends duration.
- Shadow Stealth replaces Deep Stealth. Attacks, spells, item use and incoming hits no longer cancel Vanish. Natural expiration and explicit dispels still remove it. Native visuals and mob detection rules remain intact.
- Mutilate: 200% combined dual-wield melee damage per enemy in its native 160-degree sweep. Improved keeps the 12-to-10-second cooldown reduction. Crimson Strikes remains 10% raw dual power healing per target. Bloody Strikes replaces Envenom with 7.5 seconds of existing Rogue Bleed, using Serrated Traps' existing strength scaling.
- Cheat Death retains its fatal-hit trigger, three-second immunity and 60-second haste-affected base cooldown. It additionally grants Speed III and Jump Boost III for three seconds, plus 30 percentage points of evasion for six seconds. Its existing trigger requires an attributed attacker; it does not heal.
- Prepared Counter replaces Preparation. Evading an attributed enemy attack always counters that attacker for 25% combined dual-wield power, with a one-second haste-affected base cooldown. It no longer resets skill cooldowns. Shared party and pet protections still apply.

## Native mechanics corrected

Native melee delivery wrote `lastHurtMobTimestamp` rather than the attack-strength timer. A Mutilate-only wrapper fills the actual attack and item-swap strength fields while performing each strike and restores them afterward. Its configured +100% bonus therefore produces a 200% full-strength strike even when the normal attack meter is depleted. Other melee skills keep their existing path.

Native melee modifiers remained installed during appended secondary impacts, inflating their power calculation. Mutilate's secondary effects now receive a snapshot of unmodified dual power so the attack's 200% coefficient does not also multiply Crimson healing or Bleed strength. Critical hits, enchantments and target defenses retain their native behavior.

Action cancellation is patched at RogueEffects' existing removal calls; incoming-hit cancellation is patched at Spell Engine's existing RemoveOnHit call with the affected entity supplied. Only Stealth with the Shadow Stealth node skips cancellation. All other effects retain their removal behavior. Temporary movement effects are removed with Stealth, including early dispels.

## Verification and rebuilding

The configuration check failed against the previous active Vanish cooldown before implementation. The final isolated dedicated-server run passed **2,842 Archer/Rogue/party measurements**, including movement penalties, all four stealth-break paths with and without Shadow Stealth, natural expiry, 200% raw Mutilate damage, unchanged 10% Crimson healing, Bleed duration, Cheat Death's extra buffs, counterattack target/damage/cooldown and owned-pet protection. Full measurements are ignored under `tmp/rogue-tier-three/server/archer-results.txt`; focused evidence is in `runtime-results.txt`. This was not a multiplayer visual playthrough.

```
python docs/patches/rogue-tier-three/build.py
python docs/patches/rogue-tier-three/check_values.py tmp/rogue-tier-three/built
python docs/patches/rogue-tier-three/run_probe.py
python docs/patches/rogue-tier-three/verify.py
python docs/patches/rogue-reference/export.py
```

`inputs.json` pins the ignored `tmp/rogue-tier-three/before/` inputs. `artifacts.json` records exact archive entry changes and output hashes. Tests and ASM tools are not shipped. The previous active JARs are retained under ignored `mod fixing/retired packs/rogue-tier-three-2026-10-23/`; verified replacements also reside in `mod fixing/1 fixed packs/`. Personal installations and worlds were untouched.
