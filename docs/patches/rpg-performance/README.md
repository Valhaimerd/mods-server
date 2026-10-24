# RPG entity lifecycle audit and cleanup

24 October 2026. Spell Engine 1.10.5, Archers/Wizards/Rogues/Paladins 3.1.2, Skill Tree 1.6.1; Minecraft 26.2 / NeoForge 26.2.0.90 / Java25 bytecode. This follows the user's correction to investigate the RPG series. No Bifrost optimization is included.

## Confirmed defects

| Path | Installed behavior | Repair |
|---|---|---|
| Spell Cloud with missing spell data | `tick()` returns before the lifetime check. An invalid cloud can continue ticking indefinitely. A removed cloud variant can also throw on an invalid index. | Bounds-check the native cloud lookup and discard invalid clouds on the server. Client synchronization may briefly lack data, so clients return null without independently deleting entities. Healthy lookups still resolve the registry once. |
| Hydras, Frost Elementals and Spirit Wolves | Native save data records lifetime/phase limits but omits `tickCount`, which those limits compare against. Reloading resets elapsed age. | Save and restore elapsed age through the existing entity save/load methods. All configured lifetimes, phases, damage and ownership remain intact. |
| Spell projectiles | Native save data omits elapsed age, traveled distance and configured range, restarting or changing their expiry limits after reload. | Preserve all three values through native save/load. Existing distance and age caps continue to own disposal. |

A native baseline reproduced the first cloud failure and age resets on all three summoned creature types before production changes. The final focused test also covers valid cloud survival/expiry, missing cloud variants, summon expiry immediately after reload, old saves lacking the new field, and projectile distance expiry after reload.

The save extension uses `rpg_pack_age`, `rpg_pack_range` and `rpg_pack_distance`. Old saves remain accepted; their already-lost elapsed age cannot be reconstructed, so no speculative mass deletion is performed. Lifetimes still count loaded simulation ticks, consistent with the existing design, rather than wall-clock time while chunks are unloaded. Ordinary tamed pets are not affected. No new global tick listener, static entity collection, periodic world sweep or particle effect is introduced.

## Other findings and limits

- The process inventory before testing contained the user's launcher and active Minecraft process, with no leftover repository test JVM. Test JVMs started for this audit are isolated, and the lifecycle runner has a 180-second subprocess timeout.
- Magic Arrows already terminate at 200 ticks through their rework. Ordinary Spell Projectiles have a native 1200-tick safeguard, distance expiry and owner-removal handling. Their problem here was persistence across reloads, rather than an absent loaded-time cap.
- Healthy clouds already serialize age and expire normally. Ownerless but valid clouds retain their configured lifetime; they are not blindly deleted.
- Hydras search a local area each tick, and Magic Arrows search a 32-block area each tick. These are potential load multipliers with many simultaneous summons/projectiles. This pass preserves targeting responsiveness and does not assert those searches explain the reported server delay without a representative profile.
- Local Warrior drag/channel/rage and Wizard channel caches use weak entity keys with value records that do not retain the entity key. Wall burst state has player logout, expiry and server-stop cleanup. No unbounded retained entity reference was established in those inspected helpers.
- The world scheduler normally removes executed buckets. Its exact-time scheduling and exceptional-task behavior were inspected, but no missed bucket was reproduced in the reported game, so scheduling semantics are unchanged.

These are verified lifecycle corrections, not proof that all server delay has been eliminated. No representative multiplayer MSPT/JFR profile was collected. The player's personal game log had one startup catch-up warning in the inspected tail/search, which does not identify a sustained RPG tick hotspot. The personal profile, worlds and separate desktop server are untouched.

## Build, test and installation

```powershell
python docs/patches/rpg-performance/build.py
python docs/patches/rpg-performance/run_probe.py --candidate
python docs/patches/rpg-performance/run_regression.py
python docs/patches/rpg-performance/run_client.py
# Wait for tmp/rpg-performance/client/tooltip-result.txt.
python docs/patches/rpg-performance/verify.py --candidate --runtime
```

The build pins the currently installed Spell Engine archive under ignored `tmp/rpg-performance/before/`. It adds two mixin classes and their registration. Every existing class, spell resource, damage value, cooldown, party rule, tooltip and item stays byte-identical to that input. This avoids rebuilding an older gameplay layer over newer changes.

The focused suite passed 17 native entity save/load and tick checks. The isolated connected client passed 43 native tooltip checks with the lifecycle build loaded on both client and integrated server; the broad suite retains the existing Warrior, shield, Rogue and party regressions. Hashes and observed counts are recorded in `runtime-results.txt`. After verification the same filename replaces `mods/spell_engine-neoforge-1.10.5+26.2.jar`, with a matching fixed copy and predecessor retained under ignored `mod fixing/retired packs/rpg-performance-2026-10-24/`. Matching updated client/server archives are recommended; no protocol or registry change is made.

The handbook's intended lifetimes and controls remain accurate, so no catalog or PDF rebuild is needed. No GitHub commit or push is part of this task.
