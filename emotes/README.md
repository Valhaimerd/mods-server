# Bundled emotes

These user-supplied sets install automatically with the client pack:

| Archive | Emotes |
| --- | ---: |
| `SPEmotes Dances.zip` | 12 |
| `SPEmotes Dances 2.zip` | 15 |
| `SPEmotes Dances 3.zip` | 30 |
| `SPEmotes Dances 4.zip` | 37 |
| `SPEmotes Dances 5.zip` | 41 |
| `SPEmotes KSEPSP 2.zip` | 46 |
| `SPEmotes Nightmares.zip` | 15 |
| `SPEmotes Nightmares 2.zip` | 17 |
| `SPEmotes Nightmares 3.zip` | 40 |
| `SPEmotes Multiverse.zip` | 11 |
| `SPEmotes Multiverse 2.zip` | 26 |
| `SPEmotes Multiverse 3.zip` | 38 |
| `Emote Pack 1.zip` | 24 |
| **Total** | **352** |

Keep the ZIPs here; do not commit extracted duplicates. Installer v1.4.3 or newer reads all ZIPs from GitHub main and downloads each separately from the mods. Older installers supporting only one ZIP must be replaced with the latest release.

Each archive must contain flat `.emotecraft` entries, preserving original Unicode filenames. The installer verifies each Git blob hash before extraction and rejects paths, links, and duplicate filenames within an archive. Updating or adding archives requires no further installer rebuild. Only clients install emotes; `--check` reports each pack without downloading ZIPs or creating folders.

The destination is `emotes/Mods Server Pack` beside the selected instance's `mods` folder. Installer v1.4.5 displays its version on startup and in the menu so stale executables are identifiable. Replace existing downloaded installer copies: v1.4.3 leaves Dances 5 in the parent folder. Versions 1.4.4 and newer give every archive a subfolder named after its ZIP without the extension. Dances 5 retains its `modpack-state/emotes.json` tracking file; other sets use `modpack-state/emotes-<pack>.json`. Tracked legacy Dances files are backed up and removed from the flat parent folder during migration, including edited copies; untracked personal files stay in place. This keeps identical filenames in different sets independent. The default TLauncher instance is `%APPDATA%\.minecraft`.

The client-store SPEmotes Translations JAR loads translations automatically for Minecraft 26.2, including local English additions for Dances 5. Select English in Minecraft for English names and descriptions; original filenames stay unchanged. See [packaging and rebuild notes](../docs/patches/spemotes-translations/README.md).

Emotecraft reads emotes recursively. Open the wheel with G, then use All Emotes and add favorites to wheel slots. Installing files preserves wheel assignments and Emotecraft configuration.

Missing or changed current pack files are repaired. Replacements and unchanged files retired from an updated archive are backed up under `modpack-backups/emotes-*`; modified retired files and untracked files with other names are preserved. An incoming filename collision is backed up before replacement. Add personal emotes outside `Mods Server Pack`. Removing an entire ZIP from this repository does not automatically delete its previously installed folder.

Validation: all thirteen ZIPs pass CRC checks and contain 352 flat animation files, including 75 added Multiverse animations. Installer self-tests cover Unicode extraction, independent packs with colliding filenames, per-pack download skipping and retirement, missing-file repair, personal-file preservation, checksum/path/duplicate rejection, collision backups, and rollback after a failed tracking-file replacement. The original 253 animations were also verified in Emotecraft's loaded registry in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 client with Emotecraft 3.4.0-b.build.170 and PAL 1.2.6.

The three added Multiverse ZIPs pass archive, flat-path and filename checks; their animations have not yet been playtested individually.

The user-supplied Emote Pack 1 adds 24 flat animations. Its archive passes CRC, flat-path and duplicate-filename checks; the individual animations have not been playtested.
