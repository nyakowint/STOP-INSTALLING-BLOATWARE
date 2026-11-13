# Installs Install Guard on a USB-connected phone and turns on its protections
$ErrorActionPreference = "Stop"
$pkg = "dev.installguard"
$svc = "$pkg/$pkg.GuardService"
$apk = "$PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $apk)) { & "$PSScriptRoot\gradlew.bat" assembleDebug }

adb install -r $apk

# Installing over adb skips Android's "restricted settings" lock on sideloaded accessibility services
$cur = (adb shell settings get secure enabled_accessibility_services).Trim()
if (-not $cur -or $cur -eq "null") { $new = $svc }
elseif ($cur -notlike "*$svc*") { $new = "${cur}:$svc" }
else { $new = $cur }
adb shell settings put secure enabled_accessibility_services $new
adb shell settings put secure accessibility_enabled 1

adb shell dpm set-active-admin "$pkg/.AdminReceiver"
adb shell cmd deviceidle whitelist "+$pkg"

adb shell am start -n "$pkg/.MainActivity"
Write-Host "Done. Set a PIN on the phone to finish setup."
