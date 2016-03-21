@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call mvn clean install -P run-console %PARAMS%

cd "%WD%"

PAUSE
