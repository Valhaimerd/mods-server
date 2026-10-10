param(
    [string]$OutputPdf = (Join-Path (Split-Path $PSScriptRoot -Parent | Split-Path -Parent) 'output\pdf\modpack-player-handbook.pdf')
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path $PSScriptRoot -Parent | Split-Path -Parent
$pdfName = 'modpack-player-handbook.pdf'
$rootPdf = Join-Path $repositoryRoot $pdfName
$outputPdf = [System.IO.Path]::GetFullPath($OutputPdf)

foreach ($path in @($rootPdf, $outputPdf)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Expected built handbook PDF at '$path'."
    }
}

$rootHash = (Get-FileHash -LiteralPath $rootPdf -Algorithm SHA256).Hash
$outputHash = (Get-FileHash -LiteralPath $outputPdf -Algorithm SHA256).Hash
if ($rootHash -ne $outputHash) {
    throw 'The repository-root PDF and ignored output PDF are not identical.'
}

Write-Output "PASS: both handbook copies exist and match ($pdfName)."
