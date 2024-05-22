@echo off

set "lib=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\lib"
set "SRC=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\src"
set "JAR=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\jar"
set "CLASSPATH=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Test\lib"
set "jarName=controller" 

if not exist "%JAR%" mkdir "%JAR%"
cd "%SRC%"
javac -cp "%lib%\*" -d "%JAR%/" *.java

cd "%JAR%"


jar -cvf "%jarName%.jar" .


copy   "%jarName%.jar" "%CLASSPATH%\"
cd "%SRC%"
rmdir /s /q "%JAR%"
echo Jar créé avec succès.

pause
