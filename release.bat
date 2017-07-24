@ECHO OFF

SET WD=%CD%
SET SD=%~dp0
SET PARAMS=%*

cd "%SD%"

call mvnw release:clean release:prepare -Prelease %PARAMS%
call mvnw release:perform -Prelease %PARAMS%

cd "%WD%"

PAUSE
