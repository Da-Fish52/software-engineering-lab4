@echo off
chcp 65001 >nul
cd /d "%~dp0"
start "GitHub-Upload" cmd /k "chcp 65001 >nul && C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -NoExit -NoProfile -ExecutionPolicy Bypass -File ""%~dp0upload_github.ps1"""
