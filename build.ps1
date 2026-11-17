# Build TeamBooks against deobfuscated Minecraft 26.3 without Loom.
# (Loom requires Mojang mappings, which are not published for the deobfuscated 26.3 jar.)
# Compiled against the extracted deobfuscated server jar + Fabric API + dependencies.

$ErrorActionPreference = 'Stop'

$root = $PSScriptRoot
$cpDir = 'D:\TeamOptimization\build\cp'
$serverJar = 'C:\Users\EGIR_FoXoff\.gradle\caches\fabric-loom\26.3\minecraft-extracted_server.jar'
$equkinsLibs = 'D:\TeamLand SMP Mods\TeamSkins\libs'
$classes = Join-Path $root 'build\classes'
$packageDir = Join-Path $root 'build\package'
$libsDir = Join-Path $root 'build\libs'
$localLibs = Join-Path $root 'libs'

$cp = [System.Collections.Generic.List[string]]::new()
$cp.Add($serverJar)

# Local mod libraries (e.g. Polymer).
foreach ($jar in Get-ChildItem -LiteralPath $localLibs -Filter *.jar) {
    $cp.Add($jar.FullName)
}

# All pre-assembled dependency jars (exclude the bundler server.jar).
foreach ($jar in Get-ChildItem -LiteralPath $cpDir -Filter *.jar) {
    if ($jar.Name -ne 'server.jar') {
        $cp.Add($jar.FullName)
    }
}

# Extra runtime libs resolved via path files (guava, authlib, datafixerupper, netty).
foreach ($pathFile in @('guava_path.txt','authlib_path.txt','datafixerupper_path.txt','netty_buffer_path.txt','netty_common_path.txt')) {
    $p = Join-Path $equkinsLibs $pathFile
    if (Test-Path -LiteralPath $p) {
        $cp.Add((Get-Content -LiteralPath $p -Raw).Trim())
    }
}

$sources = Get-ChildItem -Path (Join-Path $root 'src\main\java') -Recurse -Filter *.java | ForEach-Object { $_.FullName }

New-Item -ItemType Directory -Force -Path $classes | Out-Null
$cpStr = $cp -join ';'
javac --release 25 -encoding UTF-8 -cp $cpStr -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

New-Item -ItemType Directory -Force -Path $packageDir | Out-Null
Remove-Item -Recurse -Force -Path (Join-Path $packageDir '*') -ErrorAction SilentlyContinue
Copy-Item -Recurse -Force -Path (Join-Path $classes '*') -Destination $packageDir
Copy-Item -Recurse -Force -Path (Join-Path $root 'src\main\resources\*') -Destination $packageDir

$modJson = Get-Content (Join-Path $root 'src\main\resources\fabric.mod.json') -Raw
$modJson = $modJson.Replace('${version}', '1.0.0')
Set-Content -Path (Join-Path $packageDir 'fabric.mod.json') -Value $modJson -Encoding UTF8

New-Item -ItemType Directory -Force -Path $libsDir | Out-Null
$out = Join-Path $libsDir 'teambooks-1.0.0.jar'
jar cf $out -C $packageDir .
if ($LASTEXITCODE -ne 0) { throw 'jar failed' }
Write-Output "Built $out"
