# Mods Server Pack

This repository contains the current Minecraft mod collection and a Windows installer that keeps a player's mod folder in sync with it.

The current pack requires Minecraft 26.2, NeoForge **26.2.0.90 or newer**, and Java 25 or newer. Install the matching NeoForge version on both client and server before syncing mods; the mod installer updates JARs but does not change the loader.

See [the player handbook](docs/MODLIST.md) for setup guidance, changed mechanics, controls, exploration, building, and quality-of-life features in the current pack. Libraries, APIs, and compatibility-only JARs are intentionally omitted from its gameplay catalog.

## Installer

Download [ModsServerInstaller.exe](https://github.com/Valhaimerd/mods-server/releases/latest/download/ModsServerInstaller.exe), close Minecraft, and run it.

With no command-line options, use the arrow keys and Enter to choose Client or Server; Esc goes back. The installer does not ask for a typed confirmation or path.

For Client, select TLauncher (the only currently enabled launcher), then choose whether to apply Recommended Keybinds. Yes merges the pack's keybind profile into `%APPDATA%\.minecraft\options.txt`, backing up an existing file under `modpack-backups`; No leaves controls untouched. Other settings such as graphics and audio are preserved. Either choice continues the usual install to `%APPDATA%\.minecraft\mods` with JARs from both `mods` and `mod store`. Legacy Launcher, Prism Launcher, and CurseForge App are shown as future options but are not selectable yet.

For Server, choose `Select file location…` and select the server's existing `mods` directory. The folder name must be `mods` (case-insensitive). Server installs use only the repository's `mods` folder; UI/client JARs from `mod store` are excluded.

Advanced Loot Info 2.4.0, ALICompat 1.3.0, and their required Advanced Core Info 1.5.0 are in `mods` for client/server data sharing. Advanced Worldgen Info remains in `mod store` because its worldgen scan previously held the dedicated server thread after startup; its multiplayer information may be limited without server support.

Emotecraft is installed on clients and servers. Borrow Their Emote and its MossyLib dependency, Universal Enchantment Info, Armor Hider, and the local NeoForge Vectors alpha are client-only extras in `mod store`. Configure the new emote, Vectors, and Armor Hider controls in-game; the existing recommended keybind profile does not assign them.

Punchy and the revised Modded Swords x Punchy! resource JAR are client-only extras in `mod store`. The local Punchy 2.8f repair keeps RPG weapons attached to its animated hands and yields to the original Spell Engine skill motions while they play, then resumes Punchy. The resource supplies mappings for the 139 RPG Series weapon definitions. Client installs and repairs set `bettercombatCompat` to `true` in `config/punchy/punchy_config.json`, backing up an existing file under `modpack-backups` and preserving other settings. This also runs when the JARs are already current, independently of the keybind choice. Server installs and `--check` do not change Punchy settings. Remove any old RPG namespace blacklist entries to use these mappings. See the [compatibility implementation and test notes](docs/superpowers/plans/2026-10-16-punchy-modded-swords-26-2.md).

For either mode, the installer compares Git hashes and downloads only missing or changed JARs. Extra and replaced JARs are moved to a timestamped `modpack-backups` folder beside the target `mods` folder. Downloads are verified before the existing installation is changed, and a failed update is rolled back.

After a successful install or repair, the installer opens the [player handbook PDF](https://github.com/Valhaimerd/mods-server/blob/main/modpack-player-handbook.pdf) in the default browser. It also opens the guide when the installation is already current; `--check`, cancellation, and failed updates do not open it.

The executable is not code-signed, so Windows SmartScreen may show an unknown-publisher warning. The source code is available in the `installer` folder.

Useful command-line options:

```text
ModsServerInstaller.exe --check
ModsServerInstaller.exe --target "D:\Launcher\Instance\mods"
ModsServerInstaller.exe --yes
```

`--check` reports what would change without changing files and does not create a missing target folder. `--target` installs the client mod set to another folder named `mods`. `--yes` is retained for compatibility and has no effect; no typed confirmation is used.

The keybind choice appears only in the interactive TLauncher flow. Command-line installs and server installs leave `options.txt` untouched. The embedded profile is curated from the pack's controls; the copied personal `options.txt` is excluded from Git.

## Build the installer

With the .NET 10 SDK installed:

```powershell
dotnet publish .\installer\ModsServerInstaller.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:EnableCompressionInSingleFile=true -o .\dist
```

The resulting self-contained `dist\ModsServerInstaller.exe` runs on 64-bit Windows without requiring a separate .NET installation.

For GitHub releases, create a draft release and push its `installer-v*` tag. The Windows installer workflow builds that tag, runs `--self-test`, uploads `ModsServerInstaller.exe`, and publishes the release as latest. It can also be run manually for an existing release tag.

## Maintaining the handbook

When the pack changes, follow the [player handbook maintenance checklist](docs/HANDBOOK_MAINTENANCE.md) to audit player-facing mods, controls, screenshots, indexes, and distribution counts without adding libraries or APIs to the gameplay catalog.
