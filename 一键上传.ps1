# 一键上传（交互式终端）
$ErrorActionPreference = 'Continue'
$Host.UI.RawUI.WindowTitle = 'Experiment4 -> GitHub'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$RepoName = 'software-engineering-lab4'
$Dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Git = 'C:\Program Files\Git\cmd\git.exe'
$env:GH_CONFIG_DIR = 'C:\Users\Public\gh-tool\cfg'
$Gh = 'C:\Users\Public\gh-tool\ghx\Program Files\GitHub CLI\gh.exe'

Clear-Host
Write-Host '====================================================' -ForegroundColor Cyan
Write-Host '      Experiment 4  ->  GitHub  uploader' -ForegroundColor Cyan
Write-Host '====================================================' -ForegroundColor Cyan
Write-Host "Dir : $Dir"
Write-Host "Git : $(if(Test-Path $Git){'OK'}else{'MISSING'})"
Write-Host "Gh  : $(if(Test-Path $Gh){'OK'}else{'MISSING'})"
Write-Host ''

if (-not (Test-Path $Git)) { Write-Host 'git not found' -ForegroundColor Red; Read-Host 'press Enter'; exit 1 }
if (-not (Test-Path $Gh))  { Write-Host 'gh not found' -ForegroundColor Red; Read-Host 'press Enter'; exit 1 }

# ---------- 1. login ----------
& $Gh auth status 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host '[STEP 1] GitHub login required' -ForegroundColor Yellow
    Write-Host '   A one-time code will appear below.' -ForegroundColor Yellow
    Write-Host '   Then open  https://github.com/login/device  and enter it.' -ForegroundColor Yellow
    Write-Host ''
    & $Gh auth login --hostname github.com --git-protocol https --web
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host 'Login failed. Turn ON Clash Verge (TUN mode) and run again.' -ForegroundColor Red
        Read-Host 'press Enter to close'
        exit 1
    }
}

$User = (& $Gh api user --jq '.login').Trim()
Write-Host ''
Write-Host "[STEP 2] Logged in as : $User" -ForegroundColor Green

# ---------- 3. commit ----------
Push-Location $Dir
if (-not (Test-Path .git)) { & $Git init -b main | Out-Null }
& $Git config user.name 'Han Da'
& $Git config user.email '202410120205@ctgu.edu.cn'
& $Git config core.quotepath false
& $Git add -A 2>&1 | Out-Null
& $Git commit -m 'SE Lab4: state diagram modeling and state machine implementation' 2>&1 | Out-Null

# ---------- 4. repo + push ----------
Write-Host ''
Write-Host "[STEP 3] Creating repository : $RepoName" -ForegroundColor Yellow
& $Gh repo view "$User/$RepoName" 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    & $Gh repo create $RepoName --public --description 'Software Engineering Lab 4 (202410120205)'
}

& $Git remote remove origin 2>&1 | Out-Null
& $Git remote add origin "https://github.com/$User/$RepoName.git"

Write-Host ''
Write-Host '[STEP 4] Pushing ...' -ForegroundColor Yellow
& $Git push -u origin main

Write-Host ''
if ($LASTEXITCODE -eq 0) {
    Write-Host '====================================================' -ForegroundColor Green
    Write-Host '   SUCCESS' -ForegroundColor Green
    Write-Host "   https://github.com/$User/$RepoName" -ForegroundColor Green
    Write-Host '====================================================' -ForegroundColor Green
    Write-Host ''
    Write-Host 'Send this URL back so the report can be updated.' -ForegroundColor Cyan
} else {
    Write-Host 'Push failed - see the error above.' -ForegroundColor Red
}
Pop-Location
Write-Host ''
Read-Host 'Press Enter to close'
