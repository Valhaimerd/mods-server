# Exclusive Utilities paths

30 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+. Same-filename overlays for Adventurer Skills and its existing Orb compatibility module. Install matching client/server copies together with the [new Orb recipe and use cost](../orb-acquisition/README.md).

## Player benefits

Choose **Fitness**, **Adaptability** or **Ingenuity** at its entrance. Each has seven ordinary nodes and a mastery requiring all seven. The shared entry gives +4% Stamina. Other combat and Companion paths keep their separate choices. Utilities grants no mining speed, normal movement speed, combat power or item gifts.

| Full path | Benefits | Mastery |
| --- | --- | --- |
| Fitness | +20% Stamina including the entry; +14% food and saturation received; +24% underwater breathing duration | **Emergency Rations:** at three drumsticks or less, restore four. Fixed 180s cooldown; no saturation or direct healing. |
| Adaptability | +10% jump strength; +4 blocks safe fall distance; +24% ladder/vine climbing and sneak speed; 25% slower powder-snow freezing buildup | **Sure Footing:** halve a damaging landing, then resist powder-snow sinking and freezing damage for 5s. Fixed 20s cooldown. Does not cleanse class control effects. |
| Ingenuity | +16% ordinary XP from collected orbs; 16% chance to avoid qualifying tool-use wear; −33% anvil and enchanting XP costs; +1 Luck | **Careful Preparation:** beneficial food/drink effects last 35% longer. Instant, harmful, equipment, class and splash effects are excluded. |

Three cost nodes grant −11% to both payments each, adding to exactly −33%. Enchanting retains the original required level, lapis and offer quality, but pays discounted XP **points**. Hovering an enchanting offer shows that payment. Anvil costs round up to whole levels, at least one for a positive transaction. Normal anvil eligibility and prior-work/material rules remain. Ordinary XP rewards and food bonuses carry fractional gains; they do not round every small reward up. Saturation is independently increased from the original food value and keeps the native hunger cap.

Durability preservation applies once after Unbreaking during actual tool use, including supported picks, axes, shovels, hoes and shears. Armor, shields, elytra and arbitrary scripted durability costs are excluded. Stamina changes Puffish's exhaustion threshold; it is not a separate stamina bar. Breathing extends native air consumption, preserving other respiration/water effects. Climbing is restricted to upward ladder/vine movement; ordinary walking and sprinting are unaffected.

On first login, **only Utilities allocations are refunded**. Category XP, total earned points, combat and Companion allocations remain. The migration also retires old utility leaves/mastery IDs and does not repeat. Mastery cooldowns survive respec, logout and death, advancing while the server runs. Dead, downed and spectator players cannot trigger them. Sure Footing's temporary snow protection clears on death/logout and has its own synchronized icon.

## Implementation

`nodes.py` is the exact allocation contract. `build.py` starts from captured current installed archives, changes only the three category resources in Adventurer Skills, and adds the runtime to the existing compatibility JAR. Seven incoming mastery links and the engine's native `required_skills: 7` enforce completeness on client and server. A narrow server validator additionally prevents switching paths by refunding only an entrance while retaining upper nodes.

Native synchronized attributes own the summed utility stats. Scoped hooks handle food payloads, air, ladder movement, environmental freezing, actual orb XP awards, anvil payments, enchanting debits and tool wear. Duration extension occurs only at native food/potion payload delivery to the consuming entity; unrelated callbacks during consumption are excluded. The existing Companion bridge and previous compatibility behavior remain.

```powershell
python docs/patches/utilities-paths/build.py
python docs/patches/utilities-paths/verify.py tmp/utilities/built/adventurerskills-EN-1.3-26.2-neoforge-fixed.jar
python docs/patches/utilities-paths/run_server.py
python docs/patches/utilities-paths/run_client.py
```

The build uses the existing cached Java/game dependency classpath in ignored `tmp/utilities/classpath.txt`; probes create disposable instances under that directory. Personal installations and worlds are untouched. `artifacts.json`, `runtime-mods.json` and `client-mods.json` record exact archives. `runtime-checks.txt` and the client result describe observed coverage; these are isolated scripted tests, not a full-pack multiplayer soak test. The consumption regression record documents a reproduced exclusion failure before the narrower hook.

## Installed result

The final same-filename archives are installed in `mods/` and ignored `mod fixing/1 fixed packs/`; previous copies are under ignored `mod fixing/retired packs/utilities-and-orb-2026-10-30/`. Passed **68 Utilities runtime checks**, **28 Orb checks**, and connected-client synchronization, XP-payment preview and combined reset checks. The rebuilt80-page handbook has51verified contents destinations; all pages were rendered and reviewed, with enlarged review of changed pages20?21. See [delivery evidence](verification.json). No Git commit or push was requested.
