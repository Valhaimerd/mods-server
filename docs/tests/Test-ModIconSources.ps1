$ErrorActionPreference = 'Stop'
$manifestPath = Join-Path $PSScriptRoot '..\assets\mod-icons\manifest.csv'
$rows = Import-Csv -LiteralPath $manifestPath

$downloaded = @($rows | Where-Object Status -eq 'Downloaded')
$unsupported = @($downloaded | Where-Object Platform -notin @('CurseForge', 'Modrinth'))
if ($unsupported.Count -gt 0) {
    throw "Downloaded icons must come from CurseForge or Modrinth: $($unsupported.HandbookEntry -join ', ')"
}
$invalidSource = @($downloaded | Where-Object {
    if ($_.Platform -eq 'CurseForge') {
        $_.ProjectURL -notmatch '^https://www\.curseforge\.com/minecraft/mc-mods/' -or
        $_.IconURL -notmatch '^https://media\.forgecdn\.net/'
    } else {
        $_.ProjectURL -notmatch '^https://modrinth\.com/(mod|datapack)/' -or
        $_.IconURL -notmatch '^https://cdn\.modrinth\.com/data/'
    }
})
if ($invalidSource.Count -gt 0) {
    throw "Downloaded icons must retain official project and artwork URLs: $($invalidSource.HandbookEntry -join ', ')"
}
foreach ($entry in $downloaded) {
    if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $manifestPath -Parent) $entry.LocalFile) -PathType Leaf)) {
        throw "Manifest points to a missing icon file: $($entry.HandbookEntry) ($($entry.LocalFile))"
    }
}

$groupRepresentatives = @{
    'Animal Garden collection' = 'animal-garden-capybara.gif'
    'Awesome Dungeon collection' = 'awesome-dungeon.png'
    'Dungeons and Taverns overhauls' = 'dnt-ancient-city-overhaul.png'
    'Enchantments collection' = 'ice-enchantments.png'
    'Epic Structures collection' = 'epic-structures-collection.webp'
    'Macaw''s building collection' = 'macaw-s-bridges.jpg'
    'Moog''s structure collection' = 'moog-s-bountiful-structures.png'
    'RPG classes' = 'archers.png'
    'When Dungeons Arise collection' = 'when-dungeons-arise-collection.png'
}
$groupedEntries = @($groupRepresentatives.Keys)
foreach ($name in $groupedEntries) {
    $row = $rows | Where-Object HandbookEntry -eq $name | Select-Object -First 1
    if (-not $row) { throw "Missing expected grouped entry: $name" }
    if ($row.LocalFile -ne $groupRepresentatives[$name] -or $row.Status -ne 'Downloaded') {
        throw "Grouped entry should use its approved representative member icon: $name"
    }
}
$missingMemberIcons = @($rows | Where-Object {
    $_.HandbookEntry -notin $groupedEntries -and $_.HandbookEntry -ne 'Asgardian_Mod (Bifrost Teleport)' -and
    $_.Status -ne 'Downloaded' -and $_.Status -notmatch '^Embedded author (logo|icon) from installed JAR$'
})
if ($missingMemberIcons.Count -gt 0) {
    throw "Individual mods must have verified project or embedded author icons: $($missingMemberIcons.HandbookEntry -join ', ')"
}

$bifrost = $rows | Where-Object HandbookEntry -eq 'Asgardian_Mod (Bifrost Teleport)' | Select-Object -First 1
if (-not $bifrost -or $bifrost.Platform -ne 'User supplied') {
    throw 'Asgardian_Mod (Bifrost Teleport) should remain a separate user-art entry.'
}
if ($bifrost.LocalFile -ne 'bifrost-teleport.png' -or $bifrost.Status -ne 'User-provided artwork') {
    throw 'Asgardian_Mod (Bifrost Teleport) should map to the supplied square artwork.'
}
if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $manifestPath -Parent) $bifrost.LocalFile) -PathType Leaf)) {
    throw 'Asgardian_Mod (Bifrost Teleport) artwork file is missing.'
}
if ($rows.HandbookEntry -contains 'Locked In Slots' -or $rows.HandbookEntry -contains 'Eating Animation') {
    throw 'Removed mods must not appear in the icon manifest.'
}

"PASS: $($downloaded.Count) downloaded icons have verified CurseForge or Modrinth sources; grouped entries use approved representative artwork and Bifrost uses supplied artwork."
