# Fifth pass: Replace Array -> Seq with correct ordering

$javaDirs = @("core/src", "desktop/src", "server/src", "kryonet/src", "tests/src")
$javaFiles = Get-ChildItem -Recurse -Filter "*.java" -Path $javaDirs | Select-Object -ExpandProperty FullName
$javaFiles = $javaFiles | Where-Object { $_ -notlike "*\arc\*" }

Write-Host "Processing $($javaFiles.Count) files..."
$changes = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file -Raw -ErrorAction SilentlyContinue
    if (-not $content) { continue }
    $orig = $content
    
    # ORDER MATTERS: ThreadArray must be replaced BEFORE Array
    $content = $content -replace 'ThreadArray', 'Seq'
    $content = $content -replace 'GridMap', 'GridMap' # no-op for now, GridMap compat class exists
    
    # Array -> Seq replacements (these are all GDX Array class references)
    $content = $content -replace 'Array<', 'Seq<'
    $content = $content -replace 'new Array<>\(', 'new Seq<>('
    $content = $content -replace 'new Array\(', 'new Seq('
    $content = $content -replace 'new Array\[', 'new Seq['
    $content = $content -replace 'Array\.with\(', 'Seq.with('
    $content = $content -replace 'Array\.class', 'Seq.class'
    $content = $content -replace 'Array::new', 'Seq::new'
    
    # GridMap import fix
    if ($content -match '\bGridMap\b' -and $content -notmatch 'import arc\.struct\.GridMap') {
        $lines = $content -split "`r`n|`n"
        $insertAt = -1
        for ($i = 0; $i -lt $lines.Length; $i++) {
            if ($lines[$i] -match '^import arc\.struct\.') {
                $insertAt = $i
            }
            if ($lines[$i] -match '^import ' -and $insertAt -eq -1) {
                $insertAt = $i - 1
            }
        }
        if ($insertAt -ge 0) {
            $lines[$insertAt] = $lines[$insertAt] + "`r`nimport arc.struct.GridMap;"
            $content = $lines -join "`r`n"
        } elseif ($content -match '^(package .+?;)') {
            $content = $content -replace '(package .+?;)', "`$1`r`n`r`nimport arc.struct.GridMap;"
        }
    }
    
    if ($content -ne $orig) {
        Set-Content -Path $file -Value $content -NoNewline
        $changes++
    }
}

Write-Host "Fifth pass complete. Changed $changes files."
