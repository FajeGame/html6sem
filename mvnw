#!/usr/bin/env sh
set -e

MVNW_REPOURL=https://repo.maven.apache.org/maven2
WRAPPER_LAUNCHER=org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
WRAPPER_URL="$MVNW_REPOURL/$WRAPPER_LAUNCHER"

MAVEN_PROJECTBASEDIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  if [ -f "$WRAPPER_PROPERTIES" ]; then
    WRAPPER_URL=$(grep -E "^wrapperUrl=" "$WRAPPER_PROPERTIES" | cut -d= -f2-)
  fi
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "curl/wget not found to download Maven Wrapper."
    exit 1
  fi
fi

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME is not set."
  exit 1
fi

exec "$JAVA_HOME/bin/java" -jar "$WRAPPER_JAR" "$@"
