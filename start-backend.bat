@echo off
title tt1 - Backend (Spring Boot :8080)
cd /d "%~dp0backend"
echo Starting tt1 backend on http://localhost:8080 ...
echo (Wait for "Started ReservationSystemApplication" then open the frontend)
echo.
java -jar target\reservation-system-1.0.0.jar
pause