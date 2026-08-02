@echo off
cd /d "%~dp0"

if not exist lib\*.jar (
    echoScaricando le dipendenze...
    if not exist lib mkdir lib
    cd lib
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/reflections/reflections/0.10.2/reflections-0.10.2.jar' -OutFile 'reflections-0.10.2.jar' }"
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar' -OutFile 'javassist-3.28.0-GA.jar' }"
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar' -OutFile 'guava-31.1-jre.jar' }"
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar' -OutFile 'slf4j-api-2.0.13.jar' }"
    powershell -Command "& { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar' -OutFile 'slf4j-simple-2.0.13.jar' }"
    cd ..
    echo Dipendenze scaricate.
)

echo Compilo...
if not exist bin mkdir bin
javac -d bin -cp "lib/*" src\hack_n_slash\Main.java src\hack_n_slash\bots\Bot.java src\hack_n_slash\bots\SimpleBerserkerBot.java src\hack_n_slash\bots\SimpleArcherBot.java src\hack_n_slash\engines\Engine.java src\hack_n_slash\engines\Engine1stEdition.java src\hack_n_slash\graphics\GameView.java src\hack_n_slash\graphics\GameState.java src\hack_n_slash\graphics\WebView.java src\hack_n_slash\map\MatrixLogic.java src\hack_n_slash\miscellaneous\Action.java src\hack_n_slash\miscellaneous\Coord.java

echo Avvio il gioco.
echo ------------------------------------------------
java -cp "bin;lib/*" hack_n_slash.Main