@echo off
cd /d "%~dp0"
echo Compilando...
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
if errorlevel 1 (
  echo.
  echo HUBO UN ERROR AL COMPILAR. Tome una captura de esta ventana.
  pause
  exit /b
)
echo Abriendo el programa...
java -cp "out;lib/*" AppGUI
pause
