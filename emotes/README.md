# Bundled emotes

These user-supplied sets install automatically with the client pack:

| Archive | Emotes |
| --- | ---: |
| `SPEmotes Dances 5.zip` | 41 |
| `SPEmotes KSEPSP 2.zip` | 46 |
| `SPEmotes Nightmares 3.zip` | 40 |
| **Total** | **127** |

Keep the ZIPs here; do not commit extracted duplicates. Installer v1.4.3 or newer reads all ZIPs from GitHub main and downloads each separately from the mods. Older installers supporting only one ZIP must be replaced with the latest release.

Each archive must contain flat `.emotecraft` entries, preserving original Unicode filenames. The installer verifies each Git blob hash before extraction and rejects paths, links, and duplicate filenames within an archive. Updating or adding archives requires no further installer rebuild. Only clients install emotes; `--check` reports each pack without downloading ZIPs or creating folders.

The destination is `emotes/Mods Server Pack` beside the selected instance's `mods` folder. Dances keeps its original flat destination and `modpack-state/emotes.json` tracking file for existing installations. Each additional archive uses a subfolder named after its ZIP without the extension, with separate `modpack-state/emotes-<pack>.json` tracking. This keeps identical filenames in different sets independent. The default TLauncher instance is `%APPDATA%\.minecraft`.

Emotecraft reads emotes recursively. Open the wheel with G, then use All Emotes and add favorites to wheel slots. Installing files preserves wheel assignments and Emotecraft configuration.

Missing or changed current pack files are repaired. Replacements and unchanged files retired from an updated archive are backed up under `modpack-backups/emotes-*`; modified retired files and untracked files with other names are preserved. An incoming filename collision is backed up before replacement. Add personal emotes outside `Mods Server Pack`. Removing an entire ZIP from this repository does not automatically delete its previously installed folder.

Validation: all three ZIPs pass CRC checks and contain 127 flat animation files. Installer self-tests cover Unicode extraction, independent packs with colliding filenames, per-pack download skipping and retirement, missing-file repair, personal-file preservation, checksum/path/duplicate rejection, collision backups, and rollback after a failed tracking-file replacement. All 127 animations were also verified in Emotecraft's loaded registry in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 client with Emotecraft 3.4.0-b.build.170 and PAL 1.2.6.
