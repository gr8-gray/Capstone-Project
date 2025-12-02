# ===================================================================
# Smart Expense Tracker - Docker Startup Script
# ===================================================================
# This script starts the MySQL database in Docker and then runs
# the Spring Boot application
# ===================================================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Smart Expense Tracker - Docker Startup" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "[1/4] Checking Docker status..." -ForegroundColor Yellow
try {
    $dockerStatus = docker ps 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Docker is not running!" -ForegroundColor Red
        Write-Host "Please start Docker Desktop and try again." -ForegroundColor Red
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Docker command not found!" -ForegroundColor Red
    Write-Host "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop/" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Start MySQL container
Write-Host "[2/4] Starting MySQL database container..." -ForegroundColor Yellow
docker compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to start MySQL container!" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ MySQL container started" -ForegroundColor Green
Write-Host ""

# Wait for MySQL to be ready
Write-Host "[3/4] Waiting for MySQL to be ready..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$ready = $false

while (-not $ready -and $attempt -lt $maxAttempts) {
    $attempt++
    Write-Host "  Attempt $attempt/$maxAttempts..." -ForegroundColor Gray
    
    # Check MySQL health
    $healthStatus = docker inspect --format='{{.State.Health.Status}}' smartexpensetrackingapp-mysql-1 2>&1
    
    if ($healthStatus -eq "healthy") {
        $ready = $true
        Write-Host "✓ MySQL is ready and healthy" -ForegroundColor Green
    } else {
        Start-Sleep -Seconds 2
    }
}

if (-not $ready) {
    Write-Host "WARNING: MySQL may not be fully ready yet" -ForegroundColor Yellow
    Write-Host "The application will retry connection automatically" -ForegroundColor Yellow
}

Write-Host ""

# Start Spring Boot application
Write-Host "[4/4] Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Backend server starting..." -ForegroundColor Cyan
Write-Host "  API will be available at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  Press Ctrl+C to stop the server" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

.\mvnw.cmd spring-boot:run

# Cleanup on exit
Write-Host ""
Write-Host "Application stopped." -ForegroundColor Yellow
Write-Host "MySQL container is still running in the background." -ForegroundColor Yellow
Write-Host "To stop MySQL, run: docker compose down" -ForegroundColor Yellow
