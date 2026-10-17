# Bundled emotes

`SPEmotes Dances 5.zip` is the user-supplied pack of 41 `.emotecraft` animation files. Keep the ZIP here; do not commit extracted duplicates. The Windows installer reads this folder from GitHub main and downloads the archive separately from the mods.

Maintain exactly one ZIP with flat `.emotecraft` entries, preserving the original Unicode filenames. The installer verifies the Git blob hash before extraction, rejects paths and duplicate filenames, and installs only for clients. Updating the archive's bytes is sufficient to trigger a client pack update; no installer rebuild is needed for archive-only changes.

The destination is `emotes/Mods Server Pack` beside the selected instance's `mods` folder. The default TLauncher instance is `%APPDATA%\.minecraft`. Emotecraft reads emotes recursively, so these files appear in its menu without manual extraction. Open the wheel with G, then use All Emotes and add favorites to wheel slots. Installing the files does not replace a player's wheel assignments or Emotecraft configuration.

Installed-file hashes are recorded separately in `modpack-state/emotes.json`. Missing or changed current pack files are repaired. Replacements and unchanged files removed from a later pack are backed up; modified retired files and untracked files with other names are preserved. A file sharing an incoming pack filename is backed up before replacement. Add personal emotes outside `Mods Server Pack` so future pack repairs do not replace them.

Verified on 2026-10-17: all 41 extracted files matched the original ZIP bytes and appeared in Emotecraft's loaded emote registry in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 client with Emotecraft 3.4.0-b.build.170 and PAL 1.2.6. The installer's `--self-test` covers Unicode extraction, intact-pack download skipping, missing-file repair, updates and retirement, personal-file preservation, checksum/path/duplicate rejection, filename collision backups, and rollback after a failed tracking-file replacement.
