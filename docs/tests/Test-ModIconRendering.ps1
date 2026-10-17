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
        if (-not $html.Contains('src="' + $src + '"')) { $entry.HandbookEntry }
    }
)
if ($missing.Count -gt 0) {
    throw "Downloaded project icons are not rendered in the handbook HTML: $($missing -join ', ')"
}
$bifrost = $manifest | Where-Object HandbookEntry -eq 'Bifrost Teleport' | Select-Object -First 1
$bifrostSrc = 'assets/mod-icons/' + [uri]::EscapeDataString($bifrost.LocalFile)
if (-not $bifrost -or -not $html.Contains('class="mod-entry-icon" src="' + $bifrostSrc + '"')) {
    throw 'The supplied Bifrost artwork is not rendered beside its mod entry.'
}

$partHeadings = @(
    'id="part-i--featured-mechanics"',
    'id="part-ii--character-and-core-systems"',
    'id="part-v--interface-and-reference"',
    'id="part-vi--keybinds-and-configuration"',
    'id="essential-keybinds-and-conflict-resolution"'
)
$partPositions = @($partHeadings | ForEach-Object { $html.IndexOf($_, [System.StringComparison]::Ordinal) })
if (($partPositions | Where-Object { $_ -lt 0 }).Count -gt 0 -or (($partPositions | Sort-Object) -join ',') -ne ($partPositions -join ',')) {
    throw 'Rendered keybind reference must remain in Part VI after the gameplay chapters.'
}
if ($html -notmatch '(?s)\.keybind-section td:last-child:empty\s*\{[^}]*color:\s*#(?:7[0-9a-f]{2}|8[0-9a-f]{2});[^}]*font-style:\s*italic;') {
    throw 'Empty recommended-key cells must have a muted, italic unbound style.'
}
if ($html -notmatch '(?s)\.keybind-section td:last-child:empty::before\s*\{[^}]*content:\s*["'']Unbound["''];') {
    throw 'Empty recommended-key cells must display the Unbound label.'
}
"PASS: all $(@($manifest | Where-Object Status -eq 'Downloaded').Count) downloaded mod icons render; Part VI keybind order and unbound styles are correct."
