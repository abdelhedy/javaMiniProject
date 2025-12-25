@echo off
:: ==============================================
:: Script de démarrage simple pour le projet
:: ==============================================

echo ======================================
echo   Demarrage du serveur
echo ======================================
echo.

:: Toujours se placer dans le dossier du script
cd /d "%~dp0"

:: Lancer le script PowerShell avec bypass de la politique d'execution
powershell.exe -ExecutionPolicy Bypass -NoProfile -File "%~dp0run.ps1"

pause
