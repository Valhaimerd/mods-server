param(
    [Parameter(Mandatory)][string]$ClassPath,
    [string]$JavaDirectory = 'C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin'
)
$ErrorActionPreference = 'Stop'
$repository = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$work = Join-Path $repository 'tmp/nameless-balance'
$classes = Join-Path $work 'classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName)
& (Join-Path $JavaDirectory 'javac.exe') --release 25 -cp $ClassPath -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Nameless balance compilation failed.' }
& python (Join-Path $PSScriptRoot 'repack.py')
if ($LASTEXITCODE -ne 0) { throw 'Nameless balance repack failed.' }
