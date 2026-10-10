# Installer Client and Server Modes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace typed installer prompts with a branded keyboard-driven client/server chooser, install the correct repository mod set to each target, and publish the updated Windows installer.

**Architecture:** Keep the existing integrity-checked staging, backup, reconciliation, rollback, and handbook-opening pipeline. Add small pure helpers for mode source selection, target validation, and enabled-menu navigation; route no-argument runs through a console wizard and native Windows folder picker while preserving advanced command-line options.

**Tech Stack:** C# on .NET 10 for Windows, `Console.ReadKey`, built-in Windows Forms `FolderBrowserDialog`, embedded `--self-test`, Git/GitHub CLI for push and release.

**Spec:** `docs/superpowers/specs/2026-10-10-installer-client-server-modes-design.md`

## Global Constraints

- Interactive header text is exactly `Minecraft Java RPG Series by Valhaimerd`.
- Client offers exactly four launchers: TLauncher enabled; Legacy Launcher, Prism Launcher, and CurseForge App disabled as future development.
- TLauncher automatically targets `%APPDATA%\.minecraft\mods` and loads JARs from both `mods` and `mod store`.
- Server selection requires an existing directory whose final component is `mods` (case-insensitive), and loads JARs from `mods` only.
- No execution path prompts for typed confirmation; `--yes` remains accepted as a compatibility-only no-op.
- Keep the native picker dependency-free and the output self-contained, single-file `win-x64` executable named `ModsServerInstaller.exe`.
- Preserve integrity verification, backup, rollback, handbook browser behavior, and all existing diagnostic arguments.
- Push source and documentation to `main`, then publish tag/release `installer-v1.2.0` with asset `ModsServerInstaller.exe`.

## Review Focus

- All launcher items disabled or a malformed menu index: navigation must fail clearly rather than loop forever or select a disabled item; test in Task 1.
- Arrow navigation across a disabled entry and menu wraparound: only enabled options can be selected; test in Task 1.
- Server target with casing or trailing separators: `MODS` is valid; any other final component is rejected; test in Task 1.
- Folder-picker cancellation or invalid selection: return to the Server menu or offer retry, and never begin downloads; verify manually in Task 3.
- Advanced arguments and redirected input: `--help`, `--check`, `--target`, and `--self-test` remain usable without a typed prompt; test argument behavior in Task 3.

---

### Task 1: Add pure install rules and self-tests

**Files:**
- Create: `installer/InstallerBehavior.cs`
- Modify: `installer/Program.cs` (`RunSelfTestAsync`)

**Interfaces:**
- Produces `InstallMode` with `Client` and `Server` values.
- Produces `MenuOption(string Label, bool Enabled = true)`.
- Produces `InstallerBehavior.SourceFoldersFor(InstallMode mode) -> IReadOnlyList<string>`.
- Produces `InstallerBehavior.IsModsFolder(string path) -> bool`.
- Produces `InstallerBehavior.FirstEnabledIndex(IReadOnlyList<MenuOption> options) -> int`; it throws `InvalidOperationException` for an empty or all-disabled list.
- Produces `InstallerBehavior.MoveSelection(IReadOnlyList<MenuOption> options, int currentIndex, int direction) -> int`; direction is `-1` or `1`, movement wraps, disabled items are skipped, and a menu with no enabled items throws `InvalidOperationException`.
- Produces `InstallerBehavior.ShouldUseInteractiveWizard(int argumentCount, bool inputRedirected, bool outputRedirected) -> bool`; it is true only for a no-argument launch with an interactive console.

- [x] **Step 1: Add failing self-test assertions**

In `RunSelfTestAsync`, assert Client sources are exactly `mods`, `mod store`; Server sources are exactly `mods`; `...\mods`, `...\MODS\`, and mixed-case `...\Mods` validate; another final directory name and an empty path do not; navigation skips disabled entries in both directions and wraps; a fully disabled menu throws; first selection is the first enabled item; and redirected input or output prevents starting the wizard.

- [x] **Step 2: Run the self-test and confirm it fails on missing behavior**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Expected: compilation fails because the behavior types do not yet exist.

- [x] **Step 3: Implement the pure rules in `installer/InstallerBehavior.cs`**

Use the exact source-folder arrays and navigation semantics from the Interfaces block; normalize trailing directory separators before checking the final path component.

- [x] **Step 4: Run the self-test and confirm all new assertions pass**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Expected: `Self-test passed.`

- [x] **Step 5: Commit the behavior helpers and tests**

Commit message: `feat(installer): define client and server install rules`

### Task 2: Build the keyboard-driven menu and folder picker

**Files:**
- Create: `installer/InteractiveMenu.cs`
- Modify: `installer/ModsServerInstaller.csproj`
- Modify: `installer/Program.cs` (`RunSelfTestAsync`)

**Interfaces:**
- Consumes Task 1's `MenuOption` and `InstallerBehavior.MoveSelection`.
- Produces `MenuAction` with `Continue`, `Submit`, and `Cancel` values; `MenuKeyResult(int SelectedIndex, MenuAction Action)`; and `InteractiveMenu.HandleKey(ConsoleKey key, IReadOnlyList<MenuOption> options, int selectedIndex) -> MenuKeyResult`.
- Produces `InteractiveMenu.Select(string title, IReadOnlyList<MenuOption> options) -> int`; returns the selected enabled item's index or `-1` on Escape.
- Produces `InteractiveMenu.PickFolder(string description, string? initialPath) -> string?`; returns the chosen directory or `null` on cancel. Run the dialog on a dedicated STA thread.

- [x] **Step 1: Add failing self-test assertions for menu key transitions**

Assert Up and Down update the index through Task 1's skip/wrap rules; Enter returns `Submit` without changing the index; Escape returns `Cancel`; and an unrelated key returns `Continue` without changing selection.

- [x] **Step 2: Run `--self-test` and confirm the new assertions fail**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Expected: compilation fails because `MenuKeyResult` and `HandleKey` do not yet exist.

- [x] **Step 3: Implement menu rendering and key handling**

In `InteractiveMenu.cs`, render the exact branded heading, selected-row marker, and grey disabled choices; handle each `Console.ReadKey(intercept: true)` value through the tested `HandleKey` reducer. Never call `Console.ReadLine` for menu selection. Reject an empty/all-disabled menu before reading a key.

- [x] **Step 4: Enable built-in Windows Forms and implement the native picker**

Set the project target to `net10.0-windows` and `<UseWindowsForms>true</UseWindowsForms>`. Use `FolderBrowserDialog` with new-folder creation disabled; execute it from an STA thread and return `null` on cancel.

- [x] **Step 5: Run self-test and build the Windows target**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Run: `dotnet build .\installer\ModsServerInstaller.csproj -c Release`

Expected: self-test passes and the Windows-targeted project builds without new packages.

- [x] **Step 6: Commit the menu and picker**

Commit message: `feat(installer): add keyboard menus and native folder picker`

### Task 3: Route installs through client/server modes and remove typed confirmation

**Files:**
- Modify: `installer/Program.cs`

**Interfaces:**
- Consumes Task 1's `InstallMode`, `SourceFoldersFor`, and `IsModsFolder`; consumes Task 2's `InteractiveMenu.Select` and `PickFolder`.
- Produces an internal `InstallRequest(InstallMode Mode, string Target)` used by the shared package/install pipeline.
- The no-argument wizard offers Client/Server, then TLauncher or `Select file location…`; Escape returns one menu level or exits without mutation. Canceling the server picker returns to the Server menu.

- [x] **Step 1: Add self-tests for compatibility arguments and no-confirmation policy**

Assert `Options.Parse` still accepts `--help`, `--check`, `--target <path>`, `--self-test`, and legacy `--yes`; assert the legacy flag is a no-op by comparing its parsed options with parsing no arguments. Also assert planning a package for a missing `mods` directory reports the JAR as missing without creating the directory.

- [x] **Step 2: Run self-test and confirm the new routing assertions fail**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Expected: new no-confirmation or request-routing assertions fail before implementation.

- [x] **Step 3: Implement the no-argument wizard and request resolution**

Print `Minecraft Java RPG Series by Valhaimerd` and a close-Minecraft reminder. Client's four launcher rows have only TLauncher enabled and use `DefaultModsFolder()`. Server opens the folder picker, validates the selected leaf with `IsModsFolder`, reports invalid selections and allows retry/cancel, and returns to the Server menu if the picker is canceled. Start the picker from the Desktop when it exists, otherwise use the system default location. A no-argument launch with redirected input or output exits with a short message and no file/network changes; skip the final close pause if either stream is redirected. No selection path asks for typed confirmation.

- [x] **Step 4: Pass mode-specific source folders to package loading**

Change `LoadPackageAsync(HttpClient http)` to `LoadPackageAsync(HttpClient http, InstallMode mode)` and enumerate `InstallerBehavior.SourceFoldersFor(mode)`. Keep `BuildPlanAsync`, `ApplyPlanAsync`, backup, rollback, hash verification, and handbook opening shared and unchanged.

- [x] **Step 5: Preserve advanced command-line behavior without text prompts**

Argument-based runs keep their current target resolution and client-package source behavior; `--yes` is parsed as a compatibility-only no-op. Remove the `Confirm()` call and typed `YES` prompt. Keep `--check` read-only and `--self-test` independent of interactive input.

- [x] **Step 6: Run self-tests and focused CLI checks**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --help`

Run: `rg -n 'Confirm\(|type YES' installer`

Expected: self-test passes; help exits without reading input and documents `--yes` as compatibility-only; no typed confirmation implementation remains.

- [x] **Step 7: Manually verify wizard transitions and cancellation on Windows**

Launch the built console with no arguments. Verify header and Client/Server navigation; disabled launchers cannot be selected; TLauncher resolves to `%APPDATA%\.minecraft\mods`; Server opens the native picker; cancel returns to the Server menu without network/install activity; invalid folder reports an error; valid `mods` selection begins install immediately. Use an isolated temporary `mods` directory for the valid-selection check so the test cannot change a real pack.

- [x] **Step 8: Commit the integrated installer flow**

Commit message: `feat(installer): add client and server install wizard`

### Task 4: Update installer documentation

**Files:**
- Modify: `README.md`
- Include in documentation commit: this plan and the approved design spec.

**Interfaces:**
- Documents Task 3's no-argument flow, client/server source split, server target selection, backup behavior, handbook opening, and retained command-line options.

- [x] **Step 1: Update the installation and CLI sections**

Describe arrow-key/Enter selection, the only currently enabled launcher, automatic TLauncher destination, server `mods` picker, source differences, no typed confirmation, and `--yes` compatibility-only status. Preserve build instructions and the latest-download link.

- [x] **Step 2: Review README commands against the implementation**

Run: `rg -n -C 2 'TLauncher|Select file location|mod store|--yes|--check|--target|ModsServerInstaller.exe' README.md`

Expected: no outdated statement claims that all runs install to the default client folder or require typing `YES`.

- [x] **Step 3: Commit the README and design documentation**

Commit message: `docs(installer): describe client and server setup`

### Task 5: Verify, publish, push, and release

**Files:**
- Build artifact: `dist/ModsServerInstaller.exe` (do not add generated `dist` files to Git unless the repository's release process requires it)
- Git release: `installer-v1.2.0`

**Interfaces:**
- Consumes the implementation and documentation from Tasks 1–4.
- Produces a self-contained `win-x64` release asset named exactly `ModsServerInstaller.exe`, source/docs pushed to `main`, and GitHub release `installer-v1.2.0`.

- [x] **Step 1: Run final self-test and publish build**

Run: `dotnet run --project .\installer\ModsServerInstaller.csproj -- --self-test`

Run: `dotnet publish .\installer\ModsServerInstaller.csproj -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:EnableCompressionInSingleFile=true -o .\dist`

Expected: self-test passes and `dist\ModsServerInstaller.exe` exists.

- [x] **Step 2: Run the published executable's self-test and check Git hygiene**

Run: `& .\dist\ModsServerInstaller.exe --self-test`

Run: `git diff --check`

Expected: published binary prints `Self-test passed.` and whitespace checks report no errors.

- [x] **Step 3: Push the completed commits to `origin/main`**

Confirm the current branch is `main`, review `git status --short` and `git log --oneline -5`, then run `git push origin main`.

Expected: push succeeds and the remote branch contains the implementation, README, approved design spec, and plan.

- [x] **Step 4: Publish release `installer-v1.2.0`**

Create the annotated tag and GitHub release `installer-v1.2.0`, attach `dist\ModsServerInstaller.exe` with that exact asset name, and include concise notes about client/server modes and source selection. Verify the release asset is available through the repository's latest-download URL.

- [x] **Step 5: Verify remote release state**

Run: `gh release view installer-v1.2.0 --json tagName,name,assets,url`

Expected: tag is `installer-v1.2.0`, the release contains `ModsServerInstaller.exe`, and the release URL is reported.
