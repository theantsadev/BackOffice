@echo off
REM Script de build et déploiement pour Windows

echo ==================================
echo   Build Hotel BackOffice
echo ==================================
echo.

REM Vérifier que Maven est installé
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Erreur: Maven n'est pas installe ou pas dans le PATH
    exit /b 1
)

REM Nettoyer et compiler le projet
echo 1. Nettoyage des fichiers de build...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors du nettoyage
    exit /b 1
)

echo.
echo 2. Compilation du projet...
call mvn package
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la compilation
    exit /b 1
)

echo.
echo ==================================
echo   Build reussi!
echo ==================================
echo.
echo Le fichier WAR a ete genere: target\backoffice.war
echo.

REM Deploiement automatique vers Tomcat
set TOMCAT_HOME=E:\Etude\Etude\Outils\TOMCAT\apache-tomcat-10.1.28
set WEBAPPS_DIR=%TOMCAT_HOME%\webapps

echo 3. Deploiement vers Tomcat...
if not exist "%WEBAPPS_DIR%" (
    echo Erreur: Le dossier webapps de Tomcat n'existe pas: %WEBAPPS_DIR%
    echo Veuillez verifier le chemin de Tomcat
    pause
    exit /b 1
)

REM Supprimer l'ancien deploiement si existe
if exist "%WEBAPPS_DIR%\backoffice.war" (
    echo Suppression de l'ancien WAR...
    del /Q "%WEBAPPS_DIR%\backoffice.war"
)
if exist "%WEBAPPS_DIR%\backoffice\" (
    echo Suppression de l'ancien dossier deploye...
    rmdir /S /Q "%WEBAPPS_DIR%\backoffice"
)

REM Copier le nouveau WAR
echo Copie du WAR vers Tomcat...
copy /Y "target\backoffice.war" "%WEBAPPS_DIR%\"
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la copie du WAR
    pause
    exit /b 1
)

echo.
echo ==================================
echo   Deploiement reussi!
echo ==================================
echo.
echo Le WAR a ete deploye dans: %WEBAPPS_DIR%
echo.
echo Pour demarrer l'application:
echo   1. Demarrez Tomcat: %TOMCAT_HOME%\bin\startup.bat
echo   2. Accedez a http://localhost:8080/backoffice/pages/
echo.
echo Voulez-vous demarrer Tomcat maintenant? (O/N)
set /p START_TOMCAT=
if /i "%START_TOMCAT%"=="O" (
    echo Demarrage de Tomcat...
    start "" "%TOMCAT_HOME%\bin\startup.bat"
    timeout /t 3 >nul
    echo Tomcat demarre! Attendez quelques secondes pour le deploiement...
    echo Puis ouvrez: http://localhost:8080/backoffice/pages/
)
echo.
pause
