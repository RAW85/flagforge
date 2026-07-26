#Requires -Version 5.1
# FlagForge control center - ASCII only (Windows PowerShell 5.1 safe)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

function Write-Banner {
    Clear-Host
    Write-Host ""
    Write-Host "  ============================================================" -ForegroundColor Cyan
    Write-Host "   FlagForge Control Center" -ForegroundColor Cyan
    Write-Host "   Feature Flag Platform" -ForegroundColor DarkGray
    Write-Host "  ============================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Get-JavaStatus {
    $result = [ordered]@{ Ok = $false; Detail = "MISSING - install JDK 21"; Warn = $false }
    if (-not (Test-CommandName "java")) { return $result }
    $versionText = ""
    try {
        $raw = & java -version 2>&1
        if ($null -ne $raw) {
            $versionText = ($raw | ForEach-Object { "$_" }) -join " "
        }
    } catch {
        $versionText = $_.Exception.Message
    }
    if ([string]::IsNullOrWhiteSpace($versionText)) {
        $result.Detail = "FOUND but version unreadable"
        $result.Warn = $true
        return $result
    }
    if ($versionText -match 'version "([^"]+)"') {
        $ver = $Matches[1]
        if ($ver -like "21*") {
            $result.Ok = $true
            $result.Detail = "OK - $ver"
        } else {
            $result.Ok = $true
            $result.Warn = $true
            $result.Detail = "WARN - $ver (JDK 21 recommended)"
        }
    } else {
        $result.Ok = $true
        $result.Warn = $true
        $result.Detail = "WARN - installed (could not parse version)"
    }
    return $result
}

function Get-NodeStatus {
    $result = [ordered]@{ Ok = $false; Detail = "MISSING - install Node 20.19+ / 22.12+ / 24.x"; Warn = $false }
    if (-not (Test-CommandName "node")) { return $result }
    try {
        $ver = (& node -v 2>&1 | Out-String).Trim()
    } catch {
        $ver = ""
    }
    if ([string]::IsNullOrWhiteSpace($ver)) {
        $result.Detail = "FOUND but version unreadable"
        $result.Warn = $true
        return $result
    }
    # v24.18.0 -> major 24
    $major = 0
    if ($ver -match 'v?(\d+)') { $major = [int]$Matches[1] }
    if ($major -ge 20) {
        $result.Ok = $true
        $result.Detail = "OK - $ver"
        if ($major -lt 20) {
            $result.Warn = $true
            $result.Detail = "WARN - $ver (need 20.19+ / 22.12+ / 24.x)"
        }
    } else {
        $result.Ok = $true
        $result.Warn = $true
        $result.Detail = "WARN - $ver (need 20.19+ / 22.12+ / 24.x)"
    }
    return $result
}

function Get-NpmStatus {
    $result = [ordered]@{ Ok = $false; Detail = "MISSING - install npm (comes with Node)"; Warn = $false }
    if (-not (Test-CommandName "npm")) { return $result }
    try {
        $ver = (& npm -v 2>&1 | Out-String).Trim()
    } catch {
        $ver = ""
    }
    if ([string]::IsNullOrWhiteSpace($ver)) {
        $result.Ok = $true
        $result.Warn = $true
        $result.Detail = "WARN - npm found, version unreadable"
    } else {
        $result.Ok = $true
        $result.Detail = "OK - $ver"
    }
    return $result
}

function Get-DockerStatus {
    $result = [ordered]@{ Ok = $false; Detail = "MISSING - install Docker Desktop 4.x"; Warn = $false }
    if (-not (Test-CommandName "docker")) { return $result }
    docker info 1>$null 2>$null
    if ($LASTEXITCODE -ne 0) {
        $result.Detail = "INSTALLED but NOT RUNNING - start Docker Desktop"
        $result.Warn = $true
        return $result
    }
    $composeOk = $false
    docker compose version 1>$null 2>$null
    if ($LASTEXITCODE -eq 0) { $composeOk = $true }
    if ($composeOk) {
        $result.Ok = $true
        $result.Detail = "OK - Docker running + Compose v2"
    } else {
        $result.Ok = $true
        $result.Warn = $true
        $result.Detail = "WARN - Docker running, but 'docker compose' missing"
    }
    return $result
}

function Write-StatusLine {
    param([string]$Label, $Status)
    $pad = $Label.PadRight(16)
    if (-not $Status.Ok) {
        Write-Host "  [MISSING] " -ForegroundColor Red -NoNewline
        Write-Host "$pad $($Status.Detail)" -ForegroundColor Red
    } elseif ($Status.Warn) {
        Write-Host "  [WARN]    " -ForegroundColor Yellow -NoNewline
        Write-Host "$pad $($Status.Detail)" -ForegroundColor Yellow
    } else {
        Write-Host "  [OK]      " -ForegroundColor Green -NoNewline
        Write-Host "$pad $($Status.Detail)" -ForegroundColor Gray
    }
}

function Write-Prerequisites {
    Write-Host "  SYSTEM CHECK (live)" -ForegroundColor Yellow
    Write-Host "  ------------------------------------------------------------" -ForegroundColor DarkGray

    $java = Get-JavaStatus
    $node = Get-NodeStatus
    $npm = Get-NpmStatus
    $docker = Get-DockerStatus

    Write-StatusLine "Java JDK 21" $java
    Write-StatusLine "Node.js" $node
    Write-StatusLine "npm" $npm
    Write-StatusLine "Docker" $docker

    Write-Host ""
    Write-Host "  MODE READINESS" -ForegroundColor Yellow
    Write-Host "  ------------------------------------------------------------" -ForegroundColor DarkGray

    $hostOk = $java.Ok -and $node.Ok -and $npm.Ok
    $dockerReady = $docker.Ok

    if ($hostOk) {
        Write-Host "  [OK]      Mode 1 (Local H2)              ready" -ForegroundColor Green
    } else {
        Write-Host "  [BLOCKED] Mode 1 (Local H2)              needs Java 21 + Node + npm" -ForegroundColor Red
    }

    if ($hostOk -and $dockerReady) {
        Write-Host "  [OK]      Mode 2 (Local + Redis/Kafka)   ready" -ForegroundColor Green
    } elseif (-not $hostOk -and -not $dockerReady) {
        Write-Host "  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Java/Node/npm + Docker running" -ForegroundColor Red
    } elseif (-not $hostOk) {
        Write-Host "  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Java 21 + Node + npm" -ForegroundColor Red
    } else {
        Write-Host "  [BLOCKED] Mode 2 (Local + Redis/Kafka)   needs Docker running" -ForegroundColor Red
    }

    if ($dockerReady) {
        Write-Host "  [OK]      Mode 3 (Docker + H2)           ready" -ForegroundColor Green
        Write-Host "  [OK]      Mode 4 (Full stack Docker)     ready" -ForegroundColor Green
        Write-Host "  [OK]      Modes 6-7 (Docker stop/wipe)   ready" -ForegroundColor Green
    } else {
        Write-Host "  [BLOCKED] Mode 3 (Docker + H2)           needs Docker running" -ForegroundColor Red
        Write-Host "  [BLOCKED] Mode 4 (Full stack Docker)     needs Docker running" -ForegroundColor Red
        Write-Host "  [BLOCKED] Modes 6-7 (Docker stop/wipe)   needs Docker" -ForegroundColor Red
    }

    Write-Host "  [OK]      Mode 5 (Stop app)              always available" -ForegroundColor Green

    Write-Host ""
    Write-Host "  Required by design: Java 21, Node 20.19+/22.12+/24.x, Docker 4.x (for Docker modes)." -ForegroundColor DarkGray
    Write-Host "  Images pulled on first Docker use: postgres:16-alpine, redis:7-alpine, bitnami/kafka:3.9" -ForegroundColor DarkGray
    Write-Host "  NOTE: App does NOT auto-detect Redis/Kafka/Postgres; the mode you pick wires them." -ForegroundColor DarkYellow
    Write-Host ""
}

function Write-Menu {
    Write-Host "  MODES" -ForegroundColor Yellow
    Write-Host "  ------------------------------------------------------------" -ForegroundColor DarkGray
    Write-Host "  [1] Local H2" -ForegroundColor White
    Write-Host "      Quick dev. Host BE+FE. H2+memory. No Docker. Data resets." -ForegroundColor DarkGray
    Write-Host "  [2] Local + Redis/Kafka" -ForegroundColor White
    Write-Host "      Host BE+FE. H2 DB. Redis+Kafka in Docker." -ForegroundColor DarkGray
    Write-Host "  [3] Docker + H2" -ForegroundColor White
    Write-Host "      BE+FE+Redis+Kafka in Docker. H2 inside BE. No Postgres." -ForegroundColor DarkGray
    Write-Host "  [4] Full stack Docker" -ForegroundColor White
    Write-Host "      BE+FE+Postgres+Redis+Kafka in Docker. Data persists." -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "  CONTROL" -ForegroundColor Yellow
    Write-Host "  ------------------------------------------------------------" -ForegroundColor DarkGray
    Write-Host "  [5] Stop app" -ForegroundColor White
    Write-Host "      Stop host BE/FE and app containers. Leave infra up." -ForegroundColor DarkGray
    Write-Host "  [6] Stop everything" -ForegroundColor White
    Write-Host "      Stop app + all FlagForge Docker services. Volumes kept." -ForegroundColor DarkGray
    Write-Host "  [7] Wipe Docker data" -ForegroundColor White
    Write-Host "      compose down -v (DESTRUCTIVE)." -ForegroundColor DarkGray
    Write-Host "  [0] Exit" -ForegroundColor White
    Write-Host ""
}

function Test-CommandName {
    param([string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Assert-Java {
    if (-not (Test-CommandName "java")) {
        throw "Java not found. Install JDK 21."
    }
    $versionText = ""
    try {
        $raw = & java -version 2>&1
        if ($null -ne $raw) {
            $versionText = ($raw | ForEach-Object { "$_" }) -join "`n"
        }
    } catch {
        $versionText = $_.Exception.Message
    }
    if ([string]::IsNullOrWhiteSpace($versionText)) {
        $versionText = "(could not read java -version output)"
    }
    if ($versionText -notmatch 'version "21') {
        Write-Host "  WARNING: Java 21 recommended. Detected:" -ForegroundColor Yellow
        Write-Host "  $versionText" -ForegroundColor Yellow
    }
}

function Assert-Node {
    if (-not (Test-CommandName "node")) {
        throw "Node.js not found. Install Node 20.19+ / 22.12+ / 24.x."
    }
    if (-not (Test-CommandName "npm")) {
        throw "npm not found."
    }
}

function Assert-Docker {
    if (-not (Test-CommandName "docker")) {
        throw "Docker not found. Install Docker Desktop 4.x."
    }
    docker info 1>$null 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker is not running. Start Docker Desktop."
    }
}

function Ensure-FrontendDeps {
    $nm = Join-Path $Root "frontend\node_modules"
    if (-not (Test-Path $nm)) {
        Write-Host "  Installing frontend npm dependencies..." -ForegroundColor Cyan
        Push-Location (Join-Path $Root "frontend")
        try {
            npm install
            if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
        } finally {
            Pop-Location
        }
    }
}

function Stop-PortListeners {
    param([int[]]$Ports)
    foreach ($port in $Ports) {
        try {
            $conns = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
            foreach ($c in $conns) {
                $procId = $c.OwningProcess
                if ($null -ne $procId -and $procId -ne 0) {
                    Write-Host "  Stopping PID $procId on port $port..." -ForegroundColor DarkYellow
                    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                }
            }
        } catch {
            # ignore
        }
    }
}

function Stop-AppContainers {
    if (-not (Test-CommandName "docker")) { return }
    $names = @("flagforge-backend", "flagforge-frontend")
    foreach ($n in $names) {
        $id = docker ps -aq -f "name=$n" 2>$null
        if ($id) {
            Write-Host "  Stopping container $n..." -ForegroundColor DarkYellow
            docker stop $n 1>$null 2>$null
            docker rm $n 1>$null 2>$null
        }
    }
}

function Test-PortOpen {
    param([int]$Port, [string]$HostName = "127.0.0.1")
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($HostName, $Port, $null, $null)
        $ok = $iar.AsyncWaitHandle.WaitOne(600, $false)
        if ($ok -and $client.Connected) {
            $client.EndConnect($iar) | Out-Null
            $client.Close()
            return $true
        }
        $client.Close()
    } catch { }
    return $false
}

function Wait-ForPort {
    param([int]$Port, [int]$TimeoutSec = 180, [string]$Label = "service")
    Write-Host "  Waiting for $Label on port $Port (up to ${TimeoutSec}s)..." -ForegroundColor DarkGray
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortOpen -Port $Port) {
            Write-Host "  $Label is up on port $Port." -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

# Failsafe: always clear host + Docker app before starting a new mode
function Stop-PreviousApp {
    param([string]$Reason = "Preparing clean start")
    Write-Host "  [$Reason] Stopping any previous FlagForge app (host + containers)..." -ForegroundColor Yellow
    Stop-PortListeners -Ports @(8080, 5173)
    Stop-AppContainers
    Start-Sleep -Seconds 2
    if (Test-PortOpen -Port 8080) {
        Write-Host "  Port 8080 still busy after stop - retrying..." -ForegroundColor Yellow
        Stop-PortListeners -Ports @(8080)
        Start-Sleep -Seconds 2
    }
    if (Test-PortOpen -Port 5173) {
        Write-Host "  Port 5173 still busy after stop - retrying..." -ForegroundColor Yellow
        Stop-PortListeners -Ports @(5173)
        Start-Sleep -Seconds 2
    }
    if ((Test-PortOpen -Port 8080) -or (Test-PortOpen -Port 5173)) {
        throw "Could not free ports 8080/5173. Close other FlagForge/Java/Node windows and try again."
    }
    Write-Host "  Previous app cleared." -ForegroundColor Green
}

function Start-HostBackend {
    param([string]$ProfileArgs = "")
    Assert-Java
    $be = Join-Path $Root "backend"
    $gradlew = Join-Path $be "gradlew.bat"
    if (-not (Test-Path $gradlew)) {
        throw "backend\gradlew.bat not found"
    }
    if ($ProfileArgs -ne "") {
        $inner = "gradlew.bat bootRun --args=$ProfileArgs"
    } else {
        $inner = "gradlew.bat bootRun"
    }
    $cmd = "title FlagForge Backend && cd /d `"$be`" && $inner || (echo. && echo *** BACKEND FAILED - see errors above *** && pause)"
    Write-Host "  Starting backend window (first Gradle run can take 1-2 min)..." -ForegroundColor Cyan
    Start-Process -FilePath "cmd.exe" -ArgumentList @("/k", $cmd) -WorkingDirectory $be -WindowStyle Normal
}

function Start-HostFrontend {
    Assert-Node
    Ensure-FrontendDeps
    $fe = Join-Path $Root "frontend"
    if (-not (Test-Path (Join-Path $fe "package.json"))) {
        throw "frontend\package.json not found"
    }
    $cmd = "title FlagForge Frontend && cd /d `"$fe`" && npm run dev || (echo. && echo *** FRONTEND FAILED - see errors above *** && pause)"
    Write-Host "  Starting frontend window..." -ForegroundColor Cyan
    Start-Process -FilePath "cmd.exe" -ArgumentList @("/k", $cmd) -WorkingDirectory $fe -WindowStyle Normal
}

function Open-Browser {
    param([string]$Url)
    Start-Sleep -Seconds 1
    Start-Process $Url
}

function Mode-LocalH2 {
    Write-Host ""
    Write-Host "  >> Mode 1: Local H2" -ForegroundColor Green
    Stop-PreviousApp -Reason "Mode 1"
    Start-HostBackend
    if (-not (Wait-ForPort -Port 8080 -TimeoutSec 180 -Label "backend")) {
        throw "Backend did not open port 8080. Check the 'FlagForge Backend' window (Java 21? temp dir ownership?)."
    }
    Start-HostFrontend
    if (-not (Wait-ForPort -Port 5173 -TimeoutSec 90 -Label "frontend")) {
        Write-Host "  WARNING: Frontend not on 5173 yet. Check the 'FlagForge Frontend' window." -ForegroundColor Yellow
    }
    Open-Browser "http://localhost:5173"
    Write-Host "  UI: http://localhost:5173  |  API: http://localhost:8080" -ForegroundColor Cyan
}

function Mode-LocalRedisKafka {
    Write-Host ""
    Write-Host "  >> Mode 2: Local app + Redis/Kafka Docker" -ForegroundColor Green
    Assert-Docker
    Stop-PreviousApp -Reason "Mode 2"
    docker compose --profile redis-kafka up -d
    if ($LASTEXITCODE -ne 0) { throw "docker compose failed" }
    Write-Host "  Waiting for Redis/Kafka..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 20
    Start-HostBackend -ProfileArgs "--spring.profiles.active=redis-kafka"
    if (-not (Wait-ForPort -Port 8080 -TimeoutSec 180 -Label "backend")) {
        throw "Backend did not open port 8080. Check the 'FlagForge Backend' window."
    }
    Start-HostFrontend
    if (-not (Wait-ForPort -Port 5173 -TimeoutSec 90 -Label "frontend")) {
        Write-Host "  WARNING: Frontend not on 5173 yet. Check the 'FlagForge Frontend' window." -ForegroundColor Yellow
    }
    Open-Browser "http://localhost:5173"
    Write-Host "  UI: http://localhost:5173  |  API: http://localhost:8080" -ForegroundColor Cyan
}

function Mode-DockerH2 {
    Write-Host ""
    Write-Host "  >> Mode 3: Docker BE+FE + H2 + Redis + Kafka" -ForegroundColor Green
    Assert-Docker
    Stop-PreviousApp -Reason "Mode 3"
    $env:BACKEND_ENV_FILE = "./deploy/backend-h2.env"
    docker compose --profile docker-h2 up -d --build
    if ($LASTEXITCODE -ne 0) { throw "docker compose build/up failed" }
    Write-Host "  Waiting for stack (first build can take several minutes)..." -ForegroundColor DarkGray
    if (-not (Wait-ForPort -Port 8080 -TimeoutSec 300 -Label "backend container")) {
        throw "Docker backend did not open port 8080. Try: docker logs flagforge-backend"
    }
    if (-not (Wait-ForPort -Port 5173 -TimeoutSec 120 -Label "frontend container")) {
        Write-Host "  WARNING: Frontend container not on 5173 yet. Try: docker logs flagforge-frontend" -ForegroundColor Yellow
    }
    Open-Browser "http://localhost:5173"
    Write-Host "  UI: http://localhost:5173  |  API: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "  H2 is inside the backend container - data lost when BE container stops." -ForegroundColor DarkYellow
}

function Mode-DockerFull {
    Write-Host ""
    Write-Host "  >> Mode 4: Full stack Docker (Postgres + Redis + Kafka + BE + FE)" -ForegroundColor Green
    Assert-Docker
    Stop-PreviousApp -Reason "Mode 4"
    $env:BACKEND_ENV_FILE = "./deploy/backend-full.env"
    docker compose --profile docker-full up -d --build
    if ($LASTEXITCODE -ne 0) { throw "docker compose build/up failed" }
    Write-Host "  Waiting for Postgres and stack (first build can take several minutes)..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 15
    docker restart flagforge-backend 1>$null 2>$null
    if (-not (Wait-ForPort -Port 8080 -TimeoutSec 300 -Label "backend container")) {
        throw "Docker backend did not open port 8080. Try: docker logs flagforge-backend"
    }
    if (-not (Wait-ForPort -Port 5173 -TimeoutSec 120 -Label "frontend container")) {
        Write-Host "  WARNING: Frontend container not on 5173 yet. Try: docker logs flagforge-frontend" -ForegroundColor Yellow
    }
    Open-Browser "http://localhost:5173"
    Write-Host "  UI: http://localhost:5173  |  API: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "  Postgres data persists in Docker volume flagforge_pg_data." -ForegroundColor DarkGreen
}

function Mode-StopApp {
    Write-Host ""
    Write-Host "  >> Stop app (host processes + app containers)" -ForegroundColor Green
    Stop-PreviousApp -Reason "Stop app"
    Write-Host "  App stopped. Docker infra (if any) still running." -ForegroundColor Cyan
}

function Mode-StopEverything {
    Write-Host ""
    Write-Host "  >> Stop everything" -ForegroundColor Green
    Stop-PortListeners -Ports @(8080, 5173)
    Stop-AppContainers
    if (Test-CommandName "docker") {
        docker compose --profile redis-kafka --profile docker-h2 --profile docker-full stop 2>$null
        docker compose --profile redis-kafka --profile docker-h2 --profile docker-full down 2>$null
    }
    Write-Host "  Stopped. Volumes kept (use 7 to wipe)." -ForegroundColor Cyan
}

function Mode-Wipe {
    Write-Host ""
    Write-Host "  >> Wipe Docker data (DESTRUCTIVE)" -ForegroundColor Red
    $confirm = Read-Host "  Type YES to remove FlagForge Docker volumes"
    if ($confirm -ne "YES") {
        Write-Host "  Cancelled." -ForegroundColor Yellow
        return
    }
    Stop-PortListeners -Ports @(8080, 5173)
    Assert-Docker
    docker compose --profile redis-kafka --profile docker-h2 --profile docker-full down -v
    Write-Host "  Volumes removed." -ForegroundColor Cyan
}

# main loop
while ($true) {
    Write-Banner
    Write-Prerequisites
    Write-Menu
    $choice = Read-Host "  Select option"
    try {
        switch ($choice.Trim()) {
            "1" { Mode-LocalH2 }
            "2" { Mode-LocalRedisKafka }
            "3" { Mode-DockerH2 }
            "4" { Mode-DockerFull }
            "5" { Mode-StopApp }
            "6" { Mode-StopEverything }
            "7" { Mode-Wipe }
            "0" {
                Write-Host ""
                Write-Host "  Bye." -ForegroundColor Cyan
                Write-Host ""
                exit 0
            }
            default { Write-Host "  Invalid option." -ForegroundColor Red }
        }
    } catch {
        Write-Host ""
        Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
    Read-Host "  Press Enter to return to menu"
}
