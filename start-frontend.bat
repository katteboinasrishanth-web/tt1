@echo off
title tt1 - Frontend (React + Vite :5173)
cd /d "%~dp0frontend"
if not exist node_modules (
  echo Installing frontend dependencies (first run)...
  call npm install
)
echo Starting tt1 frontend ...
echo Open http://localhost:5173 in your browser.
echo.
call npm run dev