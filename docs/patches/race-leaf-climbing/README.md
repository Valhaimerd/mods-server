# Solid leaves and racial leaf climbing

25 October 2026. Same-filename Iourus Races overlay, `iourusraces-1.1.0.jar`. Install matching client/server copies and restart.

## Player behavior

Leaves are solid for every player race, including an unselected race. Nobody receives the old racial leaf pass-through. Elf and Goblin can climb a leaf wall by walking into it, using normal climbing movement. Only side contact with recognized leaves enables this racial ability; stone and other walls do not. Leaves underfoot or overhead do not grant climbing on adjacent stone. Normal ladders and vines remain unchanged.

The pack's general wall-climbing accessory is **Spider Legs** (`nameless_trinkets:spider_legs`) from Nameless Trinkets. Its existing Curios ability remains available to all races, including Elf and Goblin on non-leaf walls. The accessory mod is not modified or made a new required dependency of Iourus.

Recognized foliage includes `minecraft:leaves` and LeavesBlock subclasses. This covers standard and typical modded leaves, including the tested untagged subclass with an otherwise empty collision shape. Custom foliage outside both conventions needs a proper leaves tag to receive this behavior.

## Implementation and verification

The existing shared leaf collision policy now returns solid for every race. A LivingEntity `onClimbable` return hook adds Elf/Goblin side-contact eligibility without overriding vanilla positive results. It checks only the player's immediate horizontal faces after the race/contact guards. No player maps, saved flags, entities, tick listeners or scheduled work are added. All other race logic, including the preceding delayed Elf slow falling, remains byte-identical. Short English/Spanish Elf and Goblin traits/tooltips reflect the new benefit without numerical dumps.

The active baseline failed the universal solid-leaf expectation. The final connected Minecraft 26.2 / NeoForge 26.2.0.90 client and integrated server passed **76 checks**: every enum race's vanilla/custom leaf collision and climb eligibility; no natural stone climbing; actual equipped Spider Legs curio ticks on stone for every race; floor/ceiling/contact restrictions; immediate race changes; native ladders; and five real client movement phases. Elf and Goblin rose about 3.88 blocks on oak leaves, Goblin also climbed the untagged modded leaf fixture, Elf stayed on the ground against stone and Human stayed on the ground against leaves. Every movement phase checked that the wall was not bypassed.

The synchronous fixture changes many walls within one tick. Its native ladder check needed the Entity `inBlockState` cache cleared between scenarios; leaving the previous block cached caused fixture failures. This reset is test-only and is not added to gameplay code. Personal worlds/installations are untouched.

```powershell
python docs/patches/race-leaf-climbing/build.py
python docs/patches/race-leaf-climbing/run_client.py
python docs/patches/race-leaf-climbing/verify.py --installed
```

Hashes and exact entry changes are recorded in `artifacts.json`. The runtime-tested hash matches the installed active/fixed copies. Predecessors are archived under ignored `mod fixing/retired packs/race-leaf-climbing-2026-10-25/`; disposable instances remain in ignored `tmp/race-leaf-climbing/`. Apply this overlay after historical race patches. No push requested.

## Later Orb/synchronization overlay

The [Orb race reset and delayed-sync fix](../race-orb-reset/README.md) preserves these gameplay classes byte-for-byte. Use that patch?s final installed-artifact verifier after applying the overlay.
