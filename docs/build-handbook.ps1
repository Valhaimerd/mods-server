[CmdletBinding()]
param(
    [string]$Source = (Join-Path $PSScriptRoot 'MODLIST.md'),
    [string]$Stylesheet = (Join-Path $PSScriptRoot 'handbook-print.css'),
    [string]$Output = (Join-Path (Split-Path $PSScriptRoot -Parent) 'output\pdf\modpack-player-handbook.pdf'),
    [string]$WorkingDirectory = (Join-Path (Split-Path $PSScriptRoot -Parent) 'tmp\pdfs'),
    [switch]$HtmlOnly
)

$ErrorActionPreference = 'Stop'

$sourcePath = (Resolve-Path -LiteralPath $Source).Path
$stylesheetPath = (Resolve-Path -LiteralPath $Stylesheet).Path
$outputPath = [System.IO.Path]::GetFullPath($Output)
$repositoryRoot = Split-Path $PSScriptRoot -Parent
$rootPdfPath = Join-Path $repositoryRoot 'modpack-player-handbook.pdf'
$workingPath = [System.IO.Path]::GetFullPath($WorkingDirectory)

$edgeCandidates = @(
    'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe',
    'C:\Program Files\Microsoft\Edge\Application\msedge.exe',
    'C:\Program Files\Google\Chrome\Application\chrome.exe',
    'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'
)
$browserPath = $edgeCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $browserPath) {
    throw 'Microsoft Edge or Google Chrome is required to build the handbook PDF.'
}

New-Item -ItemType Directory -Force -Path $workingPath | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $outputPath -Parent) | Out-Null

$bodyPath = Join-Path $workingPath 'handbook-body.html'
$htmlPath = Join-Path $workingPath 'handbook.html'

& npx.cmd --yes marked@18.1.0 --input $sourcePath --output $bodyPath
if ($LASTEXITCODE -ne 0) {
    throw "Markdown conversion failed with exit code $LASTEXITCODE."
}

$body = [System.IO.File]::ReadAllText($bodyPath, [System.Text.Encoding]::UTF8)
$css = [System.IO.File]::ReadAllText($stylesheetPath, [System.Text.Encoding]::UTF8)
$docsUri = [System.Uri]::new(($PSScriptRoot.TrimEnd('\') + '\')).AbsoluteUri
$iconDirectory = Join-Path $PSScriptRoot 'assets\mod-icons'
$iconManifest = Join-Path $iconDirectory 'manifest.csv'
$catalogIcons = @{}
if (Test-Path -LiteralPath $iconManifest) {
    foreach ($entry in (Import-Csv -LiteralPath $iconManifest)) {
        $name = [System.Net.WebUtility]::HtmlDecode($entry.HandbookEntry)
        $iconFile = ''
        if (-not [string]::IsNullOrWhiteSpace($entry.LocalFile)) {
            $candidate = Join-Path $iconDirectory $entry.LocalFile
            if (Test-Path -LiteralPath $candidate -PathType Leaf) {
                $iconFile = 'assets/mod-icons/' + [uri]::EscapeDataString($entry.LocalFile)
            }
        }
        $catalogIcons[$name] = $iconFile
    }
}

$keybindContentStart = $body.IndexOf('<h3>Essential keybinds and conflict resolution</h3>', [System.StringComparison]::Ordinal)
$keybindContentEnd = $body.IndexOf('<h3>Choosing a race</h3>', $keybindContentStart, [System.StringComparison]::Ordinal)
if ($keybindContentStart -lt 0 -or $keybindContentEnd -lt $keybindContentStart) {
    throw 'Could not locate the keybind section boundaries before formatting catalog icons.'
}

$body = [regex]::Replace(
    $body,
    '<h4>(.*?)</h4>',
    {
        param($match)
        if ($match.Index -gt $keybindContentStart -and $match.Index -lt $keybindContentEnd) { return $match.Value }
        $titleHtml = $match.Groups[1].Value
        $name = [System.Net.WebUtility]::HtmlDecode($titleHtml)
        if (-not $catalogIcons.ContainsKey($name)) { return $match.Value }

        $iconMarkup = ''
        if ($catalogIcons[$name]) {
            $iconMarkup = '<img class="mod-entry-icon" src="' + $catalogIcons[$name] + '" alt="" loading="eager">'
        }
        '<h4 class="mod-entry-heading mod-entry-primary">' + $iconMarkup + '<span class="mod-entry-title">' + $titleHtml + '</span></h4>'
    }
)

$body = [regex]::Replace(
    $body,
    '<p><strong>(.*?)</strong>\s+\u2014\s+(.*?)</p>',
    {
        param($match)
        $nameHtml = $match.Groups[1].Value
        $name = [System.Net.WebUtility]::HtmlDecode($nameHtml)
        if (-not $catalogIcons.ContainsKey($name)) { return $match.Value }

        $iconMarkup = ''
        if ($catalogIcons[$name]) {
            $iconMarkup = '<img class="mod-entry-icon" src="' + $catalogIcons[$name] + '" alt="" loading="eager">'
        }
        '<div class="mod-entry mod-entry-block"><div class="mod-entry-heading">' + $iconMarkup + '<span class="mod-entry-title"><strong>' + $nameHtml + '</strong></span></div><p class="mod-entry-description">' + $match.Groups[2].Value + '</p></div>'
    }
)

$body = [regex]::Replace(
    $body,
    '<li><strong>(.*?)</strong>\s+\u2014\s+(.*?)</li>',
    {
        param($match)
        $nameHtml = $match.Groups[1].Value
        $name = [System.Net.WebUtility]::HtmlDecode($nameHtml)
        if (-not $catalogIcons.ContainsKey($name)) { return $match.Value }

        $iconMarkup = ''
        if ($catalogIcons[$name]) {
            $iconMarkup = '<img class="mod-entry-icon" src="' + $catalogIcons[$name] + '" alt="" loading="eager">'
        }
        '<li class="mod-entry"><div class="mod-entry-heading">' + $iconMarkup + '<span class="mod-entry-title"><strong>' + $nameHtml + '</strong></span></div><p class="mod-entry-description">' + $match.Groups[2].Value + '</p></li>'
    }
)

function Get-HandbookAnchor([string]$text) {
    $decoded = [System.Net.WebUtility]::HtmlDecode($text)
    $slug = [regex]::Replace($decoded.ToLowerInvariant(), '<[^>]+>', '')
    $slug = [regex]::Replace($slug, '[^\p{L}\p{Nd}\s_-]', '')
    [regex]::Replace($slug, '\s', '-')
}

$anchorCounts = @{}
$body = [regex]::Replace(
    $body,
    '<h([1-6])([^>]*)>(.*?)</h\1>',
    {
        param($match)
        $level = $match.Groups[1].Value
        $attributes = $match.Groups[2].Value
        $content = $match.Groups[3].Value
        $baseAnchor = Get-HandbookAnchor $content
        if ($anchorCounts.ContainsKey($baseAnchor)) {
            $anchorCounts[$baseAnchor]++
            $anchor = "$baseAnchor-$($anchorCounts[$baseAnchor])"
        } else {
            $anchorCounts[$baseAnchor] = 0
            $anchor = $baseAnchor
        }
        if ($attributes -match '\bid=') {
            "<h$level$attributes>$content</h$level>"
        } else {
            "<h$level$attributes id=`"$anchor`">$content</h$level>"
        }
    }
)

$keybindHeading = '<h3 id="essential-keybinds-and-conflict-resolution">Essential keybinds and conflict resolution</h3>'
$raceHeading = '<h3 id="choosing-a-race">Choosing a race</h3>'
$partyHeading = '<h3 id="parties-downed-players-and-death">Parties, downed players, and death</h3>'

$contentsHeading = '<h2 id="table-of-contents">Table of Contents</h2>'
$pageBreak = '<div class="page-break" aria-hidden="true"></div>'
$contentsStart = $body.IndexOf($contentsHeading, [System.StringComparison]::Ordinal)
if ($contentsStart -lt 0) { throw 'Could not locate the Table of Contents heading.' }
$contentsEnd = $body.IndexOf($pageBreak, $contentsStart, [System.StringComparison]::Ordinal)
if ($contentsEnd -lt 0) { throw 'Could not locate the page break after the Table of Contents.' }
$body = $body.Insert($contentsStart, '<section class="contents-page">')
$contentsEnd += '<section class="contents-page">'.Length
$body = $body.Insert($contentsEnd, '</section>')

$quickHeading = '<h2 id="quick-reference">Quick Reference</h2>'
$partOneStart = '<div class="page-break" aria-hidden="true"></div><h2 id="part-i--start-here">'
$body = $body.Replace($quickHeading, "$quickHeading<div class=`"quick-reference`">")
$body = $body.Replace($partOneStart, "</div>$partOneStart")

$body = $body.Replace($keybindHeading, "<div class=`"keybind-section`">$keybindHeading")
$body = $body.Replace($raceHeading, "</div>$raceHeading")

$alphabeticalHeading = '<h3 id="alphabetical-mod-index">Alphabetical mod index</h3>'
$gameplayBreak = '<div class="page-break" aria-hidden="true"></div><h3 id="gameplay-tag-index">Gameplay-tag index</h3>'
$body = $body.Replace($alphabeticalHeading, "$alphabeticalHeading<div class=`"alphabetical-index`">")
$body = $body.Replace($gameplayBreak, "</div><div class=`"gameplay-tag-index`"><h3 id=`"gameplay-tag-index`">Gameplay-tag index</h3>")
$body += '</div>'
$alphabeticalStart = $body.IndexOf($alphabeticalHeading, [System.StringComparison]::Ordinal)
$tagStart = $body.IndexOf('<div class="gameplay-tag-index">', [System.StringComparison]::Ordinal)
$tagEnd = $body.LastIndexOf('</div>', [System.StringComparison]::Ordinal)
if ($alphabeticalStart -lt 0 -or $tagStart -lt $alphabeticalStart -or $tagEnd -lt $tagStart) {
    throw 'Could not locate both index sections at the end of the handbook.'
}
$alphabeticalBlock = $body.Substring($alphabeticalStart, $tagStart - $alphabeticalStart)
$tagBlock = $body.Substring($tagStart, $tagEnd + '</div>'.Length - $tagStart)
$precedingPageBreak = $body.LastIndexOf($pageBreak, $alphabeticalStart, [System.StringComparison]::Ordinal)
if ($precedingPageBreak -lt 0 -or $body.Substring($precedingPageBreak + $pageBreak.Length, $alphabeticalStart - ($precedingPageBreak + $pageBreak.Length)).Trim().Length -ne 0) {
    throw 'Could not locate the page break immediately before the indexes.'
}
$body = $body.Remove($precedingPageBreak)
$quickSectionMarker = $pageBreak + $quickHeading
if (-not $body.Contains($quickSectionMarker)) {
    throw 'Could not locate the page break before Quick Reference.'
}
$indexSequence = $pageBreak + $alphabeticalBlock + $pageBreak + $tagBlock + $pageBreak + $quickHeading
$body = $body.Replace($quickSectionMarker, $indexSequence)

$html = @"
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="author" content="Valhaimerd">
  <title>26.2 RPG Series by Valhaimerd</title>
  <base href="$docsUri">
  <style>
$css
  </style>
</head>
<body>
$body
</body>
</html>
"@

[System.IO.File]::WriteAllText($htmlPath, $html, [System.Text.UTF8Encoding]::new($false))

if ($HtmlOnly) {
    Get-Item -LiteralPath $htmlPath | Select-Object FullName, Length, LastWriteTime
    return
}

$htmlUri = [System.Uri]::new($htmlPath).AbsoluteUri
$browserProfilePath = Join-Path $workingPath ("browser-profile-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $browserProfilePath | Out-Null
if (Test-Path -LiteralPath $outputPath) {
    Remove-Item -LiteralPath $outputPath -Force
}
$browserArgs = @(
    '--headless',
    '--disable-gpu',
    '--disable-background-mode',
    '--disable-background-networking',
    '--no-first-run',
    '--allow-file-access-from-files',
    '--run-all-compositor-stages-before-draw',
    "--user-data-dir=$browserProfilePath",
    '--no-pdf-header-footer',
    "--print-to-pdf=$outputPath",
    $htmlUri
)

& $browserPath @browserArgs
$browserExitCode = $LASTEXITCODE
for ($attempt = 0; $attempt -lt 40 -and -not (Test-Path -LiteralPath $outputPath); $attempt++) {
    Start-Sleep -Milliseconds 250
}
if ($browserExitCode -ne 0 -or -not (Test-Path -LiteralPath $outputPath)) {
    throw 'Browser PDF generation failed.'
}

if (-not [string]::Equals($outputPath, $rootPdfPath, [System.StringComparison]::OrdinalIgnoreCase)) {
    Copy-Item -LiteralPath $outputPath -Destination $rootPdfPath -Force
}

Get-Item -LiteralPath $outputPath | Select-Object FullName, Length, LastWriteTime
Get-Item -LiteralPath $rootPdfPath | Select-Object FullName, Length, LastWriteTime
