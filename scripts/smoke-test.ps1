# End-to-end smoke test for campus-network-repair.
# Prerequisites: app running on $BaseUrl, database imported from database/seed.sql.
# The script creates a temporary device and repair orders; re-import seed.sql to clean up.
param(
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$script:failed = 0

function Call-Api {
    param([string]$Method, [string]$Path, [string]$Token, $Body)
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    $req = @{ Uri = "$BaseUrl$Path"; Method = $Method; UseBasicParsing = $true; TimeoutSec = 20; Headers = $headers }
    if ($null -ne $Body) {
        $req['ContentType'] = 'application/json; charset=utf-8'
        $req['Body'] = ($Body | ConvertTo-Json -Compress -Depth 6)
    }
    try {
        $r = Invoke-WebRequest @req
        [pscustomobject]@{ Status = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $status = 0
        if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
        [pscustomobject]@{ Status = $status; Body = $_.ErrorDetails.Message }
    }
}

function Assert-Status {
    param([string]$Name, $Result, [int]$Expected)
    $ok = $Result.Status -eq $Expected
    if (-not $ok) { $script:failed++ }
    $flag = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Output ("[{0}] {1} -> HTTP {2} (expected {3})" -f $flag, $Name, $Result.Status, $Expected)
}

function Login {
    param([string]$User)
    $res = Call-Api 'POST' '/api/auth/login' $null @{ username = $User; password = '123456' }
    ($res.Body | ConvertFrom-Json).data.token
}

Write-Output '== auth =='
$admin = Login 'admin'
$tech = Login 'tech01'
$stu07 = Login 'stu07'
$stu01 = Login 'stu01'
Assert-Status 'health' (Call-Api 'GET' '/api/health' $null $null) 200
Assert-Status 'no token -> 401' (Call-Api 'GET' '/api/repair-orders/my' $null $null) 401
Assert-Status 'other user order -> 403' (Call-Api 'GET' '/api/repair-orders/12' $stu01 $null) 403
Assert-Status 'owner order -> 200' (Call-Api 'GET' '/api/repair-orders/12' $stu07 $null) 200

Write-Output '== device =='
Assert-Status 'device page' (Call-Api 'GET' '/api/devices?page=1&size=3' $admin $null) 200
Assert-Status 'tech create device -> 403' (Call-Api 'POST' '/api/devices' $tech @{
        deviceCode = 'DEV-DENY'; deviceName = 'deny'; roomName = 'test'
    }) 403
$tag = Get-Random -Minimum 10000 -Maximum 99999
$created = Call-Api 'POST' '/api/devices' $admin @{
    deviceCode = "DEV-TEST-$tag"; deviceName = 'test switch'; roomName = 'test room'; status = 'NORMAL'
}
Assert-Status 'admin create device' $created 200
$deviceId = ($created.Body | ConvertFrom-Json).data.id
Assert-Status 'admin update device' (Call-Api 'PUT' "/api/devices/$deviceId" $admin @{
        deviceCode = "DEV-TEST-$tag"; deviceName = 'test switch v2'; roomName = 'test room'; status = 'FAULT'
    }) 200
Assert-Status 'delete device without orders' (Call-Api 'DELETE' "/api/devices/$deviceId" $admin $null) 200
Assert-Status 'delete device with orders -> 400' (Call-Api 'DELETE' '/api/devices/1' $admin $null) 400

Write-Output '== state machine =='
$created = Call-Api 'POST' '/api/repair-orders' $stu07 @{
    deviceId = 12; faultDesc = 'smoke test: fiber transceiver down'; priority = 'NORMAL'
}
Assert-Status 'create order' $created 200
$orderId = ($created.Body | ConvertFrom-Json).data.id
Assert-Status 'accept' (Call-Api 'POST' "/api/repair-orders/$orderId/accept" $tech @{
        remark = 'accepted'; content = 'call confirmed'
    }) 200
Assert-Status 'duplicate accept -> 400' (Call-Api 'POST' "/api/repair-orders/$orderId/accept" $tech @{}) 400
Assert-Status 'start repair' (Call-Api 'POST' "/api/repair-orders/$orderId/start-repair" $tech @{
        content = 'replace module'
    }) 200
Assert-Status 'finish' (Call-Api 'POST' "/api/repair-orders/$orderId/finish" $tech @{
        content = 'link recovered'
    }) 200
Assert-Status 'other user confirm -> 403' (Call-Api 'POST' "/api/repair-orders/$orderId/confirm" $stu01 @{
        result = 'PASS'; score = 5
    }) 403
Assert-Status 'owner confirm PASS' (Call-Api 'POST' "/api/repair-orders/$orderId/confirm" $stu07 @{
        result = 'PASS'; score = 5; comment = 'ok'
    }) 200
Assert-Status 'confirm again -> 400' (Call-Api 'POST' "/api/repair-orders/$orderId/confirm" $stu07 @{
        result = 'PASS'; score = 5
    }) 400
Assert-Status 'order detail' (Call-Api 'GET' "/api/repair-orders/$orderId" $stu07 $null) 200

Write-Output '== query and stats =='
Assert-Status 'page by status' (Call-Api 'GET' '/api/repair-orders?status=CLOSED&page=1&size=5' $admin $null) 200
Assert-Status 'page by keyword' (Call-Api 'GET' '/api/repair-orders?keyword=RO2026&page=1&size=5' $tech $null) 200
Assert-Status 'stats as technician' (Call-Api 'GET' '/api/stats/overview' $tech $null) 200
Assert-Status 'stats as reporter -> 403' (Call-Api 'GET' '/api/stats/overview' $stu07 $null) 403

if ($script:failed -gt 0) {
    Write-Output "FAILED: $($script:failed) case(s)"
    exit 1
}
Write-Output 'ALL PASSED'
