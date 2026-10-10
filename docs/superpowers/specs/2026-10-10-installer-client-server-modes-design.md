# Installer Client and Server Modes Design

Date: 2026-10-10
Status: Approved in chat; implementation plan under review

## Purpose

Make the normal installer experience usable without typing paths or confirmation words. Players navigate with the arrow keys and Enter, choose client or server installation, and let the installer reconcile the selected `mods` folder against the appropriate repository content.

The installer remains a self-contained Windows console application. The interactive screens use the exact header `Minecraft Java RPG Series by Valhaimerd`.

## Approved interaction flow

### Common entry

On a normal no-argument launch, display the branded header and an arrow-key menu:

- Client
- Server

Use Up/Down to move, Enter to select, and Esc to exit or return to the previous menu. Show a reminder to close Minecraft before continuing. Do not ask the player to type `YES`, `NO`, a folder path, or another confirmation string.

### Client

Selecting Client opens a four-item launcher menu:

1. TLauncher — enabled
2. Legacy Launcher — disabled and visibly marked as future development
3. Prism Launcher — disabled and visibly marked as future development
4. CurseForge App — disabled and visibly marked as future development

Disabled entries are greyed out and skipped by keyboard navigation; Enter cannot activate them. Selecting TLauncher uses the existing default destination `%APPDATA%\\.minecraft\\mods` without a folder prompt. The client package contains JARs from both repository folders, `mods` and `mod store`.

Once TLauncher is selected, begin the normal client install/reconciliation immediately, without an additional confirmation screen.

### Server

Selecting Server shows a `Select file location…` menu action. Enter opens a native Windows folder picker for the server's existing `mods` directory. The selected directory must have the final path component `mods`, case-insensitively, matching the existing target validation. The picker should start from a useful location when available, but must not require the user to type a path.

After a valid directory is selected, immediately install/reconcile JARs from the repository's `mods` folder only. Never include `mod store` in server installs. Canceling the picker returns to the Server menu without downloading or changing files. An invalid folder selection reports the reason and allows retry or cancellation.

## Shared installation behavior

Both modes reuse the existing integrity-checked staging, Git blob verification, repair, and rollback behavior. Existing JARs replaced by repository files and extra local JARs are moved to timestamped `modpack-backups` directories. Non-JAR files remain untouched. A failed download or install must not leave a partially reconciled target.

Keep the current post-success handbook behavior: open the stable PDF in the default browser after a successful update or when the selected installation is already current. Do not open it after cancellation, check-only execution, or a failed run.

No execution path asks the user to type a confirmation word. Existing diagnostic/automation arguments (`--help`, `--check`, `--target`, and `--self-test`) remain supported. The old `--yes` switch remains accepted as a compatibility-only no-op; it must not reintroduce a prompt.

## Implementation approach

Use `Console.ReadKey` and a small testable menu-navigation model for arrow-key selection and disabled items. Use the .NET Windows Forms `FolderBrowserDialog` as the native folder picker; do not add a third-party TUI or dialog package. Because the shipped app is Windows-only, target the Windows .NET framework needed for this built-in dialog while preserving the self-contained single-file `win-x64` publish.

Represent the chosen install type explicitly and pass it into package loading. Client mode loads `mods` and `mod store`; server mode loads only `mods`. Keep the existing plan/apply logic shared so backup, hash verification, replacement, and rollback stay consistent.

## Non-goals

- Implementing support for the three disabled launchers.
- Changing the mod contents or which JARs are classified into the repository's `mods` and `mod store` folders.
- Redesigning handbook content or publishing a separate HTML handbook site. A static GitHub Pages handbook remains a possible independent follow-up; the installer continues opening the PDF.
- Removing the advanced command-line interface used by scripts and diagnostics.

## Acceptance criteria

1. A no-argument launch displays the exact branded title and Client/Server menu; all navigation is arrow-key/Enter based.
2. TLauncher is the only enabled launcher; the three future launchers are visible, greyed out, and not selectable.
3. TLauncher automatically targets `%APPDATA%\\.minecraft\\mods` and loads client JARs from both source folders.
4. Server mode opens a folder picker, validates that the selected folder is named `mods`, and loads only GitHub's `mods` folder.
5. Selecting a valid server folder begins reconciliation immediately. Canceling makes no file changes; invalid paths do not start installation.
6. Both modes preserve current verification, backup, rollback, and browser-opening behavior.
7. There are no typed YES/NO confirmation prompts in any mode; existing diagnostic/automation arguments continue to work.
8. Automated self-tests cover source-folder selection, folder-name validation, and menu navigation/disabled-item handling.
9. The single-file Windows x64 installer builds, passes self-test, and is published under the stable asset name `ModsServerInstaller.exe` in a new `installer-v1.2.0` GitHub release.

## Release and documentation

Update README usage to describe the new interactive flow and distinguish client and server content sources. Build and test the self-contained Windows x64 executable, push the source and documentation to `main`, then publish `installer-v1.2.0` with the exact release asset name used by the existing latest-download link.
