# Mods Server Pack

This repository contains the current Minecraft mod collection and a Windows installer that keeps a player's mod folder in sync with it.

See [the player handbook](docs/MODLIST.md) for setup guidance, changed mechanics, controls, exploration, building, and quality-of-life features in the current pack. Libraries, APIs, and compatibility-only JARs are intentionally omitted from its gameplay catalog.

## Installer

Download [ModsServerInstaller.exe](https://github.com/Valhaimerd/mods-server/releases/latest/download/ModsServerInstaller.exe), close Minecraft, and run it.

With no command-line options, use the arrow keys and Enter to choose Client or Server; Esc goes back. The installer does not ask for a typed confirmation or path.

For Client, select TLauncher (the only currently enabled launcher). The installer automatically targets `%APPDATA%\.minecraft\mods` and installs JARs from both repository folders: `mods` and `mod store`. Legacy Launcher, Prism Launcher, and CurseForge App are shown as future options but are not selectable yet.

For Server, choose `Select file location…` and select the server's existing `mods` directory. The folder name must be `mods` (case-insensitive). Server installs use only the repository's `mods` folder; UI/client JARs from `mod store` are excluded.

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

## Build the installer

With the .NET 10 SDK installed:

```powershell
dotnet publish .\installer\ModsServerInstaller.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:EnableCompressionInSingleFile=true -o .\dist
```

The resulting self-contained `dist\ModsServerInstaller.exe` runs on 64-bit Windows without requiring a separate .NET installation.

## Maintaining the handbook

When the pack changes, follow the [player handbook maintenance checklist](docs/HANDBOOK_MAINTENANCE.md) to audit player-facing mods, controls, screenshots, indexes, and distribution counts without adding libraries or APIs to the gameplay catalog.
