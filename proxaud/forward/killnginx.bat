@echo off
cd ..\..\..\..\nginx-1.21.1
nginx.exe -s stop
nginx.exe -s quit
taskkill /F /IM nginx.exe