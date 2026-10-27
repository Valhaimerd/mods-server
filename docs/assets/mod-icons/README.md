# Mod icon assets

This folder holds the verified project icons used beside player-facing mod titles in the handbook. `build-handbook.ps1` adds available icons beside the title, places each description below it, and omits unresolved icons rather than guessing or substituting artwork.

`manifest.csv` records each alphabetical handbook entry's image source, local filename, and match status. Group rows retain the official page and icon source of their approved representative member. Existing icons use verified CurseForge artwork; newer ATi, Epic Structures, Stoneholm, Yggdrasil, AmbientSounds, Sound Physics, and Effortless Building: Sophisticated entries use the exact Modrinth project icons. The retrieval script accepts exact project-title matches and manually reviewed aliases, verifies the downloaded image format from its file signature, and does not substitute a merely similar search result.

Current catalog snapshot: 175 entries use verified project icons, including nine compact group entries, plus eight author icons from installed JARs. Each group uses one approved member's official icon as representative artwork; it is not a combined collection logo. Asgardian_Mod (Bifrost Teleport) has no official icon; `bifrost-teleport.png` is the user's square illustrative artwork, not a project logo.

The project icons remain the property of their respective creators. A project's software license does not necessarily grant reuse rights for its artwork. The manifest links back to each original page for attribution and review; confirm applicable artwork terms before distributing a handbook that reproduces these icons.

The featured systems grid reuses those verified icons. Its extra `curios.png` comes from the [Curios API CurseForge project](https://www.curseforge.com/minecraft/mc-mods/curios) ([image source](https://media.forgecdn.net/avatars/thumbnails/195/772/256/256/636881112930494939.png)); Curios is not an alphabetical catalog entry, so it is not in the catalog manifest.

To refresh the current catalog after changing mod entries, run from the repository root:

```powershell
.\docs\fetch-mod-icons.ps1
```

CurseForge project metadata is resolved through CFWidget, while image files are fetched from CurseForge's `media.forgecdn.net` host. Verified Modrinth icons are retained from the manifest when refreshed. The local folder is only an asset staging area; it is not a complete mod inventory or a dependency list.

MrCrayfish's Furniture Mod: Refurbished uses `refurbished_furniture_icon.png` extracted unchanged from the installed 1.0.25 JAR, saved here as `mrcrayfish-s-furniture-mod-refurbished.png`. Its official project is [Refurbished Furniture](https://www.curseforge.com/minecraft/mc-mods/refurbished-furniture). The public project-metadata request timed out, so the verified embedded author icon is retained instead.
