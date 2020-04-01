@echo off
set/p channel="please input channel:"
set/p buildNum="please input buildType:(1 is release, otherwise is debug)"
set buildType="Debug"
set _buildType="debug"
if %buildNum% == 1 (
    set buildType=Release
    set _buildType=release
)


set/p versionCode="please input versionCode:"

set/p versionName="please input versionName:"

for /f "usebackq delims=" %%i in (`"svn info . | findstr "Rev""`) do set revision=%%i
set revision=%revision:~18%

echo %buildType%
echo %channel%
echo %revision%
echo %versionCode%
echo %versionName%


gradlew clean resguardRelease -Pchannel=%channel% -PbuildType=%buildType% -Prevision=%revision% -PversionCode=%versionCode% -PversionName=%versionName% copySrc
