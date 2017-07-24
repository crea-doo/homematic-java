@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call mvnw release:rollback %PARAMS%

cd "%WD%"

PAUSE
