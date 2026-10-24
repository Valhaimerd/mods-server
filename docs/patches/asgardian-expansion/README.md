# Asgardian / Bifrost expansion

Local same-version repack for Minecraft 26.2, NeoForge 26.2.0.90+, Java 25 bytecode, completed 24 October 2026. This extends [the travel-only alignment](../bifrost-alignment/README.md) and the [Iourus eighth race](../asgardian-race/README.md). The active filename remains `bifrost-teleport-0.4.3-alpha-neoforge-mc26.2.jar`; the mod ID stays `bifrost` and display name is `Asgardian_Mod`. No push or commit is part of this pass.

The [first-person visibility repair](visibility-fix.md) keeps the beam readable from inside its footprint, with a low inward-facing prismatic rim and particles near passengers. The tall sweep and all transit/capture timing remain unchanged. Its focused client checks include Sodium, More Culling and Vectors.

The [marker occlusion repair](marker-occlusion-fix.md) also prevents the decorative marker from hiding adjacent terrain faces through its model gaps. It retains collision, registry IDs and travel behavior, and includes the beam repair.

The [claim and recipe discovery repair](claim-and-recipes.md) accepts close unobstructed ground hammers without floor-level aiming, reports specific claim failures, and adds native recipe-book discovery for all four existing crafting recipes. The recipes are verified through JEI's visible crafting lookup. The separate [shield pose repair](../shield-pose/README.md) connects component-based kite shields to existing blocking poses.

The [weapon alignment follow-up](../bifrost-weapon-alignment/README.md) supersedes the weapon attributes below: Hofund matches Diamond Sword, Mjolnir matches Netherite Mace, and Stormbreaker matches Netherite Great Hammer with 0.8 attack speed. Native Swift Strikes, Smash and Ground Slam are attached; custom ability inputs are unchanged in that pass.

## Implemented behavior

The [player handbook](../../MODLIST.md#asgardian_mod-bifrost-teleport) explains equipment, percentages, controls and travel. Full approved design: `mod fixing/ASGARDIAN.md` (local working reference).

- Hofund 8 damage / 1.5 speed: charged wave, Realm Shift, Bash/Counter and low-health Berserk.
- Mjolnir 9 / 1.2: finite throw/return, held lightning beam, bounded shard swarm, offhand-shield strike, vortex and Thunder's Respite.
- Stormbreaker 10 / 1.0: exact shaped recipe, finite throw/return, collision-checked Ground Slam, 3D ricochet and Stormforged Recovery.
- Original hammer/axe models and textures; unbound **Asgardian Ability** key opens a scrollable selector with primary/extra skill details. Ordinary entity melee is retained. Air-left or Sneak+entity-left throws the hammer/axe. Right-use charges Hofund or Mjolnir; Stormbreaker opens Travel. Marker use and the selector retain named destinations.
- Server-authoritative identity, owner, packet, action, target and cooldown validation. Existing party members and all player-owned pets are protected. Native hurt, shields, armor, invulnerability and the pack's PvP scaling apply once. Lightning uses a dedicated damage type without vanilla bolts/fire. No terrain damage.
- Haste scales combat cooldowns and windups. Realm/Berserk cannot fall below 75% of base. Pulses, healing timers, travel and throw launch lockout retain their stated fixed timing. Cooldowns are per player and weapon kind, preventing duplicate copies from bypassing a family cooldown. An active weapon action cannot overlap an RPG cast, another weapon action or Bifrost charge.
- Both bound weapons have a persistent UUID/owner authority. Craft results bind before extraction. Legacy items require `/asgard claim`; pickup never silently binds. Inventory/cursor, menu, Q-drop, creative, bundle, hopper, mob/equipment and supported transfer paths enforce authority. Throws reserve the original slot and preserve full stack components. Full inventories retain a protected orbit visual until space exists.
- Downing retains the physical bound weapon but denies use. Final death converts inventory, cursor or in-flight authority to a saved waiting record, excludes it from Better Revive's grave/retained gear, and invalidates late return callbacks. Crouch against the waiting weapon to retrieve it; only its owner can do so. Logout, race loss, respawn and restart do not grant remote recall.
- Transit captures eligible collision boxes once at source impact, tick 14, within the six-block circular footprint and 72-block column. Transfer remains tick 60; target cleanup is tick 100. Source/target descent is 14/25 ticks. Mixed-race players, pets, mobs, boats/minecarts and riders are eligible; late entrants, drops, XP, projectiles, display entities and downed/dead players are excluded. Riding groups get distinct safe destinations within six blocks. Unsafe/protection-vetoed groups remain at source without canceling successful groups. Marker removal/race loss/invalid action cancels cleanly.
- Original translucent prismatic geometry and bounded particles leave no world blocks. Four original packaged Vorbis sounds are registered, subtitled and broadcast once per phase in the correct dimension, within 96 blocks. Captured passengers receive the phase event if positional routing would miss them.

## Ownership administration and safety limits

`/asgard claim` claims an eligible unbound held weapon or nearby targeted protected ground weapon. New crafting binds automatically. Ownership never changes through normal gameplay.

Operator-only recovery, with an online player target:

```text
/asgard recover <owner> <weapon_uuid>
/asgard reassign <new_owner> <weapon_uuid> <previous_owner_uuid>
```

Recovery preserves the UUID, owner, full live components and clocks. Exceptional reassignment requires the exact former owner UUID, changes the canonical owner and invalidates old copies; the command sends an operator audit message. Weapon identity/owner data are in the held stack's custom data and `bifrost:bound_weapons` SavedData. Recovery is an explicit operator exception, not a normal summon.

Death-site placement searches loaded safe footing within 16 blocks without carving or loading a distant area. If none exists, it uses clear fluid-free space in the loaded source column; this can be floating. A pathological fully solid column falls back above build height, logs the coordinates and may require operator recovery. Players receive their saved waiting coordinates.

Rulings: default Stormbreaker throw uses Mjolnir's finite 20-block/135%/0.6s behavior; its selected ricochet has separate 24-block/16s values. Hofund has no unspecified thrown-sword ability. Healing accepts confirmed hostile mob or eligible player damage, excludes pets/allies/training fixtures, and shares a ten-second lockout refreshed by successful healing ticks.

## Build and verification

```powershell
python docs/patches/asgardian-expansion/build.py
python docs/patches/asgardian-expansion/verify.py --candidate
python docs/patches/asgardian-expansion/run_server.py --candidate --revive
# Repeat the revive run to exercise an actual saved-world restart.
python docs/patches/asgardian-expansion/run_server.py --candidate --no-revive
python docs/patches/asgardian-expansion/run_client.py
# Client launch is asynchronous: wait for expansion-client-result.txt before validation.
python docs/patches/asgardian-expansion/verify.py --candidate --runtime
# After installing the tested candidate under the unchanged filename:
python docs/patches/asgardian-expansion/verify.py --runtime
```

`build.py` layers on the pinned travel-alignment archive under ignored `tmp/asgardian-expansion/`, never on a previously expanded archive. `artifacts.json` records before/final hashes, changed archive entries and all production source/resource hashes. The original marker SavedData codec, marker service, commands, block IDs and recipes remain intact. New required dependencies are the already installed Spell Engine, Spell Power and Better Party; revive integrations remain optional. Client and server require matching `asgardian-expansion-v1` networking.

Disposable server/client runners inject probes only into their test copies. Production archives contain no probe classes or test metadata. Test instances, backups, logs and screenshots remain ignored; neither the personal Minecraft profile nor the separate desktop server is changed. The predecessor is archived under `mod fixing/retired packs/asgardian-expansion-2026-10-24/`; the tested production archive is mirrored in `mod fixing/1 fixed packs/`.

See [runtime-results.txt](runtime-results.txt) for final evidence. These are functional tests, not a measurement of final race/class balance. No claim is made for full-pack multiplayer latency, low-FPS stress, every third-party storage mod, every give-up/bleed-out timing combination, or audible human listening. Actual connected-client sound play events and rendered phase screenshots were checked. Further gameplay testing should compare the complete race/class/weapon rotation with equivalent late-game gear.
