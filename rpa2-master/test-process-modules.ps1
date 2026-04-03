# 流程管理系统测试脚本
# 测试四个模块：流程定义、流程设计、流程维护、流程查询

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  流程管理系统全面测试" -ForegroundColor Cyan
Write-Host "  测试模块：流程定义、流程设计、流程维护、流程查询" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$testResults = @{
    Passed = 0
    Failed = 0
    Total = 0
}

# 辅助函数：发送 HTTP 请求
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
            $content = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $body = $reader.ReadToEnd()
            
            return @{
                StatusCode = $statusCode
                Content = $body | ConvertFrom-Json
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

# 测试计数器
function Test-Start {
    param([string]$Name)
    $testResults.Total++
    Write-Host "  测试：$Name" -NoNewline
}

function Test-Pass {
    param([string]$Message)
    $testResults.Passed++
    Write-Host " - ✅ 通过" -ForegroundColor Green
    if ($Message) {
        Write-Host "    $Message" -ForegroundColor Gray
    }
}

function Test-Fail {
    param([string]$Message)
    $testResults.Failed++
    Write-Host " - ❌ 失败" -ForegroundColor Red
    if ($Message) {
        Write-Host "    $Message" -ForegroundColor Yellow
    }
}

# 登录获取 Token
Write-Host "[准备] 登录系统获取 Token" -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/auth/login" -Body $loginBody

if ($loginResult.Success -and $loginResult.Content.code -eq 200) {
    $token = $loginResult.Content.data.token
    Write-Host "  ✅ 登录成功，获取 Token" -ForegroundColor Green
    $authHeaders = @{
        "Authorization" = "Bearer $token"
    }
}
else {
    Write-Host "  ❌ 登录失败，使用默认 Token" -ForegroundColor Red
    $authHeaders = @{
        "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzQzODQ2MDAwLCJleHAiOjE3NDM5MzI0MDB9.test"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  模块一：流程定义测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 1.1: 创建流程
Test-Start "创建新流程"
$createBody = @{
    processCode = "TEST_PROCESS_001"
    processName = "测试流程 001"
    category = "测试分类"
    version = "1.0.0"
    description = "这是一个测试流程"
    status = 1
    steps = 0
} | ConvertTo-Json

$createResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/process" -Body $createBody -Headers $authHeaders

if ($createResult.Success -and $createResult.StatusCode -eq 200) {
    $processId = $createResult.Content.data.id
    Test-Pass "流程创建成功，ID: $processId"
}
else {
    Test-Fail "流程创建失败：$($createResult.Content.message)"
    $processId = $null
}

# 测试 1.2: 重复流程编码
Test-Start "测试流程编码唯一性"
$dupResult = Invoke-ApiRequest -Method POST -Url "$baseUrl/process" -Body $createBody -Headers $authHeaders -ExpectSuccess $false

if ($dupResult.Success -eq $false -or $dupResult.StatusCode -ne 200) {
    Test-Pass "流程编码唯一性校验通过"
}
else {
    Test-Fail "流程编码唯一性校验失败"
}

# 测试 1.3: 获取流程详情
if ($processId) {
    Test-Start "获取流程详情"
    $detailResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($detailResult.Success -and $detailResult.StatusCode -eq 200) {
        Test-Pass "流程详情获取成功"
    }
    else {
        Test-Fail "流程详情获取失败"
    }
}

# 测试 1.4: 更新流程
if ($processId) {
    Test-Start "更新流程信息"
    $updateBody = @{
        processCode = "TEST_PROCESS_001"
        processName = "测试流程 001 - 已更新"
        category = "测试分类 - 更新"
        version = "1.0.1"
        description = "这是更新后的测试流程"
        status = 1
        steps = 0
    } | ConvertTo-Json
    
    $updateResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId" -Body $updateBody -Headers $authHeaders
    
    if ($updateResult.Success -and $updateResult.StatusCode -eq 200) {
        Test-Pass "流程更新成功"
    }
    else {
        Test-Fail "流程更新失败：$($updateResult.Content.message)"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  模块二：流程设计测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 2.1: 获取流程脚本
if ($processId) {
    Test-Start "获取流程脚本（空脚本）"
    $scriptsResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId/scripts" -Headers $authHeaders
    
    if ($scriptsResult.Success -and $scriptsResult.StatusCode -eq 200) {
        Test-Pass "脚本获取成功"
    }
    else {
        Test-Fail "脚本获取失败"
    }
}

# 测试 2.2: 更新流程脚本（四环节配置）
if ($processId) {
    Test-Start "配置采集环节脚本"
    $collectScript = @"
// 采集环节测试脚本
def collect() {
    def url = "https://api.example.com/test"
    def params = [page: 1, size: 10]
    
    // 发送请求
    def response = httpRequest(url, params)
    
    // 返回采集的数据
    return response.data
}
"@
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = ""
        processScript = ""
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "采集环节配置成功"
    }
    else {
        Test-Fail "采集环节配置失败"
    }
}

# 测试 2.3: 配置解析环节
if ($processId) {
    Test-Start "配置解析环节脚本"
    $parseScript = @"
// 解析环节测试脚本
def parse(data) {
    def result = []
    
    if (data instanceof List) {
        data.each { item ->
            result.add([
                id: item.id,
                name: item.name,
                value: item.value
            ])
        }
    }
    
    return result
}
"@
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = ""
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "解析环节配置成功"
    }
    else {
        Test-Fail "解析环节配置失败"
    }
}

# 测试 2.4: 配置加工环节
if ($processId) {
    Test-Start "配置加工环节脚本"
    $processScript = @"
// 加工环节测试脚本
def process(data) {
    // 数据转换
    data.each { item ->
        item.total = item.price * item.quantity
        
        // 数据校验
        if (!item.name) {
            throw new Exception("名称不能为空")
        }
    }
    
    return data
}
"@
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = $processScript
        saveScript = ""
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "加工环节配置成功"
    }
    else {
        Test-Fail "加工环节配置失败"
    }
}

# 测试 2.5: 配置落库环节
if ($processId) {
    Test-Start "配置落库环节脚本"
    $saveScript = @"
// 落库环节测试脚本
def save(data) {
    // 批量插入
    data.each { item ->
        def sql = """
            INSERT INTO target_table 
            (id, name, value, create_time) 
            VALUES (?, ?, ?, NOW())
        """
        
        // 执行插入
        executeSql(sql, [item.id, item.name, item.value])
    }
    
    return true
}
"@
    
    $scriptsBody = @{
        collectScript = $collectScript
        parseScript = $parseScript
        processScript = $processScript
        saveScript = $saveScript
    } | ConvertTo-Json
    
    $updateScriptsResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/scripts" -Body $scriptsBody -Headers $authHeaders
    
    if ($updateScriptsResult.Success -and $updateScriptsResult.StatusCode -eq 200) {
        Test-Pass "落库环节配置成功"
    }
    else {
        Test-Fail "落库环节配置失败"
    }
}

# 测试 2.6: 验证所有脚本已保存
if ($processId) {
    Test-Start "验证四环节脚本完整性"
    $verifyScriptsResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId/scripts" -Headers $authHeaders
    
    if ($verifyScriptsResult.Success -and 
        $verifyScriptsResult.Content.data.collectScript -and
        $verifyScriptsResult.Content.data.parseScript -and
        $verifyScriptsResult.Content.data.processScript -and
        $verifyScriptsResult.Content.data.saveScript) {
        Test-Pass "四环节脚本完整性验证通过"
    }
    else {
        Test-Fail "四环节脚本完整性验证失败"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  模块三：流程维护测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 3.1: 切换流程状态
if ($processId) {
    Test-Start "切换流程状态（启用 -> 停用）"
    $toggleResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/status" -Headers $authHeaders
    
    if ($toggleResult.Success -and $toggleResult.StatusCode -eq 200) {
        Test-Pass "状态切换成功"
    }
    else {
        Test-Fail "状态切换失败"
    }
}

# 测试 3.2: 再次切换状态
if ($processId) {
    Test-Start "切换流程状态（停用 -> 启用）"
    $toggleResult = Invoke-ApiRequest -Method PUT -Url "$baseUrl/process/$processId/status" -Headers $authHeaders
    
    if ($toggleResult.Success -and $toggleResult.StatusCode -eq 200) {
        Test-Pass "状态再次切换成功"
    }
    else {
        Test-Fail "状态再次切换失败"
    }
}

# 测试 3.3: 验证流程状态
if ($processId) {
    Test-Start "验证流程状态"
    $verifyStatusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($verifyStatusResult.Success -and $verifyStatusResult.Content.data.status -eq 1) {
        Test-Pass "流程状态验证通过（当前为启用状态）"
    }
    else {
        Test-Fail "流程状态验证失败"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  模块四：流程查询测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 4.1: 分页查询所有流程
Test-Start "分页查询所有流程"
$listResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10" -Headers $authHeaders

if ($listResult.Success -and $listResult.StatusCode -eq 200) {
    $total = $listResult.Content.data.total
    Test-Pass "查询成功，共 $total 条记录"
}
else {
    Test-Fail "查询失败"
}

# 测试 4.2: 关键字查询（流程名称）
Test-Start "关键字查询（测试流程）"
$keywordResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=测试" -Headers $authHeaders

if ($keywordResult.Success -and $keywordResult.StatusCode -eq 200) {
    $keywordTotal = $keywordResult.Content.data.total
    Test-Pass "关键字查询成功，找到 $keywordTotal 条匹配记录"
}
else {
    Test-Fail "关键字查询失败"
}

# 测试 4.3: 关键字查询（流程编码）
Test-Start "关键字查询（流程编码）"
$codeResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=TEST" -Headers $authHeaders

if ($codeResult.Success -and $codeResult.StatusCode -eq 200) {
    $codeTotal = $codeResult.Content.data.total
    Test-Pass "编码查询成功，找到 $codeTotal 条匹配记录"
}
else {
    Test-Fail "编码查询失败"
}

# 测试 4.4: 状态筛选查询（启用）
Test-Start "状态筛选查询（启用状态）"
$statusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&status=1" -Headers $authHeaders

if ($statusResult.Success -and $statusResult.StatusCode -eq 200) {
    $statusTotal = $statusResult.Content.data.total
    Test-Pass "状态筛选成功，启用状态共 $statusTotal 条"
}
else {
    Test-Fail "状态筛选失败"
}

# 测试 4.5: 状态筛选查询（停用）
Test-Start "状态筛选查询（停用状态）"
$statusResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&status=0" -Headers $authHeaders

if ($statusResult.Success -and $statusResult.StatusCode -eq 200) {
    $statusTotal = $statusResult.Content.data.total
    Test-Pass "状态筛选成功，停用状态共 $statusTotal 条"
}
else {
    Test-Fail "状态筛选失败"
}

# 测试 4.6: 组合查询（关键字 + 状态）
Test-Start "组合查询（关键字 + 状态）"
$combinedResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=1&pageSize=10&keyword=测试&status=1" -Headers $authHeaders

if ($combinedResult.Success -and $combinedResult.StatusCode -eq 200) {
    $combinedTotal = $combinedResult.Content.data.total
    Test-Pass "组合查询成功，找到 $combinedTotal 条匹配记录"
}
else {
    Test-Fail "组合查询失败"
}

# 测试 4.7: 分页测试（第二页）
Test-Start "分页查询（第二页）"
$page2Result = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/list?page=2&pageSize=10" -Headers $authHeaders

if ($page2Result.Success -and $page2Result.StatusCode -eq 200) {
    Test-Pass "分页查询成功"
}
else {
    Test-Fail "分页查询失败"
}

# 测试 4.8: 查询不存在的流程
Test-Start "查询不存在的流程"
$notFoundResult = Invoke-ApiRequest -Method GET -Url "$baseUrl/process/999999" -Headers $authHeaders -ExpectSuccess $false

if ($notFoundResult.Success -eq $false) {
    Test-Pass "异常处理正确（流程不存在）"
}
else {
    Test-Fail "异常处理不当"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  清理测试数据" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 清理：删除测试流程
if ($processId) {
    Test-Start "删除测试流程"
    $deleteResult = Invoke-ApiRequest -Method DELETE -Url "$baseUrl/process/$processId" -Headers $authHeaders
    
    if ($deleteResult.Success -and $deleteResult.StatusCode -eq 200) {
        Test-Pass "测试流程删除成功"
    }
    else {
        Test-Fail "测试流程删除失败"
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "  总测试数：$($testResults.Total)" -ForegroundColor White
Write-Host "  ✅ 通过：$($testResults.Passed)" -ForegroundColor Green
Write-Host "  ❌ 失败：$($testResults.Failed)" -ForegroundColor Red
Write-Host ""

$passRate = [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2)
Write-Host "  通过率：$passRate%" -ForegroundColor $(if ($passRate -eq 100) { "Green" } elseif ($passRate -ge 80) { "Yellow" } else { "Red" })
Write-Host ""

if ($passRate -eq 100) {
    Write-Host "🎉 所有测试通过！系统运行正常！" -ForegroundColor Green
}
elseif ($passRate -ge 80) {
    Write-Host "⚠️  大部分测试通过，系统基本功能正常" -ForegroundColor Yellow
}
else {
    Write-Host "❌ 测试失败较多，请检查系统" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
