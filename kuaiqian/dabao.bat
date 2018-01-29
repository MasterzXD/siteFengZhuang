@echo off

cd /d %~dp0
del /s /Q release
echo 当前盘符：%~d0
echo 当前路径：%cd%
echo 当前执行命令行：%0
echo 当前bat文件路径：%~dp0

for /d  %%i in (newapp\*) do (
	echo %%i
)

::xcopy newapp newapp1 /e

set cmd1=gradlew clean assembleRelease
%cmd1%

::start explorer release

pause