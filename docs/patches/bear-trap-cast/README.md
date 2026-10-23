# Bear Trap casting crash

The 23 October 2026 report crashed in the client `updateSpellCastAnimationsOnTick`: `cast.animation` was null when reading its speed. The tier-II rework changed Bear Trap from an instant skill to a 0.2-second cast but supplied no casting animation. A dedicated-server probe cannot detect this client-only failure.

The fix adds the existing `spell_engine:one_handed_ground_charge` animation to Bear Trap. Its asset exists in the installed Spell Engine JAR and is already used by the original Rogue/Warrior ground-casting skill. Only `data/rogues/spell/bear_trap.json` changes in the same-filename Rogues JAR; no existing combat settings or other entries change. The canonical Rogue source builder now preserves cast fields and supplies this animation, preventing a rebuild from reintroducing the bug. No Spell Engine-wide workaround is added.

The isolated NeoForge 26.2.0.90 client reproduced the exact null-pointer exception before the fix. With the candidate it passed 40 cast/stop cycles and 120 calls to the actual client animation method at normal/double casting speed. The fixture sets the native client's predicted casting process and calls the method from the crash report; it is not a manual keypress recording. The 2,842-measurement isolated server suite also passed, including native trap placement/activation/damage/rooting and party/pet protection. See `runtime-results.txt`.

```
python docs/patches/bear-trap-cast/build.py
python docs/patches/bear-trap-cast/run_client.py --baseline
python docs/patches/bear-trap-cast/run_client.py --candidate
python docs/patches/rogue-tier-three/run_probe.py --candidates tmp/bear-trap-cast/built
```

Client runs are asynchronous. Check `tmp/bear-trap-cast/client/bear-cast-result.txt` and `launcher-output.log` after the process exits. Its isolated game directory contains a copy of the existing disposable test world; personal Minecraft and server worlds are untouched. `artifacts.json` pins the exact one-entry change and hashes. The old active JAR is retained under ignored `mod fixing/retired packs/bear-trap-cast-2026-10-23/`; the verified replacement is installed in `mods/` and `mod fixing/1 fixed packs/`. Test classes are not shipped. This restores intended behavior, so handbook mechanics and controls do not change. No commit or push was requested.
