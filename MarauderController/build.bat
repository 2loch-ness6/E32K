@echo off
REM Build script for ESP32 Marauder Controller Android App (Windows)

echo =====================================
echo ESP32 Marauder Controller Build Script
echo =====================================
echo.

REM Check if ANDROID_HOME is set
if "%ANDROID_HOME%"=="" (
    echo ERROR: ANDROID_HOME environment variable is not set.
    echo.
    echo Please set ANDROID_HOME to your Android SDK location:
    echo   set ANDROID_HOME=%%LOCALAPPDATA%%\Android\Sdk
    echo.
    pause
    exit /b 1
)

echo √ ANDROID_HOME: %ANDROID_HOME%
echo.

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found. Are you in the MarauderController directory?
    pause
    exit /b 1
)

REM Parse command line arguments
set BUILD_TYPE=%1
if "%BUILD_TYPE%"=="" set BUILD_TYPE=debug

if "%BUILD_TYPE%"=="debug" (
    echo Building DEBUG APK...
    call gradlew.bat assembleDebug
    set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
) else if "%BUILD_TYPE%"=="release" (
    echo Building RELEASE APK...
    echo Note: Release builds require signing configuration
    call gradlew.bat assembleRelease
    set APK_PATH=app\build\outputs\apk\release\app-release.apk
) else if "%BUILD_TYPE%"=="clean" (
    echo Cleaning build artifacts...
    call gradlew.bat clean
    echo √ Clean complete
    exit /b 0
) else (
    echo Unknown build type: %BUILD_TYPE%
    echo Usage: %~nx0 [debug^|release^|clean]
    pause
    exit /b 1
)

echo.
echo =====================================
echo Build Complete!
echo =====================================
echo.
echo APK location: %APK_PATH%
echo.

REM Check if APK exists
if exist "%APK_PATH%" (
    echo √ APK built successfully
    echo.
    echo To install on a connected device:
    echo   adb install %APK_PATH%
    echo.
    echo Or copy the APK to your Android device and install manually.
) else (
    echo × APK not found at expected location
    echo Build may have failed. Check the output above for errors.
    pause
    exit /b 1
)

pause
