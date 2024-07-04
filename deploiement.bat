@echo off

set "lib=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\lib"
set "SRC=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\src"
set "JAR=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\jar"
set "libParanamer=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Framework\lib\paranamer-2.8.jar"
set "CLASSPATH=D:\itu\LICENCE-2\Semestre4\MrNiaina\springMVC\Test\lib"
set "jarName=sprint8-2658" 

if not exist "%JAR%" mkdir "%JAR%"
cd "%SRC%"
javac -parameters -cp "%lib%\*" -d "%JAR%/" *.java

cd "%JAR%"


jar -cvf "%jarName%.jar" .


copy   "%jarName%.jar" "%CLASSPATH%\"
copy   "%libParanamer%" "%CLASSPATH%\"
cd "%SRC%"
rmdir /s /q "%JAR%"
echo Jar créé avec succès.

pause
