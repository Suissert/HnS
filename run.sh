#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -d lib ] || [ -z "$(ls lib/*.jar 2>/dev/null)" ]; then
  echo "lib/ vuota o mancante. Scarico le dipendenze..."
  mkdir -p lib
  cd lib
  curl -sLO https://repo1.maven.org/maven2/org/reflections/reflections/0.10.2/reflections-0.10.2.jar
  curl -sLO https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar
  curl -sLO https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar
  curl -sLO https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar
  curl -sLO https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar
  cd ..
  echo "Dipendenze scaricate."
fi

echo "Compilo..."
javac -d bin -cp "lib/*" $(find src -name '*.java')

echo "Avvio il gioco."
echo "------------------------------------------------"
java -cp "bin:lib/*" hack_n_slash.Main