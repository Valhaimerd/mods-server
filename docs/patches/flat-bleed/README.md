# Flat single-level Bleed - 24 October 2026

Same-filename Skill Tree1.6.1 and Spell Engine1.10.5 overlays, Minecraft26.2 / NeoForge26.2.0.90 / Java25 bytecode. The latest pack was pinned before building; a final install check detected concurrent Warrior Throw changes, so the patch was rebased and retested on those latest shared archives. The first-pass inputs remain under ignored `before-first-pass/`; no newer Warrior changes are overwritten.

| Setting | Final behavior |
|---|---|
| Tick interval | 25 ticks / 1.25 seconds, unchanged |
| Stationary | 2 raw HP per tick |
| Moving horizontally | 6 raw HP per tick, immediately; no speed interpolation |
| Power/amplifier scaling | None |
| Levels | One level only, amplifier0; reapplication may refresh duration, never adds levels |
| Serrated Traps duration | 2.5 seconds, unchanged |
| Bloody Strikes duration | 7.5 seconds, unchanged |

Movement checks horizontal velocity squared above 0.000001, rejecting microscopic drift (speed threshold 0.001 blocks/tick). Vertical movement alone remains stationary; horizontal knockback counts as movement. Damage still uses the native ownerless magic source and normal defenses, so this change does not add player attribution or activate the pack's 30% attributed-PvP multiplier. It is fixed raw damage, rather than resistance-bypassing health subtraction.

The native `BleedStatusEffect` implementation ignores the amplifier and applies only the two flat values, retaining its existing constructor, public methods, cadence and damage source. Rogue application resources remove power/amplifier-cap scaling. A small MobEffectInstance mixin normalizes Bleed to amplifier0 in the canonical seven-argument constructor and copy constructor. All shorter, codec and network construction paths delegate to those. Testing caught that a bare `<init>` selector only affected the first constructor in this environment; explicit signatures cover the actual application and legacy decode paths. Other effects retain their levels. No new effect ID, task queue, entity map or tick listener is introduced.

## Verification

```powershell
python docs/patches/flat-bleed/build.py
python docs/patches/flat-bleed/run_probe.py --candidate
python docs/patches/flat-bleed/run_client.py
# Wait for tmp/flat-bleed/client/tooltip-result.txt.
python docs/patches/flat-bleed/verify.py --candidate
```

The native baseline failed the stationary flat-damage assertion. The final dedicated fixture passed **49 focused Bleed checks**: stationary, vertical-only, slow and fast horizontal movement at amplifiers0/1/11/99; single-level constructors/copies/reapplications; simulated legacy NBT decode at those levels; both Rogue effect applications at power1/40/400; original durations; unaffected Poison level; and the 25-tick cadence. The actual private native impact path applies the node effects; an earlier test incorrectly supplied an impact list to a method that recomputes it from the spell, and was corrected. Client/integrated server passed **43 native tooltip/loading checks** with both final archives.

An attempt to reuse the old broad Warrior fixture stopped at an unrelated outdated Throw expectation (22 actual versus16 expected), before reaching Bleed. No claim that that whole suite passed is made. Focused tests and exact archive comparison prove this overlay preserves all prior classes/resources except the Bleed implementation, two node resources, their descriptions and the mixin registration/addition.

The updated reference is `../rogue-reference/readable.md`, exported to `mod fixing/Rogue skills - raw attributes and nodes.md`. Root README and skill tooltips are synchronized. The handbook only names Bleed without its old scaling formula, so its existing overview and PDF remain accurate; no PDF rebuild is needed.

The verified archives replace repository mods and matching fixed-pack copies, with predecessors retained in ignored `retired packs/flat-bleed-2026-10-24/`. Personal installations, external server and worlds are untouched. Sync/restart matching client/server copies to receive the change. No commit or push requested.
