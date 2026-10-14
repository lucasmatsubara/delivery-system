# Fires N simultaneous POST /orders for the flash-sale dish (requires PowerShell 7+).
# Usage: ./scripts/race-test.ps1 -Requests 50 -DishId 1
# Expected: exactly 10 x 201 and the dish stock ends at 0.
param(
    [int]$Requests = 50,
    [int]$DishId = 1,
    [string]$ServerHost = "localhost"
)

$url = "http://${ServerHost}:8080"
Write-Host "Stock before:" (Invoke-RestMethod "$url/dishes/$DishId" | ConvertTo-Json -Compress)
Write-Host "Sending $Requests simultaneous orders for dish $DishId..."

$body = @{ dishId = $DishId; quantity = 1 } | ConvertTo-Json -Compress
$codes = 1..$Requests | ForEach-Object -Parallel {
    try {
        $response = Invoke-WebRequest -Uri "$($using:url)/orders" -Method Post `
            -ContentType "application/json" -Body $using:body -SkipHttpErrorCheck
        $response.StatusCode
    } catch {
        0
    }
} -ThrottleLimit $Requests

$codes | Group-Object | Sort-Object Name | ForEach-Object { Write-Host "  HTTP $($_.Name): $($_.Count)" }
Write-Host "Stock after:" (Invoke-RestMethod "$url/dishes/$DishId" | ConvertTo-Json -Compress)
