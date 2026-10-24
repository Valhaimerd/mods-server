# Bear Trap and Rain of Arrows balance

- Bear Trap base hit: 40% dual-wield melee power, one-second unrestricted root and three-second Slowness I. Improved Bear Trap retains 75% damage; Serrated Bleed and activation geometry retain their existing behavior. The fixed 0.2-second ground-casting animation is preserved.
- Rain of Arrows: 60% ranged power per impact for every variant. Base/Torrential retain 40 arrows at five/two-tick intervals. Endless retains 60 arrows but adds two ticks to the base five-tick interval: seven ticks / 0.35 seconds per arrow, a 20.65-second first-to-last launch span. Six-block scatter, 2.5-block impact splash, target following, cooldown and party/pet protection are preserved.

Exactly two JSON resources change in each current same-filename Rogues, Archers and Skill Tree JAR: the requested spell data and its affected English descriptions. No classes or other resources change. Source builders, native probes, raw skill references and player handbook are synchronized. Older patch notes retain historical values; this is the current follow-up.

The configuration assertion failed against the old Bear Trap coefficient before the patch. The isolated dedicated-server run then passed 2,842 Archer/Rogue/party measurements, including actual base/improved trap damage, root/slow durations, party protection and native scheduled seven-tick Endless intervals. No new full client playthrough was run for these numeric changes.

```
python docs/patches/trap-rain-balance/build.py
python docs/patches/trap-rain-balance/check_values.py tmp/trap-rain-balance/built
python docs/patches/rogue-tier-three/run_probe.py --candidates tmp/trap-rain-balance/built
python docs/patches/trap-rain-balance/verify.py
```

Pinned inputs are ignored under `tmp/trap-rain-balance/before/`; `artifacts.json` records their hashes and the exact output entry changes. Previous active JARs are backed up under ignored `mod fixing/retired packs/trap-rain-balance-2026-10-23/`; matching verified replacements reside in `mods/` and `mod fixing/1 fixed packs/`. Personal installations and worlds were untouched. No commit or push was requested.
