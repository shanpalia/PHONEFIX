@echo off
setlocal
set "GRADLE_VERSION=9.3.1"
if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "DIST_DIR=%GRADLE_USER_HOME%\phonefix-dists\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%DIST_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat"
set "ZIP=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
set "URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%GRADLE_BIN%" (
  if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%ZIP%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%DIST_DIR%'"
)
call "%GRADLE_BIN%" %*
endlocal
