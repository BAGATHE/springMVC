@echo off

set "lib=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Framework\lib"
set "SRC=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Framework\src"
set "JAR=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Framework\jar"
set "libParanamer=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Framework\lib\paranamer-2.8.jar"
set "libGson=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Framework\lib\gson-2.8.2.jar"
set "CLASSPATH=E:\Licence_3\Semestre-5\M.Niaina\SpringMVC\Test\lib"
set "jarName=sprint9-2658" 

if not exist "%JAR%" mkdir "%JAR%"
cd "%SRC%"
javac -parameters -cp "%lib%\*" -d "%JAR%/" *.java

cd "%JAR%"


jar -cvf "%jarName%.jar" .


copy   "%jarName%.jar" "%CLASSPATH%\"
copy   "%libParanamer%" "%CLASSPATH%\"
copy   "%libGson%" "%CLASSPATH%\"
cd "%SRC%"
rmdir /s /q "%JAR%"
echo Jar créé avec succès.

pause
