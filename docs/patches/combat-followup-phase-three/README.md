# Archery follow-up: Frenzy reload and slight assist

> **Superseding assist repair - 4 November 2026:** [Archer-only launch correction](../archer-assist-repair/README.md) replaces the ordinary-arrow steering behavior below. Frenzy changes remain.

Implemented 2 November 2026 as same-filename CrossArms and Spell Engine replacements. Client and server need matching builds and a restart. No config deletion is needed; personal installations and the separate desktop server were not modified.

## Frenzy

The clarified report concerns multiplayer Survival. Two native state gaps were reproduced: an unloaded frame between automatic feeds could start a premature manual crank, and an exhausted crossbow already containing an arrow could never complete a normal crank. The latter also failed with a real connected client on the original build.

Server scores remain authoritative. Held-item hints reconcile ordinary client use prediction; an active partial burst waits for auto-feed instead of starting a crank. An exhausted loaded crossbow temporarily stores its existing projectile in persistent item data while native charging runs. Interrupted charging retains that projectile; a completed crank restores it without consuming another arrow. This also survives changing hands/item-stack copies. Malformed stored data cannot unlock the weapon. Inventory reconciliation broadcasts only when the hint changes or stored ammo is restored.

Shot 26 still exhausts the burst. Three idle seconds reset only partial counters; exhaustion still requires a completed crank. Normal-arrow requirements, Infinity, heat/damage/spread, recoil, durability and the shared player counter remain. The Frenzy iframe hook is byte-identical to the input; it was not responsible for these reload transitions.

## Slight Archer assist

The shared steering allowance was 0.25 degrees per tick. It is now multiplied by actual speed relative to 3 blocks/tick, bounded between 1x and 2x: 0.25 degrees at ordinary bow speed, 0.375 at +50% speed and at most 0.5. The existing 2-degree total correction budget, narrow acquisition cone, visibility, party/pet/Vanish exclusions and one-query acquisition remain unchanged. Rotation preserves velocity magnitude. Eligible ordinary Barrage/Frenzy arrows inherit the shared change; separate Magic Arrows homing is untouched.

The supplied target image is treated as a qualitative near-miss goal, not a calibrated world-distance box.

## Evidence and limits

- Original native code fails the premature-reload regression; the candidate passes 21 focused native checks covering partial/empty/loaded states, interrupted/full charging, saved ammunition, hand changes, unsupported ammunition, malformed state and steering bounds.
- A real NeoForge client connected to an isolated dedicated server reproduced the loaded exhaustion lockup on the original build. All eight candidate assertions pass: rapid repeated input accepts exactly 26 shots in Survival and Creative, normal empty reloads finish, and interrupted/complete loaded recovery preserves ammunition. Survival spends 26 arrows on the burst and one on an ordinary empty reload; recovering an already-loaded arrow spends no extra arrow.
- Native arrows exercised 18 measured trajectories at speeds 3/4.5, distances 12/18/24 and offsets 0.45/0.6/0.75 blocks. Both runs pass 128 bounds/speed checks. Successful near-misses increase from 7/18 to 8/18; large misses remain unassisted. This is a modest improvement, not guaranteed hits.
- 2,529 archive entries outside the declared changes remain byte-identical, including phase-one/two fixes, iframe handling and separate homing profiles. Metadata, filenames and archive integrity are verified.

The network fixture uses loopback rapid input, not a WAN latency/backlog benchmark. No full-pack manual PvP session, reconnect/restart playtest, moving/competing-target visual assessment or separate offhand remote-client run is claimed. Native hand/item-copy checks and persisted item state cover the recovery mechanism; unchanged targeting code retains previous safeguards.

## Reproduce

```powershell
python docs/patches/combat-followup-phase-three/build.py
python docs/patches/combat-followup-phase-three/run_native.py --baseline
python docs/patches/combat-followup-phase-three/run_native.py
python docs/patches/combat-followup-phase-three/run_native.py --arrows --baseline
python docs/patches/combat-followup-phase-three/run_native.py --arrows
python docs/patches/combat-followup-phase-three/run_remote.py --baseline
python docs/patches/combat-followup-phase-three/run_remote.py
python docs/patches/combat-followup-phase-three/verify.py
```

Inputs are pinned under ignored `tmp/combat-followup-phase-three/before`. The remote runner returns after launching the client: wait for its result and process exit before another run. Tests use isolated worlds and port 25578. `install.py` verifies tested candidate hashes and preserves ignored backups before replacing the two repository JARs. No commit or push is included.

Final read-only review found no critical or important defects. The rebuilt handbook remains 80 pages with 469 valid internal links and 51 correct contents destinations. Changed pages 20/26 and the contents were rendered and visually reviewed; both PDF copies match.
