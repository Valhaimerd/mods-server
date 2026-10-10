$ErrorActionPreference = 'Stop'
$builder = Join-Path $PSScriptRoot '..\build-handbook.ps1'
$workingDirectory = Join-Path (Split-Path $PSScriptRoot -Parent | Split-Path -Parent) 'tmp\pdfs\icon-render-test'

& $builder -HtmlOnly -WorkingDirectory $workingDirectory
$htmlPath = Join-Path $workingDirectory 'handbook.html'
$html = [System.IO.File]::ReadAllText($htmlPath, [System.Text.Encoding]::UTF8)
$manifest = Import-Csv (Join-Path $PSScriptRoot '..\assets\mod-icons\manifest.csv')
$missing = @(
    foreach ($entry in $manifest | Where-Object Status -eq 'Downloaded') {
        $src = 'assets/mod-icons/' + [uri]::EscapeDataString($entry.LocalFile)
        if (-not $html.Contains('class="mod-entry-icon" src="' + $src + '"')) { $entry.HandbookEntry }
    }
)
if ($missing.Count -gt 0) {
    throw "Downloaded CurseForge icons are not rendered in the handbook HTML: $($missing -join ', ')"
}
$bifrost = $manifest | Where-Object HandbookEntry -eq 'Bifrost Teleport' | Select-Object -First 1
$bifrostSrc = 'assets/mod-icons/' + [uri]::EscapeDataString($bifrost.LocalFile)
if (-not $bifrost -or -not $html.Contains('class="mod-entry-icon" src="' + $bifrostSrc + '"')) {
    throw 'The supplied Bifrost artwork is not rendered beside its mod entry.'
}

$partOneHeadings = @(
    'id="essential-keybinds-and-conflict-resolution"',
    'id="choosing-a-race"',
    'id="choosing-a-class"',
    'id="skills-abilities-spells-and-runes"',
    'id="first-session-checklist"',
    'id="parties-downed-players-and-death"'
)
$partOnePositions = @($partOneHeadings | ForEach-Object { $html.IndexOf($_, [System.StringComparison]::Ordinal) })
if (($partOnePositions | Where-Object { $_ -lt 0 }).Count -gt 0 -or (($partOnePositions | Sort-Object) -join ',') -ne ($partOnePositions -join ',')) {
    throw 'Rendered Part I must place keybinds before race, class, skills, checklist, and parties.'
}
if (-not $html.Contains('assets/screenshots/02-class-specialization-tree.png') -or $html.Contains('assets/screenshots/02-class-loadout.png')) {
    throw 'The class screenshot must use its specialization-tree filename.'
}
"PASS: all $(@($manifest | Where-Object Status -eq 'Downloaded').Count) downloaded mod icons render; Part I keybind-first order and class screenshot reference are correct."
