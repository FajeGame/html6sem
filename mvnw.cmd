@echo off

setlocal
set MAVEN_PROJECTBASEDIR=%~sdp0
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=.
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

if not exist "%WRAPPER_JAR%" (
  if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" (
    mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
  )
  where curl.exe >nul 2>nul
  if errorlevel 1 (
    echo curl.exe not found. Install curl or put maven-wrapper.jar into .mvn\wrapper manually.
    exit /b 1
  )
  curl.exe -f -L -o "%WRAPPER_JAR%" "%WRAPPER_URL%"
  if errorlevel 1 (
    echo Failed to download Maven Wrapper from %WRAPPER_URL%
    exit /b 1
  )
)

set JAVA_EXE=java
if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

"%JAVA_EXE%" -classpath %WRAPPER_JAR% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% org.apache.maven.wrapper.MavenWrapperMain %*
endlocal & exit /b %ERRORLEVEL%
