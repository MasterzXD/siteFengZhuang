@echo off

cd /d %~dp0
del /s /Q release
echo ��ǰ�̷���%~d0
echo ��ǰ·����%cd%
echo ��ǰִ�������У�%0
echo ��ǰbat�ļ�·����%~dp0

for /d  %%i in (newapp\*) do (
	echo %%i
)

::xcopy newapp newapp1 /e

set cmd1=gradlew clean assembleRelease
%cmd1%

::start explorer release

pause