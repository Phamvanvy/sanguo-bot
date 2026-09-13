param(
    [ValidateSet("Brave", "Edge", "Chrome")]
    [string]$Browser = "Brave",
    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[A-Za-z0-9._-]+$")]
    [string]$Profile
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$browserCandidates = @{
    Brave = @(
        "$env:ProgramFiles\BraveSoftware\Brave-Browser\Application\brave.exe",
        "${env:ProgramFiles(x86)}\BraveSoftware\Brave-Browser\Application\brave.exe"
    )
    Edge = @(
        "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
        "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe"
    )
    Chrome = @(
        "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
        "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe"
    )
}

$browserExe = $browserCandidates[$Browser] | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $browserExe) {
    throw "Khong tim thay $Browser tren may nay."
}

$profileDir = Join-Path $repoRoot ".browser-instances\$($Browser.ToLowerInvariant())-$Profile"
$extensionDir = Join-Path $repoRoot "extension"
New-Item -ItemType Directory -Path $profileDir -Force | Out-Null

$browserArgs = @(
    "--user-data-dir=$profileDir",
    "--load-extension=$extensionDir",
    "--new-window",
    "--start-maximized",
    "--disable-background-timer-throttling",
    "--disable-backgrounding-occluded-windows",
    "--disable-renderer-backgrounding",
    "--disable-features=CalculateNativeWinOcclusion,IntensiveWakeUpThrottling",
    "https://play.minhchauh5.com/"
)

Start-Process -FilePath $browserExe -ArgumentList $browserArgs
Write-Host "Da mo $Browser profile '$Profile' voi background throttling bi tat."
