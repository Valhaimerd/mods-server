# Bifrost / Asgardian travel alignment

**Historical layer:** the active JAR now includes the [full Asgardian expansion](../asgardian-expansion/README.md). This page and its verifier describe the earlier pinned travel-only input; do not rebuild/install it over the full expansion.

Minecraft 26.2, NeoForge 26.2.0.90, Java 25 bytecode. The same-filename local replacement for `bifrost-teleport-0.4.3-alpha-neoforge-mc26.2.jar` extends the existing travel mod. It does not replace its marker saves or implement the proposed combat arsenal.

## Installed behavior

- Displayed mod name: **Asgardian_Mod**. Heimdall's Sword is displayed as **Hofund**; Thor's Hammer consistently uses **Mjolnir**. The `bifrost` namespace, item IDs, recipes, models and saved marker codec are retained.
- Iourus' stored **ASGARDIAN** identity is required for focus use, focus melee attacks, marker placement/interaction/renaming, destination menus, commands and travel/rename/refresh packets. Holding an item never changes race. Non-Asgardians may still possess/trade/craft these items and break markers as ordinary blocks; they cannot activate them or attack with a focus.
- Dead, spectator and downed actors are rejected. PlayerRevive and Better Revive are optional integrations. Changing race or becoming ineligible during charge cancels the transit and removes its tracked visuals.
- Existing beam, particles, sounds and destination validation remain: 60-tick charge, 28/50-tick source/destination descent, 40-tick arrival, five-second cooldown, same-dimension travel. Only the initiating player travels.
- Both sides require the matching Bifrost repack and Asgardian-enabled Iourus repack. Bifrost declares Iourus as a required dependency, validates the Asgardian enum at startup and uses `asgardian-v1` for its payload protocol. An unmodified Iourus JAR does not satisfy the runtime contract despite sharing the upstream version number.

The MD's passenger transport, revised beam/audio effects, ability selector, expanded Hofund/Mjolnir combat, Stormbreaker, owner binding and final-death recovery remain proposals. They are not silently enabled by this travel alignment.

## Build and verification

```powershell
python docs/patches/bifrost-alignment/build.py
python docs/patches/bifrost-alignment/run_server.py --candidate --revive
python docs/patches/bifrost-alignment/run_server.py --candidate --revive # actual restart/save check
python docs/patches/bifrost-alignment/run_server.py --candidate --no-revive
python docs/patches/bifrost-alignment/run_client.py
python docs/patches/bifrost-alignment/verify_contract.py
python docs/patches/bifrost-alignment/verify.py --candidate
```

The original input is pinned by SHA-256 in `artifacts.json` and kept in ignored `tmp/bifrost-alignment/before-*.jar`; the installed predecessor is retained in ignored retired packs. `PatchBifrost.java` inserts server checks into the original native methods, leaving the save codec and unrelated entries byte-for-byte intact. The new shared `AsgardianAccess` source is compiled against the installed pack. Build/probe classpaths use the existing local NeoForge workspaces.

The server runner writes only its disposable instance. Its probe is injected into the **test copy** of Bifrost to access package-private native methods without creating a Java split package. Test classes/metadata are absent from the production candidate. The client runner uses a disposable copied test world and unique offline profile; installed personal worlds/options are not modified. Its launcher reads shared libraries/assets only.

See `runtime-results.txt` for observed results and limits. FakePlayer does not implement normal player teleport networking: the dedicated probe checks native completion/arrival state, while the connected client separately verifies actual coordinates.
