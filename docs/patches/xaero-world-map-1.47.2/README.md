# Xaero?s World Map 1.47.2 update

3 November 2026. Replaced `mods/xaeroworldmap-neoforge-26.2-1.47.1.jar` with the official `xaeroworldmap-neoforge-26.2-1.47.2.jar`.

[Official release](https://modrinth.com/mod/xaeros-world-map/version/neoforge-26.2-1.47.2) ? [Author changelog](https://chocolateminecraft.com/update.php?mod_id=2)

The release is explicitly for Minecraft 26.2 / NeoForge, published 25 October 2026. Its SHA-512 matches the publisher?s Modrinth record; JAR integrity and embedded metadata were checked. Its bundled XaeroLib provides the required library. Better Party and Gateway of Doom map integrations accept this version and register during startup. No Minimap/Better PvP module is installed or added.

The previous JAR is retained under ignored `mod backup/xaero-world-map-2026-11-03/`; only one World Map version remains active. This is a genuine upstream version change, so its filename changes. Pack counts remain 305 main-pack JARs plus 23 client extras.

## Full-pack smoke test

An isolated offline client uses Minecraft 26.2, NeoForge 26.2.0.90, Java 26.0.2 and a 4 GB heap. Every repository main-pack and client-extra JAR is included, without adding a test/probe mod. The exact test hashes are in `tested-manifest.json`.

The test world is a copy of an existing isolated flat test world. Personal worlds, maps, profiles, configuration and the separate desktop server remain untouched. Test files/logs live under ignored `tmp/xaero-update-2026-11-03/client`.

World Map completed loading, both map integrations registered, the player entered the integrated-server world and a new map session initialized. The process became responsive after heavy initial paging with another Minecraft instance open. The user confirmed that launching is sufficient; further Computer Use was stopped by the user. The map screen was not visually verified. This is a successful launch/world-entry smoke test, not a comprehensive gameplay test.

Unrelated log warnings include absent optional Selling Bin quality-food processors, unresolved Dungeons Arise advancements, and an Advanced Loot Info compatibility adapter referring to an unavailable Spell Engine class. Offline test credentials also generate expected authentication/Realms errors. No unrelated JAR has been changed to address these messages.

The 1.47.2 changelog concerns XaeroLib text-rendering compatibility; no controls, catalog entries, icons or documented gameplay mechanics changed. The existing player handbook remains accurate, so no PDF rebuild is required for this patch-level update. No personal installation or GitHub push is included.
