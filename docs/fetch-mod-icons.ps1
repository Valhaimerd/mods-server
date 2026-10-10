[CmdletBinding()]
param(
    [string]$Source = (Join-Path $PSScriptRoot 'MODLIST.md'),
    [string]$AssetDirectory = (Join-Path $PSScriptRoot 'assets\mod-icons'),
    [string]$Manifest = (Join-Path $PSScriptRoot 'assets\mod-icons\manifest.csv'),
    [switch]$RefreshExisting
)

$ErrorActionPreference = 'Stop'
$sourceText = [System.IO.File]::ReadAllText((Resolve-Path -LiteralPath $Source).Path, [System.Text.Encoding]::UTF8)
$index = [regex]::Match($sourceText, '(?s)### Alphabetical mod index\r?\n(.*?)(?=\r?\n<div class="page-break"|\r?\n### Gameplay-tag index)')
if (-not $index.Success) { throw 'Could not find the alphabetical mod index.' }

$names = [regex]::Matches($index.Groups[1].Value, '^\- \[([^]]+)\]', 'Multiline') | ForEach-Object { $_.Groups[1].Value }
New-Item -ItemType Directory -Force -Path $AssetDirectory | Out-Null
$headers = @{ 'User-Agent' = '26.2-RPG-Series-Handbook/1.0 (player-facing icon index)' }
$manifestRows = [System.Collections.Generic.List[object]]::new()
$previousRows = @{}
if (Test-Path -LiteralPath $Manifest) {
    foreach ($previous in (Import-Csv -LiteralPath $Manifest)) { $previousRows[$previous.HandbookEntry] = $previous }
}

# Verified project IDs/slugs for entries whose CurseForge project name or URL does not
# follow the handbook title. Other projects are resolved by their title slug and then
# checked against the returned CurseForge title before an image is accepted.
$approvedCurseForgeProjects = @{
    'Alchemia' = '1684783'
    "Alex's Caves" = '924854'
    "Alex's Mobs" = '426558'
    'Armored Foes' = '1369926'
    'Armory' = '1311561'
    'Arsenal' = '1230054'
    'ATi Structures: Vanilla Edition' = '1158208'
    'Animal Pen' = '1409534'
    'Better Nether' = '1422293'
    'Blood Moon' = '1555594'
    'Dungeons and Taverns' = '853794'
    'Easy Magic' = '456239'
    'Enchanting Infuser' = '551151'
    "Explorer's Compass" = '491794'
    "Farmer's Delight" = '398521'
    'Food Effect Tooltips' = '776426'
    'Functional Sculptures' = '1617223'
    'Invasion' = '1628663'
    'Iris' = '455508'
    'Iron Wolf Armor' = '963404'
    'JEI Trades' = '1561282'
    'Just Enough Items' = '238222'
    'Kingdom Cats Replacer' = '1629615'
    'Awesome Dungeon' = '530465'
    'Awesome Dungeon End Edition' = '537531'
    'Awesome Dungeon Ocean Edition' = '556490'
    'DnT Mineshaft Overhaul' = '1494838'
    'Ly-Fangs Enchantment' = '1388436'
    'Thunder Strike Enchantment' = '1388424'
    'Macaw''s Fences and Walls' = '453925'
    'Macaw''s Lights and Lamps' = '502372'
    'Macaw''s Paintings' = '438116'
    'Moog''s Nether Structures' = '967466'
    'Moog''s Ocean Structures' = '1628830'
    'Moog''s Temples Reimagined' = '1339737'
    'Paladins & Priests' = '856548'
    'Rogues & Warriors' = '1048409'
    "Lullaby's Mobs" = '1290652'
    'Market Board' = '1467970'
    'Mounts and Monsters' = '1401501'
    "Nature's Compass" = '252848'
    "Pufferfish's Skills" = '835091'
    'Relics' = '1204049'
    'Shroomcraft' = '1371092'
    'Skill Tree (RPG Series)' = '1311513'
    'The Graveyard' = '1568170'
    "Tom's Simple Storage" = '378609'
    "Xaero's Map Multiplayer" = '1525919'
    'Wild Pets' = '1679434'
}
$approvedCurseForgeSlugs = @{
    'Awesome Dungeon' = 'awesome-dungeon-neoforge'
    'Awesome Dungeon End Edition' = 'awesome-dungeon-the-end-neoforge'
    'Awesome Dungeon Ocean Edition' = 'awesome-dungeon-ocean-neoforge'
    'DnT Mineshaft Overhaul' = 'dungeons-and-taverns-mineshaft-overhaul'
    'Iron Wolf Armor' = 'iron-wolf-armor'
    'Ly-Fangs Enchantment' = 'ly-fangs-enchantment'
    'Macaw''s Bridges' = 'macaws-bridges'
    'Macaw''s Doors' = 'macaws-doors'
    'Macaw''s Fences and Walls' = 'macaws-fences-and-walls'
    'Macaw''s Furniture' = 'macaws-furniture'
    'Macaw''s Holidays' = 'macaws-holidays'
    'Macaw''s Lights and Lamps' = 'macaws-lights-and-lamps'
    'Macaw''s Paintings' = 'macaws-paintings'
    'Macaw''s Paths and Pavings' = 'macaws-paths-and-pavings'
    'Macaw''s Roofs' = 'macaws-roofs'
    'Macaw''s Stairs' = 'macaws-stairs'
    'Macaw''s Trapdoors' = 'macaws-trapdoors'
    'Macaw''s Windows' = 'macaws-windows'
    'Moog''s Bountiful Structures' = 'mbs-moogs-bountiful-structures'
    'Moog''s End Structures' = 'moogs-end-structures'
    'Moog''s Nether Structures' = 'mns-moogs-nether-structures'
    'Moog''s Ocean Structures' = 'mos-moogs-ocean-structures'
    'Moog''s Temples Reimagined' = 'mtr-moogs-temples-reimagined'
    'Moog''s Voyager Structures' = 'moogs-voyager-structures'
    'Paladins & Priests' = 'paladins-and-priests'
    'Rogues & Warriors' = 'rogues-and-warriors'
    'Runes' = 'runes'
    'Xaero''s Map Multiplayer' = 'xaeros-maps-multiplayer-plus'
    'Better Nether' = 'betternether-neoforge'
    'Dungeons and Taverns' = 'dungeon-and-taverns'
    'Functional Sculptures' = 'functional-statues'
    'Kingdom Cats Replacer' = 'cute-cats'
    'Mounts and Monsters' = 'mounts-monsters'
    'Shroomcraft' = 'new-shroomcraft'
    'The Graveyard' = 'the-graveyard-unofficial-port'
    'Xaero''s World Map' = 'xaeros-world-map'
    "Explorer's Compass" = 'explorers-compass'
    "Farmer's Delight" = 'farmers-delight'
    "Nature's Compass" = 'natures-compass'
    "Tom's Simple Storage" = 'toms-storage'
    'Just Enough Items' = 'jei'
}
$approvedTitleExceptions = @{
    'Awesome Dungeon End Edition' = 'awesomedungeontheendeditionneoforge'
    'DnT Mineshaft Overhaul' = 'dungeonsandtavernsmineshaftoverhaul'
    'Ly-Fangs Enchantment' = 'fangsenchantmentfabricforgeneoforgequilt'
    'Moog''s Bountiful Structures' = 'mbsmoogsbountifulstructures'
    'Moog''s End Structures' = 'mesmoogsendstructures'
    'Moog''s Nether Structures' = 'mnsmoogsnetherstructures'
    'Moog''s Ocean Structures' = 'mosmoogsoceanstructures'
    'Moog''s Temples Reimagined' = 'mtrmoogstemplesreimagined'
    'Moog''s Voyager Structures' = 'mvsmoogsvoyagerstructures'
    'Functional Sculptures' = 'functionalstatues'
    'Kingdom Cats Replacer' = 'cutecats'
    'Mounts and Monsters' = 'mountsmonsters'
    'JEI Trades' = 'jeireitrades'
    'Market Board' = 'themarketboard'
    'Invasion' = 'invasionmodfork'
    'Runes' = 'enchantingrunes'
    'Xaero''s Map Multiplayer' = 'xaerosmapsmultiplayer'
}
$groupedEntries = @(
    'Animal Garden collection',
    'Awesome Dungeon collection',
    'Dungeons and Taverns overhauls',
    'Enchantments collection',
    'Macaw''s building collection',
    'Moog''s structure collection',
    'RPG classes'
)

function Normalize-ProjectTitle([string]$value) {
    return [regex]::Replace($value.ToLowerInvariant(), '[^a-z0-9]', '')
}

function Get-ActualImageExtension([string]$Path) {
    $bytes = [System.IO.File]::ReadAllBytes($Path)
    if ($bytes.Length -ge 8 -and $bytes[0] -eq 137 -and $bytes[1] -eq 80 -and $bytes[2] -eq 78 -and $bytes[3] -eq 71) { return '.png' }
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 255 -and $bytes[1] -eq 216 -and $bytes[2] -eq 255) { return '.jpg' }
    if ($bytes.Length -ge 12 -and [System.Text.Encoding]::ASCII.GetString($bytes, 0, 4) -eq 'RIFF' -and [System.Text.Encoding]::ASCII.GetString($bytes, 8, 4) -eq 'WEBP') { return '.webp' }
    if ($bytes.Length -ge 6 -and [System.Text.Encoding]::ASCII.GetString($bytes, 0, 3) -eq 'GIF') { return '.gif' }
    if ($bytes.Length -gt 0) {
        $prefix = [System.Text.Encoding]::UTF8.GetString($bytes, 0, [Math]::Min(256, $bytes.Length)).TrimStart([char]0xFEFF, [char]0x20, [char]0x09, [char]0x0A, [char]0x0D)
        if ($prefix -match '^(<\?xml[^>]*>\s*)?<svg\b') { return '.svg' }
    }
    throw "Downloaded file is not a recognized image: $Path"
}

foreach ($name in $names) {
    $row = [ordered]@{
        HandbookEntry = $name
        ProjectTitle = ''
        Platform = ''
        ProjectURL = ''
        IconURL = ''
        LocalFile = ''
        Status = 'Needs source match'
        CheckedAt = (Get-Date -Format 'yyyy-MM-dd')
    }

    if ($name -in $groupedEntries) {
        $manifestRows.Add([pscustomobject]$row)
        continue
    }

    if ($name -eq 'Bifrost Teleport') {
        $artworkFile = 'bifrost-teleport.png'
        $artworkPath = Join-Path $AssetDirectory $artworkFile
        $row.Platform = 'User supplied'
        $row.ProjectTitle = 'Illustrative artwork; no official mod icon'
        if (Test-Path -LiteralPath $artworkPath -PathType Leaf) {
            $row.LocalFile = $artworkFile
            $row.Status = 'User-provided artwork'
        } else {
            $row.Status = 'Custom artwork pending'
        }
        $manifestRows.Add([pscustomobject]$row)
        continue
    }

    if (-not $RefreshExisting -and $previousRows.ContainsKey($name)) {
        $existing = $previousRows[$name]
        $existingFile = Join-Path $AssetDirectory $existing.LocalFile
        if ($existing.Platform -eq 'CurseForge' -and $existing.Status -eq 'Downloaded' -and
            $existing.IconURL -match '^https://media\.forgecdn\.net/' -and
            (Test-Path -LiteralPath $existingFile -PathType Leaf) -and
            ([System.IO.Path]::GetExtension($existingFile) -eq (Get-ActualImageExtension $existingFile))) {
            $existing.CheckedAt = Get-Date -Format 'yyyy-MM-dd'
            $manifestRows.Add($existing)
            continue
        }
    }

    $candidates = [System.Collections.Generic.List[string]]::new()
    if ($approvedCurseForgeProjects.ContainsKey($name)) { $candidates.Add($approvedCurseForgeProjects[$name]) }
    if ($approvedCurseForgeSlugs.ContainsKey($name)) { $candidates.Add($approvedCurseForgeSlugs[$name]) }
    if ($previousRows.ContainsKey($name)) {
        $old = $previousRows[$name]
        if ($old.ProjectURL -match '/mc-mods/([^/?#]+)') { $candidates.Add($Matches[1]) }
    }
    $candidates.Add([regex]::Replace($name.ToLowerInvariant(), '[^a-z0-9]+', '-').Trim('-'))
    $candidates = @($candidates | Where-Object { $_ } | Select-Object -Unique)

    $expected = Normalize-ProjectTitle $name
    $project = $null
    foreach ($candidate in $candidates) {
        $endpoint = if ($candidate -match '^\d+$') {
            "https://api.cfwidget.com/$candidate"
        } else {
            "https://api.cfwidget.com/minecraft/mc-mods/$candidate"
        }
        try {
            $found = Invoke-RestMethod -Uri $endpoint -Headers $headers -TimeoutSec 20
            $candidateTitle = Normalize-ProjectTitle $found.title
            $approvedTitle = $approvedTitleExceptions.ContainsKey($name) -and $candidateTitle -eq $approvedTitleExceptions[$name]
            if ($approvedTitle -or $candidateTitle -eq $expected -or $candidateTitle.StartsWith($expected)) {
                $project = $found
                break
            }
        } catch {
            if ($_.Exception.Message -notmatch '\(404\)') {
                $row.Status = 'Lookup error: ' + $_.Exception.Message
            }
        }
        Start-Sleep -Milliseconds 350
    }

    if ($project) {
        $row.Platform = 'CurseForge'
        $row.ProjectTitle = $project.title
        $row.ProjectURL = $project.urls.curseforge
        $row.IconURL = $project.thumbnail
        if (-not $row.IconURL) {
            $row.Status = 'Matched project; no icon published'
        } else {
            $iconUri = [uri]$row.IconURL
            $baseName = [regex]::Replace($name.ToLowerInvariant(), '[^a-z0-9]+', '-').Trim('-')
            $downloadPath = Join-Path $AssetDirectory ($baseName + '.download')
            Invoke-WebRequest -Uri $row.IconURL -Headers $headers -OutFile $downloadPath -TimeoutSec 20
            $extension = Get-ActualImageExtension $downloadPath
            $fileName = $baseName + $extension
            Move-Item -LiteralPath $downloadPath -Destination (Join-Path $AssetDirectory $fileName) -Force
            $row.LocalFile = $fileName
            $row.Status = 'Downloaded'
        }
    }
    $manifestRows.Add([pscustomobject]$row)
}

$manifestDirectory = Split-Path -Parent $Manifest
New-Item -ItemType Directory -Force -Path $manifestDirectory | Out-Null
$manifestRows | Export-Csv -LiteralPath $Manifest -NoTypeInformation -Encoding UTF8
$manifestRows | Group-Object Status | Sort-Object Name | ForEach-Object { '{0}: {1}' -f $_.Name, $_.Count }
"Manifest: $Manifest"
"Icons: $AssetDirectory"
