$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path $PSScriptRoot -Parent | Split-Path -Parent
$handbookPath = Join-Path $repositoryRoot 'docs\MODLIST.md'
$manifestPath = Join-Path $repositoryRoot 'docs\assets\mod-icons\manifest.csv'
$handbook = [System.IO.File]::ReadAllText($handbookPath, [System.Text.Encoding]::UTF8)

if ($handbook -match '(?i)Moonstone|Vital Relics') {
    throw 'Removed mods must not remain in the player-facing handbook.'
}
if ($handbook -match '(?i)two sources of active powers') {
    throw 'The handbook must not retain obsolete guidance about the removed relic-power system.'
}

if (-not $handbook.Contains('- **FF Bandage**')) {
    throw 'FF Bandage needs one concise player-facing catalog entry.'
}
if ($handbook -notmatch '(?m)^- \[FF Bandage\]\(#potions-and-alchemy\)$') {
    throw 'FF Bandage must appear in the alphabetical mod index.'
}
if ($handbook -notmatch '\[FFBandage\]\(https://www\.curseforge\.com/minecraft/mc-mods/ffbandage\)') {
    throw 'FF Bandage must link to its official CurseForge project.'
}

$manifest = Import-Csv -LiteralPath $manifestPath
if ($manifest.HandbookEntry -contains 'Moonstone' -or $manifest.HandbookEntry -contains 'Vital Relics') {
    throw 'Removed mods must not remain in the active icon manifest.'
}
$bandage = $manifest | Where-Object HandbookEntry -eq 'FF Bandage' | Select-Object -First 1
if (-not $bandage -or $bandage.Status -ne 'Downloaded' -or $bandage.Platform -ne 'CurseForge') {
    throw 'FF Bandage must have a verified downloaded CurseForge icon.'
}
if (-not (Test-Path -LiteralPath (Join-Path (Split-Path $manifestPath -Parent) $bandage.LocalFile) -PathType Leaf)) {
    throw 'FF Bandage icon manifest entry must point to an existing asset.'
}

'PASS: removed mods are absent and FF Bandage is cataloged, indexed, sourced, and illustrated.'
