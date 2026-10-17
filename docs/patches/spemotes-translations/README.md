# SPEmotes Translations: NeoForge 26.2 client resources

`mod store/spemotes-translations-neoforge-26.2-1.1.1.jar` wraps the official [SPEmotes Translations v1.1](https://modrinth.com/resourcepack/spemotes-translations/version/1.1) resource pack as an automatically loaded client resource mod. It targets Minecraft 26.2 / NeoForge 26.2.0.90 with Emotecraft 3.4 or newer. It contains no executable classes and belongs only in `mod store`, not the server's `mods` folder. No manual resource-pack selection is needed.

The original language assets and artwork are preserved. The obsolete resource-pack metadata is replaced with format 88 metadata for 26.2 and NeoForge dependencies. Credits and the upstream ARR license attribution are retained. The English file adds 82 local translations for the newer Dances 5 names and descriptions; these additions are maintained in `en_us_additions.json` and are not claimed as upstream translations. Language keys, animation ZIPs, UUIDs, author credits and wheel assignments are unchanged. The official pack's other language files remain available.

Rebuild from the repository root with Python 3:

```powershell
python docs/patches/spemotes-translations/build.py
```

The builder downloads the version-pinned official ZIP, verifies SHA-256 `6d174030ccef4b627a2b88151733f25f5f59969fb4a38d6db2abd2599e5b70b5`, writes a deterministic JAR and checks that all 504 unique translation keys used by the current 253 emotes have English values without Cyrillic text. English labels require an English game language; Russian filenames on disk are intentionally preserved.

Installer v1.4.4 also moves Dances 5 into `emotes/Mods Server Pack/SPEmotes Dances 5`. Its existing `modpack-state/emotes.json` remains the tracking file. On upgrade, only tracked legacy files are moved out of the flat parent folder, with backups (including any edited copies) under `modpack-backups/emotes-*/legacy`; untracked parent files remain. Failed replacements restore both legacy and scoped files. The installer self-test covers migration, repeat runs, user-file preservation and rollback during a locked tracking-file replacement.

Verified on 2026-10-17 in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 client with Emotecraft 3.4.0-b.build.170 and PAL 1.2.6: the JAR was discovered and its resources loaded automatically; all 127 loaded names and descriptions were English; the registry contained exactly the three named pack folders. All emote archive bytes remained unchanged.

Additional sets verified on 2026-10-17: Dances (12), Dances 3 (30) and Nightmares 2 (17). The unchanged translation JAR covers all 370 unique keys across the six sets; the isolated client loaded all 186 animations with English names/descriptions in six named folders.

Further sets verified on 2026-10-17: Dances 2 (15), Dances 4 (37) and Nightmares (15). The isolated client loaded all 253 animations with English names/descriptions in nine named folders. A second Nightmares 2 download had identical extracted files and was kept only as an ignored local backup.
