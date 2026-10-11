# Mod icon assets

This folder holds the verified project icons used beside player-facing mod titles in the handbook. `build-handbook.ps1` adds available icons beside the title, places each description below it, and omits unresolved icons rather than guessing or substituting artwork.

`manifest.csv` records each alphabetical handbook entry's image source, local filename, and match status. Group rows retain the CurseForge page and icon source of their approved representative member. The pack's downloaded mods were sourced from CurseForge, so this handbook uses CurseForge artwork only. The retrieval script accepts exact project-title matches and manually reviewed aliases, verifies the downloaded image format from its file signature, and does not substitute a merely similar search result.

Current catalog snapshot: 163 entries use verified CurseForge project icons, including the seven compact group entries. Each group uses one approved member's official icon as representative artwork; it is not a combined collection logo. Bifrost Teleport has no official icon; `bifrost-teleport.png` is the user's square illustrative artwork, not a project logo.

The project icons remain the property of their respective creators. A project's software license does not necessarily grant reuse rights for its artwork. The manifest links back to each original page for attribution and review; confirm applicable artwork terms before distributing a handbook that reproduces these icons.

To refresh the current catalog after changing mod entries, run from the repository root:

```powershell
.\docs\fetch-mod-icons.ps1
```

CurseForge project metadata is resolved through CFWidget, while image files are fetched from CurseForge's `media.forgecdn.net` host and the manifest retains each official project URL. The refresh script keeps the seven representative mappings from the group's selected member. The local folder is only an asset staging area; it is not a complete mod inventory or a dependency list.
