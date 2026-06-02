$ErrorActionPreference = "Stop"

$jdk21 = "C:\Users\15447\.jdks\temurin-21.0.11"

if (-not (Test-Path "$jdk21\bin\java.exe")) {
    throw "JDK 21 not found at $jdk21"
}

$env:JAVA_HOME = $jdk21
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
& java -version
& mvn -pl demo1-sse -am -DskipTests compile
