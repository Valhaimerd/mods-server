# Player Handbook Maintenance

Last verified: 2026-10-10<br>
Environment: Windows PowerShell, Minecraft 26.2, NeoForge 26.2

## Purpose

Use this checklist whenever the pack adds, removes, renames, or substantially reconfigures a mod. It keeps the player handbook aligned with the installed pack without turning it into a raw dependency list.

## Prerequisites and inputs

- Work from the repository root with PowerShell and Git available.
- Know which JARs changed and whether each change is main-pack or optional client-store content.
- Have the current in-game controls and the verified recommended assignments available.
- Have the current recommended controls available in Minecraft when key assignments change.

## Inclusion decision

Add a canonical handbook entry when a mod exposes meaningful content or behavior during ordinary play: mechanics, progression, creatures, structures, equipment, interfaces, travel, building, storage, or other visible utilities.

Do not add libraries, APIs, loaders, compatibility bridges, performance internals, or server-only plumbing to the gameplay catalog. A support mod may still appear in the complete keybind tables if it exposes a control that players can see. Keep broad mods integrated rather than duplicating them across every feature category. When a group contains separately installed, player-facing modules, retain the image-free group overview and add a distinct titled entry for each installed member with its own verified icon.

Keep the full category-ordered keybind tables together at the beginning of Part I, before race, class, skills, abilities, and first-session onboarding. The print builder preserves this order and keeps the complete keybind block together; do not split or duplicate its tables. Other chapters may mention a shortcut when it helps explain a mechanic, but should describe it in prose rather than repeat it in a table. Keep comparison tables only when they clarify genuinely different workflows; use compact lists or short paragraphs for ordinary mod descriptions.

## Update procedure

1. Inventory the pack and compare it with Git:

   ```powershell
   (Get-ChildItem -LiteralPath '.\mods' -File -Filter '*.jar').Count
   (Get-ChildItem -LiteralPath '.\mod store' -File -Filter '*.jar').Count
   git status --short -- mods 'mod store'
   ```

2. Inspect every added, removed, or renamed JAR. Read its embedded metadata and official page before deciding whether it is player-facing. Do not infer a feature from the filename alone.
3. Classify player-facing additions as a focused entry, integrated expansion, utility, interface feature, or optional client feature. Select the section that best explains the mod's main player use; use gameplay tags for secondary roles.
4. Update all affected handbook locations:

   - one canonical entry in the relevant topic;
   - the alphabetical mod index;
   - only materially useful gameplay-tag links;
   - the official source list for that part;
   - onboarding or mechanic guidance when the change affects what a new player must do.

5. Update keybinds from the top of Minecraft's Controls screen downward. Keep one table per category with only `Action` and `Recommended key`; leave an action's recommendation blank when it should remain unbound. The print stylesheet fixes every keybind table to the same 65/35 column split. Resolve assignments in the actual profile before publishing, but keep default-key and conflict explanations out of the player tables.
6. Update screenshots through [the screenshot queue](assets/screenshots/README.md). Use real captures from the current pack, follow the privacy and readability standards, replace the matching `handbook-figure` marker only after the PNG exists, and update its queue status.
7. Update the handbook's `Last audited` date and distribution counts. Treat the baselines below as values that must change deliberately when the pack changes, not as permanent constants.
8. Run the verification checks, review the documentation diff, and stage documentation files explicitly. Do not stage unrelated mod-folder changes.
9. When the canonical catalog changes, refresh `assets/mod-icons/manifest.csv` and `assets/mod-icons/` with `fetch-mod-icons.ps1`. Use CurseForge artwork only for this pack, verify every project match, and do not substitute a similar result. Keep the seven grouped overview entries image-free, but register each installed member mod as its own player-facing entry with the corresponding project icon. Track Bifrost's user-supplied illustration separately from official project logos. The builder pairs each available verified icon beside its title and puts details below it. Check artwork reuse terms before distributing reproduced icons.

## Verification

Current verified baseline:

- 282 main-pack JARs;
- 4 optional client-store JARs;
- 214 alphabetical gameplay entries;
- 206 verified CurseForge project icons for individual mods plus user-provided Bifrost artwork; seven group overviews remain image-free;
- 41 keybind tables;
- 5 handbook parts;
- zero broken internal links.

Check the simple counts:

```powershell
$handbook = Get-Content -LiteralPath '.\docs\MODLIST.md' -Encoding UTF8
($handbook | Where-Object { $_ -match '^## Part [IVX]+ ' }).Count
($handbook | Where-Object { $_ -eq '| Action | Recommended key |' }).Count
(Get-ChildItem -LiteralPath '.\mods' -File -Filter '*.jar').Count
(Get-ChildItem -LiteralPath '.\mod store' -File -Filter '*.jar').Count
```

Check icon sources and ensure each downloaded icon is emitted into HTML without generating a PDF:

```powershell
& '.\docs\tests\Test-ModIconSources.ps1'
& '.\docs\tests\Test-ModIconRendering.ps1'
```

Check the alphabetical index count, order, and duplicates:

```powershell
$alphaStart = [Array]::IndexOf($handbook, '### Alphabetical mod index')
$tagStart = [Array]::IndexOf($handbook, '### Gameplay-tag index')
$alphaEntries = $handbook[($alphaStart + 1)..($tagStart - 1)] | Where-Object { $_ -match '^- \[' }
$alphaNames = $alphaEntries -replace '^- \[([^]]+)\].*$', '$1'
$alphaEntries.Count
$alphaNames | Group-Object | Where-Object Count -gt 1
Compare-Object $alphaNames ($alphaNames | Sort-Object)
```

The count should match the reviewed catalog, and the last two commands should produce no output. Then run:

```powershell
function Get-HandbookAnchor([string]$text) {
    $slug = $text.ToLowerInvariant()
    $slug = [regex]::Replace($slug, '<[^>]+>', '')
    $slug = [regex]::Replace($slug, '[^\p{L}\p{Nd}\s_-]', '')
    [regex]::Replace($slug, '\s', '-')
}

$anchors = [System.Collections.Generic.HashSet[string]]::new()
$anchorCounts = @{}
foreach ($line in $handbook) {
    if ($line -match '^#{1,6}\s+(.+?)\s*#*$') {
        $base = Get-HandbookAnchor $matches[1]
        if ($anchorCounts.ContainsKey($base)) {
            $anchorCounts[$base]++
            $anchor = "$base-$($anchorCounts[$base])"
        } else {
            $anchorCounts[$base] = 0
            $anchor = $base
        }
        [void]$anchors.Add($anchor)
    }
}

$targets = foreach ($line in $handbook) {
    foreach ($match in [regex]::Matches($line, '\]\(#([^)]+)\)')) {
        [uri]::UnescapeDataString($match.Groups[1].Value)
    }
}
$targets | Sort-Object -Unique | Where-Object { -not $anchors.Contains($_) }
```

The internal-link check should produce no output. Confirm that every queued screenshot is embedded once and that all embedded local images exist:

```powershell
$embeddedScreenshots = $handbook | ForEach-Object {
    if ($_ -match '\(assets/screenshots/([^)]+\.png)\)') { $matches[1] }
}
$manifest = Get-Content -LiteralPath '.\docs\assets\screenshots\README.md' -Encoding UTF8
$queued = $manifest | ForEach-Object {
    if ($_ -match '`(\d{2}-[^`]+\.png)`') { $matches[1] }
}
Compare-Object $embeddedScreenshots $queued

$localImages = $handbook | ForEach-Object {
    foreach ($match in [regex]::Matches($_, '!\[[^]]*\]\(([^)]+)\)')) {
        $match.Groups[1].Value
    }
} | Where-Object { $_ -notmatch '^(https?:)?//' }
$localImages | Where-Object { -not (Test-Path -LiteralPath (Join-Path '.\docs' $_)) }
```

Both checks should produce no output. Finally run:

```powershell
git diff --check
git diff -- docs/MODLIST.md docs/HANDBOOK_MAINTENANCE.md docs/handbook-print.css docs/assets README.md
git status --short
```

Confirm that only intended documentation files are staged before committing:

```powershell
git add -- docs/MODLIST.md docs/HANDBOOK_MAINTENANCE.md docs/handbook-print.css docs/assets README.md
git diff --cached --name-status
```

Expected result: the handbook counts reflect the current reviewed pack, the alphabetical index is ordered and duplicate-free, Markdown has no whitespace errors, screenshot references point to real files, and unrelated workspace changes remain unstaged.

## PDF build and review

Build the distributable handbook from the repository root:

```powershell
.\docs\build-handbook.ps1
```

The script converts `docs/MODLIST.md` to HTML, restores GitHub-style heading anchors for internal links, applies `docs/handbook-print.css`, and prints an A4 PDF with Microsoft Edge or Google Chrome. Its generated working files go under `tmp/pdfs/`; it writes the stable `modpack-player-handbook.pdf` filename to both `output/pdf/` and the repository root. The output copy remains ignored by Git. The root copy is committed so the installer can open GitHub's PDF viewer directly without downloading a separate file.

The visual system uses black body text, forest-green part openers, and amber accents. Preserve that hierarchy and compact rhythm when adjusting the print styles. The Quick Reference keeps control reminders in prose; only the Part I controls chapter carries keybind tables.

The handbook cover title is `26.2 RPG Series by Valhaimerd`. Keep the linked, one-page Table of Contents after the introduction. Its page numbers are maintained in `MODLIST.md`; after any pagination-affecting edit, rebuild the PDF and update those numbers to match the rendered pages. In the PDF, place the alphabetical mod index immediately after the contents, then the Gameplay-tag index with its own title bar, followed by Quick Reference. The source may keep both index sections at the end of `MODLIST.md`; `build-handbook.ps1` moves them into the front matter for print.

Project artwork is staged in `assets/mod-icons/`, with origin URLs and match status in its manifest. The PDF builder renders verified icons beside prominent mod titles, followed by each entry's details, with a slim amber vertical accent and horizontal separator. Image proportions are preserved; unresolved icons are omitted rather than substituted. Confirm artwork terms before public distribution.

Before distribution, render and inspect every PDF page. Confirm that:

- every screenshot is present, legible, and paired with its caption;
- tables, headings, and images are not clipped or split incorrectly;
- Quick Reference remains on one page;
- the alphabetical and gameplay-tag indexes remain readable;
- the one-page Table of Contents has enough row spacing to read comfortably without splitting onto a second page;
- the one-page table of contents has right-aligned page numbers that match each linked destination;
- the alphabetical mod index follows the contents and precedes the separately titled Gameplay-tag index;
- the cover title is `26.2 RPG Series by Valhaimerd`, and the running footer reads `26.2 RPG SERIES BY VALHAIMERD / PLAYER FIELD GUIDE`;
- internal links are clickable and resolve to real headings;
- no blank or nearly blank page was introduced unintentionally.

## Recovery

- Before committing, use `git diff -- <path>` to isolate and correct an accidental documentation edit.
- After committing, prefer a new corrective commit so the history records what changed.
- If a documentation update must be abandoned, restore only the named documentation paths; never use a broad reset or restore against `mods`, `mod store`, or the repository root.
- If a removed mod still appears in several places, search its display name and known aliases with `rg -n '<name-or-alias>' docs README.md`, then remove or redirect every player-facing reference.

## Troubleshooting

- **Count changed unexpectedly:** inspect `git status --short -- mods 'mod store'` and check for duplicate filenames before editing the header.
- **One JAR covers many systems:** give it one integrated entry in its strongest topic and add selective gameplay tags rather than copying its description.
- **Recommended key is undecided:** leave the cell blank until the control is intentionally assigned or confirmed unbound.
- **Screenshot is unavailable:** retain the invisible capture marker and `Needed` queue status; do not create a broken image link or use unrelated promotional art.
- **Index link is broken:** verify the target heading text and its GitHub-style lowercase hyphenated anchor, then update every index or cross-reference pointing to it.
