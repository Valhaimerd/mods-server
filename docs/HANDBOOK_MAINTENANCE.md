# Player Handbook Maintenance

Last verified: 2026-10-09<br>
Environment: Windows PowerShell, Minecraft 26.2, NeoForge 26.2

## Purpose

Use this checklist whenever the pack adds, removes, renames, or substantially reconfigures a mod. It keeps the player handbook aligned with the installed pack without turning it into a raw dependency list.

## Prerequisites and inputs

- Work from the repository root with PowerShell and Git available.
- Know which JARs changed and whether each change is main-pack or optional client-store content.
- Have an official mod page or repository for player-facing behavior and default-key claims.
- Have the current recommended controls available in Minecraft when key assignments change.

## Inclusion decision

Add a canonical handbook entry when a mod exposes meaningful content or behavior during ordinary play: mechanics, progression, creatures, structures, equipment, interfaces, travel, building, storage, or other visible utilities.

Do not add libraries, APIs, loaders, compatibility bridges, performance internals, or server-only plumbing to the gameplay catalog. A support mod may still appear in the complete keybind tables if it exposes a control that players can see. Broad content mods receive one integrated entry with cross-references, not duplicate entries under every feature they contain.

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

5. Update keybinds from the top of Minecraft's Controls screen downward. Keep `Default key` and `Recommended key` separate. Enter a default only when the mod's official documentation states it; otherwise leave that cell blank. Explain intentional context sharing and every resolved conflict.
6. Update screenshots through [the screenshot queue](assets/screenshots/README.md). Use real captures from the current pack, follow the privacy and readability standards, replace the matching `handbook-figure` marker only after the PNG exists, and update its queue status.
7. Update the handbook's `Last audited` date and distribution counts. Treat the baselines below as values that must change deliberately when the pack changes, not as permanent constants.
8. Run the verification checks, review the documentation diff, and stage documentation files explicitly. Do not stage unrelated mod-folder changes.

## Verification

Current verified baseline:

- 277 main-pack JARs;
- 4 optional client-store JARs;
- 156 alphabetical gameplay entries;
- 39 keybind tables;
- 5 handbook parts;
- zero broken internal links.

Check the simple counts:

```powershell
$handbook = Get-Content -LiteralPath '.\docs\MODLIST.md' -Encoding UTF8
($handbook | Where-Object { $_ -match '^## Part [IVX]+ ' }).Count
($handbook | Where-Object { $_ -eq '| Action | Default key | Recommended key | Conflict or notes |' }).Count
(Get-ChildItem -LiteralPath '.\mods' -File -Filter '*.jar').Count
(Get-ChildItem -LiteralPath '.\mod store' -File -Filter '*.jar').Count
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

The script converts `docs/MODLIST.md` to HTML, restores GitHub-style heading anchors for internal links, applies `docs/handbook-print.css`, and prints an A4 PDF with Microsoft Edge or Google Chrome. Its generated working files go under `tmp/pdfs/`, and the finished artifact is `output/pdf/modpack-player-handbook.pdf`. The generated directories remain ignored by Git; commit the Markdown, stylesheet, screenshots, and build script rather than the output copy.

Before distribution, render and inspect every PDF page. Confirm that:

- every screenshot is present, legible, and paired with its caption;
- tables, headings, and images are not clipped or split incorrectly;
- Quick Reference remains on one page;
- the alphabetical and gameplay-tag indexes remain readable;
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
- **Default key is uncertain:** leave the default cell blank; a screenshot or current local assignment is not proof of an official default.
- **Screenshot is unavailable:** retain the invisible capture marker and `Needed` queue status; do not create a broken image link or use unrelated promotional art.
- **Index link is broken:** verify the target heading text and its GitHub-style lowercase hyphenated anchor, then update every index or cross-reference pointing to it.
