@echo off

:: Définir les variables
set "lib=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\lib"
set "SRC=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\src"
set "temp_src=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\temp_src"
set "classes=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\classes"
set "CLASSPATH=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Test\lib"
set "libParanamer=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\lib\paranamer-2.8.jar"
set "libGson=E:\Licence_3\semestre5\M.Niaina\SpringMVC\Framework\lib\gson-2.8.2.jar"
set "jarName=sprint16-2658"

:: Créer les répertoires temporaires si nécessaires
if not exist "%temp_src%" mkdir "%temp_src%"
if not exist "%classes%" mkdir "%classes%"

:: Copier les fichiers .java dans temp_src
cd "%SRC%"
for /r %%F in (*.java) do (
    copy "%%F" "%temp_src%" >nul
)
cd ..

:: Compiler les fichiers .java dans temp_src et générer les .class dans classes
javac -parameters -cp "%lib%\*" -d "%classes%" "%temp_src%\*.java"

:: Créer le fichier JAR à partir des .class
cd "%classes%"
jar -cvf "../%jarName%.jar" .
cd ..

:: Copier le fichier JAR et les dépendances dans le CLASSPATH
if not exist "%CLASSPATH%" mkdir "%CLASSPATH%"
copy "%jarName%.jar" "%CLASSPATH%" >nul
copy "%libParanamer%" "%CLASSPATH%" >nul
copy "%libGson%" "%CLASSPATH%" >nul

:: Nettoyer les répertoires temporaires
rmdir /s /q "%temp_src%"
rmdir /s /q "%classes%"

:: Fin
echo Jar créé avec succès et copié dans %CLASSPATH%.
pause
