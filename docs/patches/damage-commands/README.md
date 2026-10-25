# Live damage commands

25 October 2026. Same-filename overlay for `pvp_flagging-26.2-1.1.2.jar`, preserving the combat flag/HUD/tool restrictions and preceding ownership/party rules.

| Operator command | Effect |
| --- | --- |
| `/pvpdamage <percent>` | Damage retained from players and their RPG summons against players. Default **35**. |
| `/pvedamage <percent>` | Damage retained from players and their RPG summons against non-player entities. Default **80**. |

Use percentages from **0 to 100**, including decimals. `100` restores full damage; `0` prevents that damage. The commands take effect immediately. Run either without a value to show its current percentage. Both require vanilla administrator command permission (normally operator level 3) or the server console. In the console, omit the leading slash.

Examples:

```mcfunction
/pvpdamage 35
/pvedamage 100
/pvpdamage
/pvedamage
```

Both commands include the existing player-owned **Spirit Wolf, Arcane Barrage emitter, Fire Hydra and Frost Elemental**, including damage attributed through their projectiles. The two values are independent; neither is multiplied by the other. Unowned summons and ordinary mob damage are unchanged. Party/team protection remains authoritative even at 100%. A zero-damage hit does not trigger the combat flag. Healing, native defenses and spell coefficients are unchanged.

Minecraft's native saved-world data stores the two percentages under the identifier `pvp_flagging:damage_percentages` at `<world>/dimensions/minecraft/overworld/data/pvp_flagging/damage_percentages.dat` in this Minecraft version. Commands save immediately, and restarting reloads the same settings. Fresh worlds use the preceding 35%/80% defaults. No personal/server config file is shipped or modified. The metadata summary lists the default values; operators may now override them per world.

## Build and verification

```powershell
python docs/patches/damage-commands/build.py
python docs/patches/damage-commands/run_server.py
python docs/patches/damage-commands/verify.py --installed
```

The build overlays exactly the entry class and damage-event class and adds `DamageCommands`. All combat state, HUD, payloads, tool integration, icons and other archive entries remain byte-identical. `artifacts.json` records the required pre-command baseline and final hashes. Prior active/fixed copies are kept under ignored retired packs, and no test classes enter the production archive.

Both the initial and restart runs pass **92 checks** each. The dedicated test dispatches actual Brigadier commands and damages entities through the native pipeline. It checks defaults, queries, administrator restrictions, malformed/out-of-range values, zero/full/decimal percentages, independent PvP/PvE values, all four owned summon types and their arrows, friendly protection, ordinary mob damage, and saved values after an actual server restart. Fixtures use Normal difficulty to avoid vanilla Easy difficulty's separate mob-attack reduction. Tests use disposable repository worlds; personal installations and the separate desktop server remain untouched.

The matching final JAR belongs on clients and server; restart once to load the new commands, then adjustments require no further restart. Handbook source and PDF are updated. No commit or push requested.
