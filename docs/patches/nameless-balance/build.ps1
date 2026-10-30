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
$compileArguments = @('--release','25','-encoding','UTF-8','-cp',$ClassPath.Trim(),'-d',$classes) + $sources
$argumentFile = Join-Path $work 'compile.args'
[IO.File]::WriteAllLines($argumentFile, @($compileArguments | ForEach-Object { '"' + $_.Replace('\','/') + '"' }), [Text.UTF8Encoding]::new($false))
& (Join-Path $JavaDirectory 'javac.exe') "@$argumentFile"
if ($LASTEXITCODE -ne 0) { throw 'Nameless balance compilation failed.' }
$guardTools = Join-Path $work 'guard-tools'
New-Item -ItemType Directory -Force -Path $guardTools | Out-Null
& (Join-Path $JavaDirectory 'javac.exe') --release 25 -cp $ClassPath -d $guardTools (Join-Path $PSScriptRoot 'tools/GuardDuplicates.java')
if ($LASTEXITCODE -ne 0) { throw 'Duplicate callback guard compilation failed.' }
$env:NAMELESS_GUARD_JAVA = Join-Path $JavaDirectory 'java.exe'
$env:NAMELESS_GUARD_CP = "$classes;$guardTools;$ClassPath"
& python (Join-Path $PSScriptRoot 'repack.py')
if ($LASTEXITCODE -ne 0) { throw 'Nameless balance repack failed.' }
