@echo off
title Sistema CRUD JavaFX - Estudiantes
echo ========================================================
echo Iniciando Sistema de Gestion de Estudiantes (JavaFX)...
echo ========================================================

REM Deteccion automatica del JDK instalado en el equipo
if exist "C:\Users\USUARIO\.jdks\jbr-25.0.4.1\bin\java.exe" (
    set "JAVA_HOME=C:\Users\USUARIO\.jdks\jbr-25.0.4.1"
) else if exist "C:\Users\USUARIO\Desktop\IntelliJ IDEA 2026.2.0.1\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Users\USUARIO\Desktop\IntelliJ IDEA 2026.2.0.1\jbr"
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Ejecutar aplicacion con Maven y JavaFX
call "C:\Users\USUARIO\Desktop\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" compile javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocurrio un error al ejecutar la aplicacion.
    pause
)
