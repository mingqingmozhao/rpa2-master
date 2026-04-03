# Process Management Module Test Script
# Test 4 modules: Process Definition, Process Design, Process Maintenance, Process Query

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Process Management System Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$testResults = @{
    Passed = 0
    Failed = 0
    Total = 0
}

function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body,
        [hashtable]$Headers,
        [bool]$ExpectSuccess = $true
    )
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
            Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-WebRequest @params -UseBasicParsing -ErrorAction Stop
        return @{
            StatusCode = $response.StatusCode
            Content = $response.Content | ConvertFrom-Json
            Success = $true
        }
    }
    catch {
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            return @{
                StatusCode = $statusCode
                Content = $_.Exception.Message
                Success = -not $ExpectSuccess
            }
        }
        return @{
            StatusCode = 0
            Content = $_.Exception.Message
            Success = $false
        }
    }
}

function Test-Start {
    param([string]$Name)
    $testResults.Total++
    Write-Host "  Test: $Name" -NoNewline
}

function Test-Pass {
    param([string]$Message)
    $testResults.Passed++
    Write-Host " - PASS" -ForegroundColor Green
    if ($Message) {
        Write-Host "    $Message" -ForegroundColor Gray
    }
}

function Test-Fail {
    param([string]$Message)
    $testResults.Failed++
    Write-Host " - FAIL" -ForegroundColor Red
    if ($Message) {
        Write-Host "    $Message" -ForegroundColor Yellow
    }
}

# Login
Write-Host "[Preparation] Login to get Token" -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/auth/login" -Body $loginBody

if ($loginResult.Success -and $loginResult.Content.code -eq 200) {
    $token = $loginResult.Content.data.token
    Write-Host "  Login successful, Token obtained" -ForegroundColor Green
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
}
else {
    Write-Host "  Login failed, using default Token" -ForegroundColor Red
    $authHeaders = @{
        "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzQzODQ2MDAwLCJleHAiOjE3NDM5MzI0MDB9.test"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Module 1: Process Definition" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1.1: Create Process
Test-Start "Create new process"
$createBody = @{
    processCode = "TEST_PROC_001"
    processName = "Test Process 001"
    category = "Test Category"
    version = "1.0.0"
    description = "This is a test process"
    status = 1
    steps = 0
} | ConvertTo-Json

$createResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/process" -Body $createBody -Headers $authHeaders

if ($createResult.Success -and $createResult.StatusCode -eq 200) {
    $processId = $createResult.Content.data.id
    Test-Pass "Process created successfully, ID: $processId"
}
else {
    Test-Fail "Process creation failed: $($createResult.Content.message)"
    $processId = $null
}

# Test 1.2: Duplicate process code
Test-Start "Test process code uniqueness"
$dupResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/process" -Body $createBody -Headers $authHeaders -ExpectSuccess $false

if ($dupResult.Success -eq $false -or $dupResult.StatusCode -ne 200) {
    Test-Pass "Process code uniqueness validation passed"
}
else {
    Test-Fail "Process code uniqueness validation failed"
}

# Test 1.3: Get process detail
if ($processId) {
    Test-Start "Get process detail"
    $detailResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($detailResult.Success -and $detailResult.StatusCode -eq 200) {
        Test-Pass "Process detail retrieved successfully"
    }
    else {
        Test-Fail "Process detail retrieval failed"
    }
}

# Test 1.4: Update process
if ($processId) {
    Test-Start "Update process information"
    $updateBody = @{
        processCode = "TEST_PROC_001"
        processName = "Test Process 001 - Updated"
        category = "Test Category - Updated"
        version = "1.0.1"
        description = "This is updated test process"
        status = 1
        steps = 0
    } | ConvertTo-Json
    
    $updateResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId" -Body $updateBody -Headers $authHeaders
    
    if ($updateResult.Success -and $updateResult.StatusCode -eq 200) {
        Test-Pass "Process updated successfully"
    }
    else {
        Test-Fail "Process update failed: $($updateResult.Content.message)"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Module 2: Process Design" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 2.1: Get process scripts
if ($processId) {
    Test-Start "Get process scripts (empty)"
    $scriptsResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId/scripts" -Headers $authHeaders
    
    if ($scriptsResult.Success -and $scriptsResult.StatusCode -eq 200) {
        Test-Pass "Scripts retrieved successfully"
    }
    else {
        Test-Fail "Scripts retrieval failed"
    }
}

# Test 2.2: Update process scripts (4 steps)
if ($processId) {
    Test-Start "Configure collection step script"
    $collectScript = "// Collection step test script`ndef collect() {`n    def url = 'https://api.example.com/test'`n    def response = httpRequest(url, [page: 1, size: 10])`n    return response.data`n}"
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = ""
        processScript = ""
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "Collection step configured successfully"
    }
    else {
        Test-Fail "Collection step configuration failed"
    }
}

# Test 2.3: Configure parse step
if ($processId) {
    Test-Start "Configure parse step script"
    $parseScript = "// Parse step test script`ndef parse(data) {`n    def result = []`n    if (data instanceof List) {`n        data.each { item ->`n            result.add([id: item.id, name: item.name])`n        }`n    }`n    return result`n}"
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = ""
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "Parse step configured successfully"
    }
    else {
        Test-Fail "Parse step configuration failed"
    }
}

# Test 2.4: Configure process step
if ($processId) {
    Test-Start "Configure process step script"
    $processScript = "// Process step test script`ndef process(data) {`n    data.each { item ->`n        item.total = item.price * item.quantity`n    }`n    return data`n}"
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = $processScript
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "Process step configured successfully"
    }
    else {
        Test-Fail "Process step configuration failed"
    }
}

# Test 2.5: Configure save step
if ($processId) {
    Test-Start "Configure save step script"
    $saveScript = "// Save step test script`ndef save(data) {`n    data.each { item ->`n        def sql = 'INSERT INTO target_table (id, name, value) VALUES (?, ?, ?)'`n        executeSql(sql, [item.id, item.name, item.value])`n    }`n    return true`n}"
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = $processScript
        saveScript = $saveScript
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "Save step configured successfully"
    }
    else {
        Test-Fail "Save step configuration failed"
    }
}

# Test 2.6: Verify all scripts saved
if ($processId) {
    Test-Start "Verify four steps scripts integrity"
    $verifyScriptsResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId/scripts" -Headers $authHeaders
    
    if ($verifyScriptsResult.Success -and 
        $verifyScriptsResult.Content.data.collectScript -and
        $verifyScriptsResult.Content.data.parseScript -and
        $verifyScriptsResult.Content.data.processScript -and
        $verifyScriptsResult.Content.data.saveScript) {
        Test-Pass "Four steps scripts integrity verification passed"
    }
    else {
        Test-Fail "Four steps scripts integrity verification failed"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Module 3: Process Maintenance" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 3.1: Toggle process status
if ($processId) {
    Test-Start "Toggle process status (Enable -> Disable)"
    $toggleResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/status" -Headers $authHeaders
    
    if ($toggleResult.Success -and $toggleResult.StatusCode -eq 200) {
        Test-Pass "Status toggled successfully"
    }
    else {
        Test-Fail "Status toggle failed"
    }
}

# Test 3.2: Toggle status again
if ($processId) {
    Test-Start "Toggle process status (Disable -> Enable)"
    $toggleResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/status" -Headers $authHeaders
    
    if ($toggleResult.Success -and $toggleResult.StatusCode -eq 200) {
        Test-Pass "Status toggled again successfully"
    }
    else {
        Test-Fail "Status toggle again failed"
    }
}

# Test 3.3: Verify process status
if ($processId) {
    Test-Start "Verify process status"
    $verifyStatusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($verifyStatusResult.Success -and $verifyStatusResult.Content.data.status -eq 1) {
        Test-Pass "Process status verification passed (Enabled)"
    }
    else {
        Test-Fail "Process status verification failed"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Module 4: Process Query" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 4.1: List all processes
Test-Start "List all processes with pagination"
$listResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10" -Headers $authHeaders

if ($listResult.Success -and $listResult.StatusCode -eq 200) {
    $total = $listResult.Content.data.total
    Test-Pass "Query successful, total: $total records"
}
else {
    Test-Fail "Query failed"
}

# Test 4.2: Keyword query (process name)
Test-Start "Keyword query (Test Process)"
$keywordResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=Test" -Headers $authHeaders

if ($keywordResult.Success -and $keywordResult.StatusCode -eq 200) {
    $keywordTotal = $keywordResult.Content.data.total
    Test-Pass "Keyword query successful, found: $keywordTotal matches"
}
else {
    Test-Fail "Keyword query failed"
}

# Test 4.3: Keyword query (process code)
Test-Start "Keyword query (process code)"
$codeResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=TEST" -Headers $authHeaders

if ($codeResult.Success -and $codeResult.StatusCode -eq 200) {
    $codeTotal = $codeResult.Content.data.total
    Test-Pass "Code query successful, found: $codeTotal matches"
}
else {
    Test-Fail "Code query failed"
}

# Test 4.4: Status filter query (Enabled)
Test-Start "Status filter query (Enabled)"
$statusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&status=1" -Headers $authHeaders

if ($statusResult.Success -and $statusResult.StatusCode -eq 200) {
    $statusTotal = $statusResult.Content.data.total
    Test-Pass "Status filter successful, enabled: $statusTotal"
}
else {
    Test-Fail "Status filter failed"
}

# Test 4.5: Status filter query (Disabled)
Test-Start "Status filter query (Disabled)"
$statusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&status=0" -Headers $authHeaders

if ($statusResult.Success -and $statusResult.StatusCode -eq 200) {
    $statusTotal = $statusResult.Content.data.total
    Test-Pass "Status filter successful, disabled: $statusTotal"
}
else {
    Test-Fail "Status filter failed"
}

# Test 4.6: Combined query (keyword + status)
Test-Start "Combined query (keyword + status)"
$combinedResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=Test&status=1" -Headers $authHeaders

if ($combinedResult.Success -and $combinedResult.StatusCode -eq 200) {
    $combinedTotal = $combinedResult.Content.data.total
    Test-Pass "Combined query successful, found: $combinedTotal matches"
}
else {
    Test-Fail "Combined query failed"
}

# Test 4.7: Pagination test (page 2)
Test-Start "Pagination query (page 2)"
$page2Result = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=2&pageSize=10" -Headers $authHeaders

if ($page2Result.Success -and $page2Result.StatusCode -eq 200) {
    Test-Pass "Pagination query successful"
}
else {
    Test-Fail "Pagination query failed"
}

# Test 4.8: Query non-existent process
Test-Start "Query non-existent process"
$notFoundResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/999999" -Headers $authHeaders -ExpectSuccess $false

if ($notFoundResult.Success -eq $false) {
    Test-Pass "Exception handling correct (process not found)"
}
else {
    Test-Fail "Exception handling incorrect"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Cleanup Test Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Cleanup: Delete test process
if ($processId) {
    Test-Start "Delete test process"
    $deleteResult = Invoke-ApiRequest -Method DELETE -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($deleteResult.Success -and $deleteResult.StatusCode -eq 200) {
        Test-Pass "Test process deleted successfully"
    }
    else {
        Test-Fail "Test process deletion failed"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "  Total Tests: $($testResults.Total)" -ForegroundColor White
Write-Host "  Passed: $($testResults.Passed)" -ForegroundColor Green
Write-Host "  Failed: $($testResults.Failed)" -ForegroundColor Red
Write-Host ""

$passRate = [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2)
Write-Host "  Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -eq 100) { "Green" } elseif ($passRate -ge 80) { "Yellow" } else { "Red" })
Write-Host ""

if ($passRate -eq 100) {
    Write-Host "All tests passed! System is working properly!" -ForegroundColor Green
}
elseif ($passRate -ge 80) {
    Write-Host "Most tests passed, system basic functions are normal" -ForegroundColor Yellow
}
else {
    Write-Host "Many tests failed, please check the system" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
