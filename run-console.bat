@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call mvnw clean install -P run-console %PARAMS%

cd "%WD%"

PAUSE
