$ErrorActionPreference = 'Stop'
$manifestPath = Join-Path $PSScriptRoot '..\assets\mod-icons\manifest.csv'
$rows = Import-Csv -LiteralPath $manifestPath

$downloaded = @($rows | Where-Object Status -eq 'Downloaded')
$nonCurseForge = @($downloaded | Where-Object Platform -ne 'CurseForge')
if ($nonCurseForge.Count -gt 0) {
    $names = ($nonCurseForge.HandbookEntry -join ', ')
    throw "Downloaded mod icons must come from CurseForge. Found other sources for: $names"
}
$invalidSource = @($downloaded | Where-Object {
    $_.ProjectURL -notmatch '^https://www\.curseforge\.com/minecraft/mc-mods/' -or
    $_.IconURL -notmatch '^https://media\.forgecdn\.net/'
})
if ($invalidSource.Count -gt 0) {
    throw "Downloaded icons must retain official CurseForge project links and ForgeCDN artwork URLs: $($invalidSource.HandbookEntry -join ', ')"
}
foreach ($entry in $downloaded) {
    if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $manifestPath -Parent) $entry.LocalFile) -PathType Leaf)) {
        throw "Manifest points to a missing icon file: $($entry.HandbookEntry) ($($entry.LocalFile))"
    }
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
foreach ($name in $groupedEntries) {
    $row = $rows | Where-Object HandbookEntry -eq $name | Select-Object -First 1
    if (-not $row) { throw "Missing expected grouped entry: $name" }
    if ($row.LocalFile -or $row.IconURL) { throw "Grouped entry should remain icon-free: $name" }
}
$missingMemberIcons = @($rows | Where-Object {
    $_.HandbookEntry -notin $groupedEntries -and $_.HandbookEntry -ne 'Bifrost Teleport' -and $_.Status -ne 'Downloaded'
})
if ($missingMemberIcons.Count -gt 0) {
    throw "Individual mods must have verified CurseForge project icons: $($missingMemberIcons.HandbookEntry -join ', ')"
}

$bifrost = $rows | Where-Object HandbookEntry -eq 'Bifrost Teleport' | Select-Object -First 1
if (-not $bifrost -or $bifrost.Platform -ne 'User supplied') {
    throw 'Bifrost Teleport should remain a separate user-art entry.'
}
if ($bifrost.LocalFile -ne 'bifrost-teleport.png' -or $bifrost.Status -ne 'User-provided artwork') {
    throw 'Bifrost Teleport should map to the supplied square artwork.'
}
if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $manifestPath -Parent) $bifrost.LocalFile) -PathType Leaf)) {
    throw 'Bifrost Teleport artwork file is missing.'
}
if ($rows.HandbookEntry -contains 'Locked In Slots' -or $rows.HandbookEntry -contains 'Eating Animation') {
    throw 'Removed mods must not appear in the icon manifest.'
}

"PASS: $($downloaded.Count) downloaded icons are CurseForge-sourced; grouped entries stay icon-free and Bifrost uses supplied artwork."
