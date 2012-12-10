@REM
@REM =================================================================================
@REM
@REM BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
@REM
@REM ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_16-24-35'
@REM
@REM Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
@REM
@REM All rights reserved.
@REM
@REM Redistribution and use in source and binary forms, with or without modification,
@REM are permitted provided that the following conditions are met:
@REM
@REM     * Redistributions of source code must retain the above copyright notice,
@REM     this list of conditions and the following disclaimer.
@REM
@REM     * Redistributions in binary form must reproduce the above copyright notice,
@REM     this list of conditions and the following disclaimer in the documentation
@REM     and/or other materials provided with the distribution.
@REM
@REM     * Neither the name of the Barchart, Inc. nor the names of its contributors
@REM     may be used to endorse or promote products derived from this software
@REM     without specific prior written permission.
@REM
@REM THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
@REM AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
@REM WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
@REM IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
@REM INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
@REM BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
@REM OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
@REM WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
@REM IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@REM
@REM Developers: Andrei Pozolotin;
@REM
@REM =================================================================================
@REM
@REM
@REM =================================================================================
@REM
@REM BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
@REM
@REM ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_16-24-04'
@REM
@REM Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
@REM
@REM All rights reserved.
@REM
@REM Redistribution and use in source and binary forms, with or without modification,
@REM are permitted provided that the following conditions are met:
@REM
@REM     * Redistributions of source code must retain the above copyright notice,
@REM     this list of conditions and the following disclaimer.
@REM
@REM     * Redistributions in binary form must reproduce the above copyright notice,
@REM     this list of conditions and the following disclaimer in the documentation
@REM     and/or other materials provided with the distribution.
@REM
@REM     * Neither the name of the Barchart, Inc. nor the names of its contributors
@REM     may be used to endorse or promote products derived from this software
@REM     without specific prior written permission.
@REM
@REM THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
@REM AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
@REM WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
@REM IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
@REM INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
@REM BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
@REM OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
@REM WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
@REM IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@REM
@REM Developers: Andrei Pozolotin;
@REM
@REM =================================================================================
@REM
@REM
@REM =================================================================================
@REM
@REM BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
@REM
@REM ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_16-17-24'
@REM
@REM Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
@REM
@REM All rights reserved.
@REM
@REM Redistribution and use in source and binary forms, with or without modification,
@REM are permitted provided that the following conditions are met:
@REM
@REM     * Redistributions of source code must retain the above copyright notice,
@REM     this list of conditions and the following disclaimer.
@REM
@REM     * Redistributions in binary form must reproduce the above copyright notice,
@REM     this list of conditions and the following disclaimer in the documentation
@REM     and/or other materials provided with the distribution.
@REM
@REM     * Neither the name of the Barchart, Inc. nor the names of its contributors
@REM     may be used to endorse or promote products derived from this software
@REM     without specific prior written permission.
@REM
@REM THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
@REM AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
@REM WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
@REM IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
@REM INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
@REM BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
@REM OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
@REM WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
@REM IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@REM
@REM Developers: Andrei Pozolotin;
@REM
@REM =================================================================================
@REM
set CMD_PATH=%~dp0

echo %CMD_PATH%

assoc .SH=bashscript
assoc .SH

ftype bashscript="%CYGWIN_HOME%\bin\bash.exe" "%%1"
ftype bashscript

pause

%CMD_PATH%associate-cygwin.bat-test.sh

pause
