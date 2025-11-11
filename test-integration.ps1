# ===================================================================
# SMART EXPENSE TRACKER - Integration Test Script
# UMGC CMSC 495 Capstone Project - Group 3
# ===================================================================
# 
# This script tests the complete integration between:
# - Frontend (HTML/JS)
# - Backend (Spring Boot API)
# - Database (MySQL)
#
# Prerequisites:
# - MySQL running with expense_db database
# - Backend server running on http://localhost:8080
# ===================================================================

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  Smart Expense Tracker - Integration Test  " -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$API_BASE = "http://localhost:8080/api/expenses"
$testsPassed = 0
$testsFailed = 0

# Helper function to print test results
function Test-Result {
    param(
        [string]$TestName,
        [bool]$Success,
        [string]$Message
    )
    
    if ($Success) {
        Write-Host "[✓] $TestName" -ForegroundColor Green
        Write-Host "    $Message" -ForegroundColor Gray
        $script:testsPassed++
    } else {
        Write-Host "[✗] $TestName" -ForegroundColor Red
        Write-Host "    $Message" -ForegroundColor Gray
        $script:testsFailed++
    }
    Write-Host ""
}

# ===================================================================
# TEST 1: Check Backend Connectivity
# ===================================================================
Write-Host "TEST 1: Checking backend connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $API_BASE -Method GET -TimeoutSec 5
    Test-Result "Backend API is accessible" $true "Connected to $API_BASE"
} catch {
    Test-Result "Backend API is accessible" $false "Cannot connect to $API_BASE. Is the backend running?"
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please start the backend with: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# ===================================================================
# TEST 2: Get All Expenses (Initial State)
# ===================================================================
Write-Host "TEST 2: Getting all expenses..." -ForegroundColor Yellow
try {
    $expenses = Invoke-RestMethod -Uri $API_BASE -Method GET
    $initialCount = $expenses.Count
    Test-Result "GET all expenses" $true "Found $initialCount existing expense(s)"
} catch {
    Test-Result "GET all expenses" $false "Failed to retrieve expenses: $_"
}

# ===================================================================
# TEST 3: Create New Expense
# ===================================================================
Write-Host "TEST 3: Creating new expense..." -ForegroundColor Yellow
$newExpense = @{
    description = "Integration Test - Coffee"
    amount = 4.50
    category = "Food"
    date = (Get-Date -Format "yyyy-MM-dd")
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri $API_BASE -Method POST -Body $newExpense -ContentType "application/json"
    $createdId = $created.id
    
    if ($null -ne $createdId) {
        Test-Result "POST create expense" $true "Created expense with ID: $createdId, Amount: `$$($created.amount)"
    } else {
        Test-Result "POST create expense" $false "Expense created but no ID returned"
    }
} catch {
    Test-Result "POST create expense" $false "Failed to create expense: $_"
    $createdId = $null
}

# ===================================================================
# TEST 4: Get Specific Expense by ID
# ===================================================================
if ($null -ne $createdId) {
    Write-Host "TEST 4: Getting expense by ID..." -ForegroundColor Yellow
    try {
        $fetchedExpense = Invoke-RestMethod -Uri "$API_BASE/$createdId" -Method GET
        
        if ($fetchedExpense.id -eq $createdId -and $fetchedExpense.description -eq "Integration Test - Coffee") {
            Test-Result "GET expense by ID" $true "Successfully retrieved expense ID $createdId"
        } else {
            Test-Result "GET expense by ID" $false "Data mismatch for expense ID $createdId"
        }
    } catch {
        Test-Result "GET expense by ID" $false "Failed to retrieve expense: $_"
    }
}

# ===================================================================
# TEST 5: Update Expense
# ===================================================================
if ($null -ne $createdId) {
    Write-Host "TEST 5: Updating expense..." -ForegroundColor Yellow
    $updateExpense = @{
        description = "Integration Test - Updated Coffee"
        amount = 5.75
        category = "Food"
        date = (Get-Date -Format "yyyy-MM-dd")
    } | ConvertTo-Json
    
    try {
        $updated = Invoke-RestMethod -Uri "$API_BASE/$createdId" -Method PUT -Body $updateExpense -ContentType "application/json"
        
        if ($updated.amount -eq 5.75 -and $updated.description -like "*Updated*") {
            Test-Result "PUT update expense" $true "Updated expense ID $createdId - New amount: `$$($updated.amount)"
        } else {
            Test-Result "PUT update expense" $false "Update didn't reflect changes"
        }
    } catch {
        Test-Result "PUT update expense" $false "Failed to update expense: $_"
    }
}

# ===================================================================
# TEST 6: Verify Update Persisted
# ===================================================================
if ($null -ne $createdId) {
    Write-Host "TEST 6: Verifying update persisted..." -ForegroundColor Yellow
    try {
        $verifyExpense = Invoke-RestMethod -Uri "$API_BASE/$createdId" -Method GET
        
        if ($verifyExpense.amount -eq 5.75) {
            Test-Result "Verify persistence" $true "Update persisted in database"
        } else {
            Test-Result "Verify persistence" $false "Update not found in database"
        }
    } catch {
        Test-Result "Verify persistence" $false "Failed to verify: $_"
    }
}

# ===================================================================
# TEST 7: Create Multiple Expenses (Batch Test)
# ===================================================================
Write-Host "TEST 7: Creating multiple expenses..." -ForegroundColor Yellow
$batchExpenses = @(
    @{ description = "Test - Groceries"; amount = 45.20; category = "Food"; date = (Get-Date -Format "yyyy-MM-dd") },
    @{ description = "Test - Gas"; amount = 35.00; category = "Transportation"; date = (Get-Date -Format "yyyy-MM-dd") },
    @{ description = "Test - Movie"; amount = 15.50; category = "Entertainment"; date = (Get-Date -Format "yyyy-MM-dd") }
)

$batchIds = @()
$batchSuccess = $true

foreach ($expense in $batchExpenses) {
    try {
        $result = Invoke-RestMethod -Uri $API_BASE -Method POST -Body ($expense | ConvertTo-Json) -ContentType "application/json"
        $batchIds += $result.id
    } catch {
        $batchSuccess = $false
    }
}

if ($batchSuccess) {
    Test-Result "Batch create expenses" $true "Created $($batchIds.Count) expenses successfully"
} else {
    Test-Result "Batch create expenses" $false "Failed to create all expenses"
}

# ===================================================================
# TEST 8: Get All Expenses (Verify Count Increased)
# ===================================================================
Write-Host "TEST 8: Verifying expense count increased..." -ForegroundColor Yellow
try {
    $finalExpenses = Invoke-RestMethod -Uri $API_BASE -Method GET
    $finalCount = $finalExpenses.Count
    $expectedCount = $initialCount + 1 + $batchIds.Count  # initial + 1 from test3 + batch
    
    if ($finalCount -ge $expectedCount) {
        Test-Result "Verify expense count" $true "Total expenses: $finalCount (expected >= $expectedCount)"
    } else {
        Test-Result "Verify expense count" $false "Expected >= $expectedCount, got $finalCount"
    }
} catch {
    Test-Result "Verify expense count" $false "Failed to get final count: $_"
}

# ===================================================================
# TEST 9: Delete Test Expense
# ===================================================================
if ($null -ne $createdId) {
    Write-Host "TEST 9: Deleting test expense..." -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$API_BASE/$createdId" -Method DELETE
        Test-Result "DELETE expense" $true "Deleted expense ID $createdId"
        
        # Verify deletion
        try {
            $deleted = Invoke-RestMethod -Uri "$API_BASE/$createdId" -Method GET
            Test-Result "Verify deletion" $false "Expense still exists after deletion"
        } catch {
            # 404 expected after deletion
            if ($_.Exception.Response.StatusCode.value__ -eq 404) {
                Test-Result "Verify deletion" $true "Expense successfully removed from database"
            } else {
                Test-Result "Verify deletion" $false "Unexpected error: $_"
            }
        }
    } catch {
        Test-Result "DELETE expense" $false "Failed to delete expense: $_"
    }
}

# ===================================================================
# TEST 10: Clean Up Batch Test Expenses
# ===================================================================
Write-Host "TEST 10: Cleaning up batch test expenses..." -ForegroundColor Yellow
$cleanupSuccess = $true
foreach ($id in $batchIds) {
    try {
        Invoke-RestMethod -Uri "$API_BASE/$id" -Method DELETE
    } catch {
        $cleanupSuccess = $false
    }
}

if ($cleanupSuccess -and $batchIds.Count -gt 0) {
    Test-Result "Cleanup test data" $true "Removed $($batchIds.Count) test expense(s)"
} elseif ($batchIds.Count -eq 0) {
    Test-Result "Cleanup test data" $true "No cleanup needed"
} else {
    Test-Result "Cleanup test data" $false "Some test expenses may not have been deleted"
}

# ===================================================================
# TEST SUMMARY
# ===================================================================
Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "           TEST SUMMARY                      " -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Tests Passed: $testsPassed" -ForegroundColor Green
Write-Host "Tests Failed: $testsFailed" -ForegroundColor $(if ($testsFailed -eq 0) { "Green" } else { "Red" })
Write-Host "Total Tests:  $($testsPassed + $testsFailed)" -ForegroundColor Cyan
Write-Host ""

if ($testsFailed -eq 0) {
    Write-Host "✓ ALL TESTS PASSED - Integration working correctly!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Your application stack is fully integrated:" -ForegroundColor Cyan
    Write-Host "  ✓ Backend API (Spring Boot)" -ForegroundColor Gray
    Write-Host "  ✓ Database (MySQL)" -ForegroundColor Gray
    Write-Host "  ✓ CRUD Operations (Create, Read, Update, Delete)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Next step: Test the frontend by opening:" -ForegroundColor Yellow
    Write-Host "  frontend\index.html in your browser" -ForegroundColor White
} else {
    Write-Host "✗ SOME TESTS FAILED - Please review errors above" -ForegroundColor Red
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "  1. Backend not running → Start with: .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
    Write-Host "  2. MySQL not running → Start MySQL service" -ForegroundColor Gray
    Write-Host "  3. Wrong credentials → Check application.properties" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
