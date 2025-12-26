# Script de démarrage standalone - Sans Maven, Sans Tomcat
# =========================================================

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Compilation et Démarrage" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Toujours se placer dans le dossier du script
Set-Location $PSScriptRoot

# Créer le dossier lib
if (-not (Test-Path "lib")) {
    New-Item -ItemType Directory -Path "lib" | Out-Null
}

Write-Host "[1/3] Téléchargement des dépendances..." -ForegroundColor Yellow

$dependencies = @{
    "mysql-connector-j-8.2.0.jar" = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar"
    "gson-2.10.1.jar" = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    "HikariCP-5.1.0.jar" = "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar"
    "slf4j-api-2.0.9.jar" = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
    "slf4j-simple-2.0.9.jar" = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar"
}

foreach ($jar in $dependencies.Keys) {
    $jarPath = "lib\$jar"
    if (-not (Test-Path $jarPath)) {
        Write-Host "  - Téléchargement de $jar..." -NoNewline
        try {
            Invoke-WebRequest -Uri $dependencies[$jar] -OutFile $jarPath -UseBasicParsing
            Write-Host " OK" -ForegroundColor Green
        } catch {
            Write-Host " ERREUR" -ForegroundColor Red
            Write-Host "    $_" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "  - $jar déjà présent" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "[2/3] Compilation du code Java..." -ForegroundColor Yellow

# Nettoyer et créer le dossier bin
if (Test-Path "bin") {
    Remove-Item -Path "bin" -Recurse -Force
}
New-Item -ItemType Directory -Path "bin" | Out-Null

# Trouver tous les fichiers Java
$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | 
    Select-Object -ExpandProperty FullName

# Créer un fichier temporaire avec la liste des sources
$javaFiles | Out-File -FilePath "sources.txt" -Encoding ASCII

# Compiler
$compileOutput = javac -encoding UTF-8 -d bin -cp "lib\*" "@sources.txt" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERREUR: La compilation a échoué" -ForegroundColor Red
    Write-Host $compileOutput -ForegroundColor Red
    Remove-Item "sources.txt" -Force -ErrorAction SilentlyContinue
    pause
    exit 1
}

Remove-Item "sources.txt" -Force
Write-Host "  - Compilation réussie" -ForegroundColor Green

Write-Host ""
Write-Host "[3/3] Démarrage du serveur..." -ForegroundColor Yellow
Write-Host ""

# Construire les chemins absolus pour éviter les problèmes
$binPath = Join-Path $PSScriptRoot "bin"
$libPath = Join-Path $PSScriptRoot "lib\*"
$resourcesPath = Join-Path $PSScriptRoot "src\main\resources"
$classPath = "$binPath;$resourcesPath;$libPath"

# Lancer le serveur
java -cp $classPath org.projectmanagement.SimpleServer
