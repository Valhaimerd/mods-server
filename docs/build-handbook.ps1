[CmdletBinding()]
param(
    [string]$Source = (Join-Path $PSScriptRoot 'MODLIST.md'),
    [string]$Stylesheet = (Join-Path $PSScriptRoot 'handbook-print.css'),
    [string]$Output = (Join-Path (Split-Path $PSScriptRoot -Parent) 'output\pdf\modpack-player-handbook.pdf'),
    [string]$WorkingDirectory = (Join-Path (Split-Path $PSScriptRoot -Parent) 'tmp\pdfs')
)

$ErrorActionPreference = 'Stop'

$sourcePath = (Resolve-Path -LiteralPath $Source).Path
$stylesheetPath = (Resolve-Path -LiteralPath $Stylesheet).Path
$outputPath = [System.IO.Path]::GetFullPath($Output)
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

& npx.cmd --yes marked --input $sourcePath --output $bodyPath
if ($LASTEXITCODE -ne 0) {
    throw "Markdown conversion failed with exit code $LASTEXITCODE."
}

$body = [System.IO.File]::ReadAllText($bodyPath, [System.Text.Encoding]::UTF8)
$css = [System.IO.File]::ReadAllText($stylesheetPath, [System.Text.Encoding]::UTF8)
$docsUri = [System.Uri]::new(($PSScriptRoot.TrimEnd('\') + '\')).AbsoluteUri

function Get-HandbookAnchor([string]$text) {
    $decoded = [System.Net.WebUtility]::HtmlDecode($text)
    $slug = [regex]::Replace($decoded.ToLowerInvariant(), '<[^>]+>', '')
    $slug = [regex]::Replace($slug, '[^\p{L}\p{Nd}\s_-]', '')
    [regex]::Replace($slug, '\s', '-')
}

$anchorCounts = @{}
$body = [regex]::Replace(
    $body,
    '<h([1-6])>(.*?)</h\1>',
    {
        param($match)
        $level = $match.Groups[1].Value
        $content = $match.Groups[2].Value
        $baseAnchor = Get-HandbookAnchor $content
        if ($anchorCounts.ContainsKey($baseAnchor)) {
            $anchorCounts[$baseAnchor]++
            $anchor = "$baseAnchor-$($anchorCounts[$baseAnchor])"
        } else {
            $anchorCounts[$baseAnchor] = 0
            $anchor = $baseAnchor
        }
        "<h$level id=`"$anchor`">$content</h$level>"
    }
)

$quickHeading = '<h2 id="quick-reference">Quick Reference</h2>'
$partOneStart = '<div class="page-break" aria-hidden="true"></div><h2 id="part-i--start-here">'
$body = $body.Replace($quickHeading, "$quickHeading<div class=`"quick-reference`">")
$body = $body.Replace($partOneStart, "</div>$partOneStart")

$alphabeticalHeading = '<h3 id="alphabetical-mod-index">Alphabetical mod index</h3>'
$gameplayBreak = '<div class="page-break" aria-hidden="true"></div><h3 id="gameplay-tag-index">Gameplay-tag index</h3>'
$body = $body.Replace($alphabeticalHeading, "$alphabeticalHeading<div class=`"alphabetical-index`">")
$body = $body.Replace($gameplayBreak, "</div>$gameplayBreak<div class=`"gameplay-tag-index`">")
$body += '</div>'

$html = @"
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Modpack Player Handbook</title>
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

Get-Item -LiteralPath $outputPath | Select-Object FullName, Length, LastWriteTime
