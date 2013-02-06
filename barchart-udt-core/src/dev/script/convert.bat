@REM
@REM Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
@REM
@REM All rights reserved. Licensed under the OSI BSD License.
@REM
@REM http://www.opensource.org/licenses/bsd-license.php
@REM

pexports.exe -o udt.DLL > udt.def
dlltool -d udt.def --dllname udt.dll --output-lib libudt.a
