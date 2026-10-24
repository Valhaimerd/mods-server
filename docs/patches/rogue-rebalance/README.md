# Rogue tier I and II adjustments - 24 October 2026

Minecraft 26.2 / NeoForge 26.2.0.90, Java25 bytecode. Same-filename overlays of current Rogues, Skill Tree and Spell Engine archives preserve all previous gameplay, party, PvP, tooltip and lifecycle repairs outside the listed changes.

| Tier I | New value |
|---|---|
| Shock Powder | 14s base cooldown; 4-block stun radius. Existing 1/3s stun logic and Improved +1s stay intact. |
| Explosive Powder | Guaranteed 50% melee-power damage; 4-block splash; no distance falloff or second stun. |
| Slice and Dice | 16s base cooldown; 10 empowered hits. |
| Improved Slice and Dice | Cooldown -2s to 14s; no additional hit charges. |
| Blades of Fury | +4 charges, for 14 hits. |
| Fleet Footed | User confirmed 3% movement per stack; existing 12-stack cap and 4s refreshed duration retained. |

| Tier II | New value |
|---|---|
| Shadowstep | Existing 12s base cooldown and 14/18-block range. One cooldown reset for an attributed enemy kill within 4s of a successful teleport. |
| Cloak of Shadows | Immunity for 10 ticks / 0.5s; repeated hits remain blocked during that interval. |
| Ambush | Immediate 40% melee-power damage; existing 2s Blindness I, Weakness I and Slowness I retained. |
| Serrated Traps | Bleed duration 2.5s. |
| Opportunist | Guaranteed next-melee critical hit; additional critical damage removed. Existing 6s base cooldown and ready window retained. |

All cooldowns remain haste-affected. Bear Trap stays at 40% ordinary / 60% Improved damage, 4s base cooldown, 1s root and 3s Slowness I. Extensive Coverage and tier III are unchanged.

## Shadowstep reset and lifecycle

The success callback records one expiry timestamp on the caster, with no retained entity collection and no global tick listener. A noncanceled attributed enemy death during the next 80 ticks schedules one confirmation for the following tick. This lets the cast impose its cooldown first, so an immediate Ambush kill cannot lose the reset. Confirmation rejects living/revived targets, removed/dead casters, consumed windows and superseded casts. Self, party and owned-pet protections still apply. The exact 80-tick boundary is expired; the window is consumed after the first reset. Logout/login clear the short-lived marker. A lone expiry scalar cannot accumulate projectiles or entity references; deferred tasks are bounded to the next scheduler tick.

Opportunist's effect configuration is overridden after the existing configuration callback, so stale generated configs cannot retain the removed critical-damage modifier. Existing critical chance is preserved. The original RogueRules class, including the previous 60% Improved Trap constant, remains byte-identical.

## Rebuild and evidence

```powershell
python docs/patches/rogue-rebalance/build.py
python docs/patches/rogue-rebalance/run_probe.py
python docs/patches/rogue-rebalance/run_client.py
# Wait for tmp/rogue-rebalance/client/tooltip-result.txt.
python docs/patches/rogue-rebalance/verify.py --candidate
```

The builder pins installed inputs under ignored `tmp/rogue-rebalance/before/`. It updates the exact spell resources and tooltips, and adds one helper and one mixin to the engine. The baseline failed the new Shock cooldown assertion (12 instead of 14). The candidate passed 1261 dedicated Warrior/shield/Rogue/party checks, including native Slice charge consumption and cooldown modifiers, explosive damage/falloff, Cloak immunity/expiry, Ambush, trap bleed, unchanged Fleet stacking and Opportunist's absent critical damage. Eleven dedicated Shadowstep checks cover no-window/failed casts, opening the window, deferred reset, one reset per cast, boundary expiry, ownerless damage, owned pets and revived/surviving targets. The connected client/integrated server passed 43 native tooltip checks with all three final archives loaded.

The focused reference is `../rogue-reference/readable.md`, exported into `mod fixing/Rogue skills - raw attributes and nodes.md` after installation. Root README is synchronized. Existing handbook class overview and controls remain accurate; no catalog or PDF change is needed for these detailed balance values. No unrelated files are reverted.

Same filenames replace active mods and fixed-pack copies; previous copies are retained in ignored `retired packs/rogue-rebalance-2026-10-24/`. No personal profile, external server or world is changed. Matching client/server updates and restart are required to use the changes. No commit or push requested.
