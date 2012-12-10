@REM
@REM Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
@REM
@REM All rights reserved. Licensed under the OSI BSD License.
@REM
@REM http://www.opensource.org/licenses/bsd-license.php
@REM

@ECHO OFF

REM #####################

REM
REM custom windows hudson build to set mingw path first
REM

REM
REM must provide maven home via
REM http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
REM

REM #####################

ECHO ### pwd   = %CD%
ECHO ### label = %label%
ECHO ### jdk   = %jdk%

IF [%jdk%]==[java32] call :do_mingw_32

IF [%jdk%]==[java64] call :do_mingw_64

ECHO ### MINGW_HOME=%MINGW_HOME%

SET PATH=%MINGW_HOME%;%PATH%
ECHO ### PATH=%PATH%

"%APACHE_MAVEN_3_HOME%\bin\mvn" %MVN_CMD_UDT%

goto :EOF

REM #####################

:do_mingw_32
ECHO ### mingw for java32
SET MINGW_HOME=c:\mingw32\bin
goto :EOF

:do_mingw_64
ECHO ### mingw for java64
SET MINGW_HOME=c:\mingw64\bin
goto :EOF

REM #####################

