@REM
@REM Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
@REM
@REM All rights reserved. Licensed under the OSI BSD License.
@REM
@REM http://www.opensource.org/licenses/bsd-license.php
@REM

@ECHO OFF

REM
REM custom windows hudson build to run vcvarsall before maven
REM

REM
REM must provide maven home via
REM http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
REM

REM #####################

ECHO ### PWD=%CD%

SET MSVC_HOME=C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\
ECHO ### MSVC_HOME=%MSVC_HOME%

IF [%jdk%]==[java32] call :do_msvc_32

IF [%jdk%]==[java64] call :do_msvc_64

"%APACHE_MAVEN_3_0_1_HOME%\bin\mvn" clean deploy --activate-profiles nar --show-version --update-snapshots

goto :EOF

REM #####################

:do_msvc_32
ECHO ### msvc for java32
call "%MSVC_HOME%\vcvarsall" x86
goto :EOF

:do_msvc_64
ECHO ### msvc for java64
call "%MSVC_HOME%\vcvarsall" amd64
goto :EOF
