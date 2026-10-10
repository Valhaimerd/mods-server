# Mod icon assets

This folder holds the verified project icons used beside player-facing mod titles in the handbook. `build-handbook.ps1` adds available icons beside the title, places each description below it, and omits unresolved icons rather than guessing or substituting artwork.

`manifest.csv` maps every alphabetical handbook entry to its CurseForge project page, original icon URL, local filename, and match status. The pack's downloaded mods were sourced from CurseForge, so this handbook uses CurseForge artwork only. The retrieval script accepts exact project-title matches and manually reviewed aliases, verifies the downloaded image format from its file signature, and does not substitute a merely similar search result.

Current catalog snapshot: 206 individual mods have verified CurseForge project icons; the seven group overview entries remain image-free. Bifrost Teleport has no official icon; `bifrost-teleport.png` is the user's square illustrative artwork, not a project logo.

The seven group overview rows remain blank by design; do not fill them with an arbitrary member's logo. Bifrost's supplied illustration is already square and is marked as user-provided artwork; never label it as the mod's official icon.

The project icons remain the property of their respective creators. A project's software license does not necessarily grant reuse rights for its artwork. The manifest links back to each original page for attribution and review; confirm applicable artwork terms before distributing a handbook that reproduces these icons.

To refresh the current catalog after changing mod entries, run from the repository root:

```powershell
.\docs\fetch-mod-icons.ps1
```

CurseForge project metadata is resolved through CFWidget, while image files are fetched from CurseForge's `media.forgecdn.net` host and the manifest retains each official project URL. The local folder is only an asset staging area; it is not a complete mod inventory or a dependency list.
