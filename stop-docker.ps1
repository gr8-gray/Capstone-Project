# ===================================================================
# Smart Expense Tracker - Docker Stop Script
# ===================================================================
# This script stops the Spring Boot application and MySQL database
# ===================================================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Smart Expense Tracker - Shutdown" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Stopping MySQL database container..." -ForegroundColor Yellow
docker compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ MySQL container stopped successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to stop MySQL container" -ForegroundColor Red
}

Write-Host ""
Write-Host "Cleanup complete!" -ForegroundColor Green
Write-Host "To start again, run: .\start-with-docker.ps1" -ForegroundColor Cyan
