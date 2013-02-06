@REM
@REM Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
@REM
@REM All rights reserved. Licensed under the OSI BSD License.
@REM
@REM http://www.opensource.org/licenses/bsd-license.php
@REM

echo %CMD_PATH%

assoc .SH=bashscript
assoc .SH

ftype bashscript="%CYGWIN_HOME%\bin\bash.exe" "%%1"
ftype bashscript

pause

%CMD_PATH%associate-cygwin.bat-test.sh

pause
