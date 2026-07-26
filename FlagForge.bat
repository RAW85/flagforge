@echo off
cd /d "%~dp0"
title FlagForge Control Center
chcp 65001 >nul 2>&1
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0FlagForge.ps1"
if errorlevel 1 (
  echo.
  echo FlagForge menu exited with an error.
  pause
)
