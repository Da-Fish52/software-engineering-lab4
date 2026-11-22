# Experiment 4 -> GitHub uploader
# Menu selections auto-answered so gh never blocks on arrow-key prompts.

$ErrorActionPreference = 'Continue'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch {}

# --- 让 gh / git 都走本地 Clash 代理 ---
$env:HTTPS_PROXY = 'http://127.0.0.1:7897'
$env:HTTP_PROXY  = 'http://127.0.0.1:7897'

$RepoName = 'software-engineering-lab4'
$Dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Git = 'C:\Program Files\Git\cmd\git.exe'
$Gh  = 'C:\Users\Public\gh-tool\ghx\Program Files\GitHub CLI\gh.exe'
$env:GH_CONFIG_DIR = 'C:\Users\Public\gh-tool\cfg'

Write-Host '====================================================' -ForegroundColor Cyan
Write-Host '      Experiment 4  ->  GitHub  uploader' -ForegroundColor Cyan
Write-Host '====================================================' -ForegroundColor Cyan
Write-Host "Dir : $Dir"
Write-Host "Git : $(if (Test-Path $Git) {'OK'} else {'MISSING'})"
Write-Host "Gh  : $(if (Test-Path $Gh)  {'OK'} else {'MISSING'})"
Write-Host "Proxy: $env:HTTPS_PROXY" -ForegroundColor DarkGray

# 代理连通性自检
try {
    $r = Invoke-WebRequest 'https://github.com' -Proxy 'http://127.0.0.1:7897' -TimeoutSec 20 -UseBasicParsing
    Write-Host "GitHub reachable via proxy (HTTP $($r.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host 'GitHub NOT reachable via proxy.' -ForegroundColor Red
    Write-Host 'Please open Clash Verge and make sure it is connected, then run again.' -ForegroundColor Red
    Read-Host 'Enter to close'
    exit 1
}
Write-Host ''

if (-not (Test-Path $Git)) { Write-Host 'git is missing' -ForegroundColor Red; Read-Host 'Enter to close'; exit 1 }
if (-not (Test-Path $Gh))  { Write-Host 'gh is missing'  -ForegroundColor Red; Read-Host 'Enter to close'; exit 1 }

# ---------------- STEP 1: login ----------------
& $Gh auth status 1>$null 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host '[STEP 1] GitHub login' -ForegroundColor Yellow
    Write-Host '  1) A one-time code appears below (like 1A2B-3C4D)' -ForegroundColor Yellow
    Write-Host '  2) Open  https://github.com/login/device' -ForegroundColor Yellow
    Write-Host '  3) Type the code and click Authorize' -ForegroundColor Yellow
    Write-Host '  4) Return here - it continues automatically' -ForegroundColor Yellow
    Write-Host ''

    # answers for: hostname / git-protocol / auth method (login with browser)
    @('github.com', 'https', 'web') | & $Gh auth login --scopes repo
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host 'LOGIN FAILED.' -ForegroundColor Red
        Write-Host 'Turn ON Clash Verge (TUN mode) and run upload.bat again.' -ForegroundColor Red
        Read-Host 'Enter to close'
        exit 1
    }
}

$User = (& $Gh api user --jq '.login').Trim()
Write-Host ''
Write-Host "[STEP 2] Logged in as: $User" -ForegroundColor Green

# ---------------- STEP 3: commit ----------------
Push-Location $Dir
if (-not (Test-Path .git)) { & $Git init -b main 1>$null }
& $Git config user.name 'Han Da'
& $Git config user.email '202410120205@ctgu.edu.cn'
& $Git config core.quotepath false
& $Git add -A 1>$null
& $Git commit -m 'SE Lab4: state diagram modeling and state machine implementation' 1>$null 2>$null

# ---------------- STEP 4: repo + push ----------------
Write-Host ''
Write-Host "[STEP 3] Creating repository: $RepoName" -ForegroundColor Yellow
& $Gh repo view "$User/$RepoName" 1>$null 2>$null
if ($LASTEXITCODE -ne 0) {
    & $Gh repo create $RepoName --public --description 'Software Engineering Lab 4 (202410120205)'
}
& $Git remote remove origin 1>$null 2>$null
& $Git remote add origin "https://github.com/$User/$RepoName.git"

Write-Host ''
Write-Host '[STEP 4] Pushing ...' -ForegroundColor Yellow
& $Git push -u origin main
$pushOk = ($LASTEXITCODE -eq 0)

Write-Host ''
if ($pushOk) {
    Write-Host '====================================================' -ForegroundColor Green
    Write-Host '   SUCCESS' -ForegroundColor Green
    Write-Host "   https://github.com/$User/$RepoName" -ForegroundColor Green
    Write-Host '====================================================' -ForegroundColor Green
    Write-Host ''
    Write-Host 'Paste this URL back to the chat so the report can be updated.' -ForegroundColor Cyan
} else {
    Write-Host 'PUSH FAILED - check the error above.' -ForegroundColor Red
}
Pop-Location
