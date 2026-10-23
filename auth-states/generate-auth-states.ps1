# Authentication State Generator for MACSuite UI Automation
# This script automates the generation of authenticated browser states for all role/environment combinations
# Prerequisites: playwright-cli installed globally (npm install -g @playwright/cli@latest)

param(
    [switch]$SkipExisting = $false,  # Skip generation if file already exists
    [string]$OnlyRole = "",           # Generate only for specific role (e.g., "BCT", "SO", "TA")
    [string]$OnlyEnv = ""             # Generate only for specific environment (e.g., "QA", "UAT", "SEI")
)

# Color output functions
function Write-Success { param($msg) Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Failure { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Step { param($msg) Write-Host "[STEP] $msg" -ForegroundColor Yellow }

# Load .env file
$parentDir = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $parentDir ".env"
if (-not (Test-Path $envFile)) {
    Write-Failure ".env file not found at: $envFile"
    exit 1
}

Write-Info "Loading credentials from .env file..."
$envVars = @{}
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim().Trim('"')
        $envVars[$key] = $value
    }
}

# Define role/environment combinations
$roles = @(
    @{Name="BCT"; Label="Budget & Contract Tracker"},
    @{Name="SO"; Label="State Operations"},
    @{Name="TA"; Label="Technical Advisor"},
    @{Name="PIER"; Label="PIER"},
    @{Name="PRA"; Label="Program Regulatory Advisor"},
    @{Name="HMH"; Label="Hiring Metrics Hub"},
    @{Name="VE"; Label="Vendor Engagement"},
    @{Name="MFP"; Label="MFP Admin"},
    @{Name="WFTCL"; Label="Workflow & Tracking Coordinator Lead"},
    @{Name="PRAREADONLY"; Label="PRA Read-Only"}
)

$environments = @(
    @{Name="QA"; UrlKey="APP_URL_QA"},
    @{Name="UAT"; UrlKey="APP_URL_UAT"},
    @{Name="SEI"; UrlKey="APP_URL_SEI"}
)

# Filter by parameters if provided
if ($OnlyRole) {
    $roles = $roles | Where-Object { $_.Name -eq $OnlyRole.ToUpper() }
    if ($roles.Count -eq 0) {
        Write-Failure "Role '$OnlyRole' not found. Available: BCT, SO, TA, PIER, PRA, HMH, VE, MFP, WFTCL, PRAREADONLY"
        exit 1
    }
}

if ($OnlyEnv) {
    $environments = $environments | Where-Object { $_.Name -eq $OnlyEnv.ToUpper() }
    if ($environments.Count -eq 0) {
        Write-Failure "Environment '$OnlyEnv' not found. Available: QA, UAT, SEI"
        exit 1
    }
}

# Create subdirectories
foreach ($env in $environments) {
    $dirPath = Join-Path $PSScriptRoot $env.Name.ToLower()
    if (-not (Test-Path $dirPath)) {
        New-Item -ItemType Directory -Path $dirPath -Force | Out-Null
        Write-Success "Created directory: $($env.Name.ToLower())/"
    }
}

# Playwright CLI path
$playwrightCli = "C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd"
if (-not (Test-Path $playwrightCli)) {
    Write-Failure "playwright-cli not found at: $playwrightCli"
    Write-Info "Install with: npm install -g @playwright/cli@latest"
    exit 1
}

# Statistics
$totalCount = 0
$successCount = 0
$skipCount = 0
$failCount = 0

Write-Info "Starting authentication state generation..."
Write-Info "Total combinations: $($roles.Count * $environments.Count)"
Write-Host ""

# Generate auth states
foreach ($role in $roles) {
    foreach ($env in $environments) {
        $totalCount++
        
        $roleName = $role.Name
        $envName = $env.Name
        $outputFile = Join-Path $PSScriptRoot "$($envName.ToLower())\$($roleName.ToLower())-$($envName.ToLower()).json"
        
        Write-Step "[$totalCount/$($roles.Count * $environments.Count)] $($role.Label) - $envName"
        
        # Check if file exists and skip if requested
        if ($SkipExisting -and (Test-Path $outputFile)) {
            Write-Info "  Skipping (file exists): $outputFile"
            $skipCount++
            continue
        }
        
        # Get credentials
        $usernameKey = "$($roleName)_$($envName)_USERNAME"
        $passwordKey = "$($roleName)_$($envName)_PASSWORD"
        $urlKey = $env.UrlKey
        
        if (-not $envVars.ContainsKey($usernameKey)) {
            Write-Failure "  Missing credential: $usernameKey in .env"
            $failCount++
            continue
        }
        
        if (-not $envVars.ContainsKey($passwordKey)) {
            Write-Failure "  Missing credential: $passwordKey in .env"
            $failCount++
            continue
        }
        
        if (-not $envVars.ContainsKey($urlKey)) {
            Write-Failure "  Missing URL: $urlKey in .env"
            $failCount++
            continue
        }
        
        $username = $envVars[$usernameKey]
        $password = $envVars[$passwordKey]
        $url = $envVars[$urlKey]
        
        Write-Info "  URL: $url"
        Write-Info "  Username: $username"
        
        try {
            # Open browser with the URL
            Write-Info "  Opening browser..."
            $sessionName = "authgen-$roleName-$envName"
            & $playwrightCli -s=$sessionName open --browser=chrome $url | Out-Null
            Start-Sleep -Seconds 3
            
            # Take snapshot to get element references
            Write-Info "  Taking snapshot..."
            $snapshot = & $playwrightCli -s=$sessionName snapshot --raw
            
            # Parse snapshot to find username/password fields and login button
            # Salesforce login typically has:
            # - input[id="username"] or getByLabel("Username")
            # - input[id="password"] or getByLabel("Password")
            # - button[id="Login"] or getByRole('button', { name: 'Log In' })
            
            Write-Info "  Filling username..."
            & $playwrightCli -s=$sessionName fill "#username" $username | Out-Null
            Start-Sleep -Seconds 1
            
            Write-Info "  Filling password..."
            & $playwrightCli -s=$sessionName fill "#password" $password | Out-Null
            Start-Sleep -Seconds 1
            
            Write-Info "  Clicking login button..."
            & $playwrightCli -s=$sessionName click "#Login" | Out-Null
            Start-Sleep -Seconds 8  # Wait for login to complete
            
            # Verify login succeeded by checking URL (should not be on login page)
            $currentUrl = & $playwrightCli -s=$sessionName eval "window.location.href" --raw
            
            if ($currentUrl -match "login" -or $currentUrl -match "secur/frontdoor") {
                Write-Failure "  Login appears to have failed (still on login page)"
                & $playwrightCli -s=$sessionName close | Out-Null
                $failCount++
                continue
            }
            
            Write-Info "  Login successful, saving state..."
            & $playwrightCli -s=$sessionName state-save $outputFile | Out-Null
            
            # Close browser
            & $playwrightCli -s=$sessionName close | Out-Null
            
            # Verify file was created
            if (Test-Path $outputFile) {
                $fileSize = (Get-Item $outputFile).Length
                Write-Success "  Generated: $outputFile ($([math]::Round($fileSize/1KB, 2)) KB)"
                $successCount++
            } else {
                Write-Failure "  Failed to create file: $outputFile"
                $failCount++
            }
            
        } catch {
            Write-Failure "  Exception: $($_.Exception.Message)"
            # Try to close browser session
            try { & $playwrightCli -s=$sessionName close | Out-Null } catch {}
            $failCount++
        }
        
        Write-Host ""
    }
}

# Summary
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Authentication State Generation Complete" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Total:    $totalCount" -ForegroundColor White
Write-Success "Success:  $successCount"
if ($skipCount -gt 0) { Write-Info "Skipped:  $skipCount" }
if ($failCount -gt 0) { Write-Failure "Failed:   $failCount" }
Write-Host ""

if ($failCount -gt 0) {
    Write-Info "Review failed combinations above and verify credentials in .env"
    exit 1
}

Write-Success "All auth states generated successfully!"
Write-Info "Files saved to: $PSScriptRoot\{qa|uat|sei}\"
Write-Host ""
Write-Info "Next steps:"
Write-Host "  1. Verify auth states work: playwright-cli open <URL> --state-load <file>"
Write-Host "  2. Run tests with auth states: mvn clean test"
Write-Host "  3. Regenerate periodically (recommended: every 30 days)"
