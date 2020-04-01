set folder=E:\Magikoly\
if not exist %folder% md %folder%
echo %_buildType%
echo %channel%
echo %revision%
echo %versionCode%
echo %versionName%
set yyyy=%date:~,4%
set mm=%date:~5,2%
set day=%date:~8,2%
set "YYYYmmdd=%yyyy%%mm%%day%"
rem 把年月日串中的空格替换为0
set "YYYYmmdd=%YYYYmmdd: =0%"
echo "YYYYmmdd%YYYYmmdd%YYYYmmdd"
rem 根据当前时间获取，时分秒串
set hh=%time:~0,2%
set mi=%time:~3,2%
set ss=%time:~6,2%
set "hhmiss=%hh%%mi%%ss%"
set "hhmiss=%hhmiss: =0%"
echo "hhmiss%Time%hhmiss"
echo %hhmiss%
rem 把时间串中的:替换为0
set "hhmiss=%hhmiss::=0%"
rem 把时间串中的空格替换为0
set "hhmiss=%hhmiss: =0%"
rem 根据日期时间生成文件名称，中间以HH区分日期和时间部分
set "filetime=%YYYYmmdd%%hhmiss%"
echo %filename%
copy /y Magikoly\build\outputs\apk\release\AndResGuard_Magikoly-%_buildType%-v%versionName%-vc%versionCode%_%channel%_svn%revision%\Magikoly-%_buildType%-v%versionName%-vc%versionCode%_%channel%_svn%revision%_signed_7zip_aligned.apk  %folder%Magikoly-%_buildType%-v%versionName%-vc%versionCode%_%channel%_svn%revision%_signed_7zip_aligned_%filetime%.apk
