@REM
@REM Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
@REM
@REM All rights reserved. Licensed under the OSI BSD License.
@REM
@REM http://www.opensource.org/licenses/bsd-license.php
@REM

@ECHO OFF

REM
REM http://blog.idm.fr/2009/03/setting-up-a-windows-hudson-slave.html
REM http://support.microsoft.com/kb/137890
REM http://www.microsoft.com/downloads/en/details.aspx?familyid=9d467a69-57ff-4ae7-96ee-b18c4790cffd&displaylang=en
REM

REM sleep 20

cd c:\jenkins\

ECHO start >> slave-agent.log

wget --no-check-certificate "https://jenkins.barchart.com/jnlpJars/slave.jar" -O slave.jar

java -jar slave.jar -slaveLog slave.log -noCertificateCheck -jnlpUrl file:slave-agent.jnlp 1> jenkins.log 2>&1

GOTO :EOF

REM
REM	http://www.paulsadowski.com/wsh/cmdutils.htm
REM

:: Subroutines

:sleep
:: sleep for x number of seconds
ping -n %1 127.0.0.1 > NUL 2>&1
goto :EOF
