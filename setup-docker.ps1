# Docker Setup Instructions for Team Members
# Smart Expense Tracker - Group 3

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Docker Setup - Smart Expense Tracker" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is installed
Write-Host "Checking Docker installation..." -ForegroundColor Yellow
$dockerInstalled = $false

try {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $dockerInstalled = $true
        Write-Host "[✓] Docker is installed: $dockerVersion" -ForegroundColor Green
    }
} catch {
    Write-Host "[✗] Docker is not installed" -ForegroundColor Red
}

if (-not $dockerInstalled) {
    Write-Host ""
    Write-Host "Please install Docker Desktop:" -ForegroundColor Yellow
    Write-Host "  Windows/Mac: https://www.docker.com/products/docker-desktop/" -ForegroundColor White
    Write-Host ""
    Write-Host "After installing Docker Desktop:" -ForegroundColor Yellow
    Write-Host "  1. Start Docker Desktop" -ForegroundColor White
    Write-Host "  2. Wait for 'Docker Desktop is running' notification" -ForegroundColor White
    Write-Host "  3. Run this script again" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Check if Docker is running
Write-Host ""
Write-Host "Checking if Docker is running..." -ForegroundColor Yellow

try {
    docker ps > $null 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[✓] Docker is running" -ForegroundColor Green
    } else {
        Write-Host "[✗] Docker is not running" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please start Docker Desktop and try again" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "[✗] Cannot connect to Docker" -ForegroundColor Red
    Write-Host "Please make sure Docker Desktop is running" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Setup Options" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Start MySQL with Docker Compose (Recommended)" -ForegroundColor White
Write-Host "2. Check Docker Status" -ForegroundColor White
Write-Host "3. View MySQL Logs" -ForegroundColor White
Write-Host "4. Access MySQL CLI" -ForegroundColor White
Write-Host "5. Stop MySQL Container" -ForegroundColor White
Write-Host "6. Reset Everything (Fresh Start)" -ForegroundColor White
Write-Host "7. View Quick Reference" -ForegroundColor White
Write-Host "8. Exit" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Enter your choice (1-8)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Starting MySQL with Docker Compose..." -ForegroundColor Yellow
        Write-Host ""
        
        # Check if container already exists
        $existingContainer = docker ps -a --filter "name=expense-tracker-mysql" --format "{{.Names}}"
        
        if ($existingContainer) {
            Write-Host "MySQL container already exists" -ForegroundColor Yellow
            Write-Host "Starting existing container..." -ForegroundColor Yellow
            docker-compose start
        } else {
            Write-Host "Creating new MySQL container..." -ForegroundColor Yellow
            docker-compose up -d
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "[✓] MySQL container started successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Waiting for MySQL to initialize (15 seconds)..." -ForegroundColor Yellow
            Start-Sleep -Seconds 15
            
            Write-Host ""
            Write-Host "[✓] MySQL is ready!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Connection Details:" -ForegroundColor Cyan
            Write-Host "  Host:     localhost" -ForegroundColor White
            Write-Host "  Port:     3306" -ForegroundColor White
            Write-Host "  Database: expense_db" -ForegroundColor White
            Write-Host "  User:     expense_user" -ForegroundColor White
            Write-Host "  Password: expense_password" -ForegroundColor White
            Write-Host ""
            Write-Host "Next Steps:" -ForegroundColor Yellow
            Write-Host "  1. Start backend: .\mvnw.cmd spring-boot:run" -ForegroundColor White
            Write-Host "  2. Run tests: .\test-integration.ps1" -ForegroundColor White
            Write-Host "  3. Open frontend: frontend\index.html" -ForegroundColor White
        } else {
            Write-Host ""
            Write-Host "[✗] Failed to start MySQL container" -ForegroundColor Red
            Write-Host "Check the error messages above" -ForegroundColor Yellow
        }
    }
    
    "2" {
        Write-Host ""
        Write-Host "Checking Docker status..." -ForegroundColor Yellow
        Write-Host ""
        
        $containers = docker ps --filter "name=expense-tracker" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        
        if ($containers) {
            Write-Host $containers
            Write-Host ""
            Write-Host "[✓] Container(s) running" -ForegroundColor Green
        } else {
            Write-Host "No expense-tracker containers running" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Start with: docker-compose up -d" -ForegroundColor White
        }
    }
    
    "3" {
        Write-Host ""
        Write-Host "Fetching MySQL logs..." -ForegroundColor Yellow
        Write-Host ""
        docker logs expense-tracker-mysql --tail 50
    }
    
    "4" {
        Write-Host ""
        Write-Host "Connecting to MySQL CLI..." -ForegroundColor Yellow
        Write-Host "Type 'exit' to return to PowerShell" -ForegroundColor Gray
        Write-Host ""
        docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db
    }
    
    "5" {
        Write-Host ""
        Write-Host "Stopping MySQL container..." -ForegroundColor Yellow
        docker-compose stop
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "[✓] MySQL container stopped" -ForegroundColor Green
            Write-Host "Data is preserved. Restart with: docker-compose start" -ForegroundColor White
        }
    }
    
    "6" {
        Write-Host ""
        Write-Host "WARNING: This will delete all data!" -ForegroundColor Red
        $confirm = Read-Host "Are you sure? (yes/no)"
        
        if ($confirm -eq "yes") {
            Write-Host ""
            Write-Host "Removing containers and volumes..." -ForegroundColor Yellow
            docker-compose down -v
            
            Write-Host ""
            Write-Host "[✓] Everything removed" -ForegroundColor Green
            Write-Host "Start fresh with: docker-compose up -d" -ForegroundColor White
        } else {
            Write-Host "Cancelled" -ForegroundColor Yellow
        }
    }
    
    "7" {
        Write-Host ""
        Write-Host "Quick Reference - Docker Commands" -ForegroundColor Cyan
        Write-Host "======================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Start MySQL:" -ForegroundColor Yellow
        Write-Host "  docker-compose up -d" -ForegroundColor White
        Write-Host ""
        Write-Host "Stop MySQL (keeps data):" -ForegroundColor Yellow
        Write-Host "  docker-compose stop" -ForegroundColor White
        Write-Host ""
        Write-Host "Restart MySQL:" -ForegroundColor Yellow
        Write-Host "  docker-compose restart" -ForegroundColor White
        Write-Host ""
        Write-Host "View logs:" -ForegroundColor Yellow
        Write-Host "  docker-compose logs -f" -ForegroundColor White
        Write-Host ""
        Write-Host "Access MySQL:" -ForegroundColor Yellow
        Write-Host "  docker exec -it expense-tracker-mysql mysql -u expense_user -pexpense_password expense_db" -ForegroundColor White
        Write-Host ""
        Write-Host "Check status:" -ForegroundColor Yellow
        Write-Host "  docker ps" -ForegroundColor White
        Write-Host ""
        Write-Host "Remove everything:" -ForegroundColor Yellow
        Write-Host "  docker-compose down -v" -ForegroundColor White
        Write-Host ""
    }
    
    "8" {
        Write-Host ""
        Write-Host "Goodbye!" -ForegroundColor Cyan
        exit 0
    }
    
    default {
        Write-Host ""
        Write-Host "Invalid choice" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
