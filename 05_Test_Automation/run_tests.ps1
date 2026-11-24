# Compiles the Online Examination System + JUnit 5 tests and runs the test suite.
# Usage:  .\run_tests.ps1                      -> run all tests, XML report in .\out\reports
#         .\run_tests.ps1 -ReportDir <path>    -> write the JUnit XML report elsewhere
#         .\run_tests.ps1 -Demo                -> run the console demo application instead
#         .\run_tests.ps1 -Web                 -> start the web UI on http://localhost:9090/
#         .\run_tests.ps1 -Web -ExamSeconds 60 -> web UI with a 1-minute exam (to demo time-out)
param(
    [string]$ReportDir = "out/reports",
    [switch]$Demo,
    [switch]$Web,
    [int]$ExamSeconds = 600,
    [int]$Port = 9090
)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# Use the java.exe that sits next to javac (avoids picking up an older JRE on PATH).
$javac = (Get-Command javac -ErrorAction Stop).Source
$java  = Join-Path (Split-Path $javac) "java.exe"
$junit = "lib/junit-platform-console-standalone-1.11.4.jar"

if (Test-Path out/main) { Remove-Item -Recurse -Force out/main }
if (Test-Path out/test) { Remove-Item -Recurse -Force out/test }

$mainSources = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName
& $javac -d out/main $mainSources
if ($LASTEXITCODE -ne 0) { throw "Compilation of application failed" }

if ($Demo) {
    & $java -cp out/main com.oes.Main
    exit $LASTEXITCODE
}

if ($Web) {
    & $java "-Does.examSeconds=$ExamSeconds" "-Does.port=$Port" -cp out/main com.oes.web.WebServer
    exit $LASTEXITCODE
}

$testSources = Get-ChildItem -Recurse src/test/java -Filter *.java | ForEach-Object FullName
& $javac -d out/test -cp "out/main;$junit" $testSources
if ($LASTEXITCODE -ne 0) { throw "Compilation of tests failed" }

& $java -jar $junit execute `
    --class-path "out/main;out/test" `
    --scan-class-path `
    --details tree `
    --disable-banner `
    --reports-dir $ReportDir
exit $LASTEXITCODE
