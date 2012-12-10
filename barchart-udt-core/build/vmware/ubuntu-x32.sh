#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


###############

THIS_PATH="$(dirname $(readlink -f $0))"

###############

#
source "$THIS_PATH/common.sh"

###

# run on guest:
# apt-get update
# apt-get upgrade
# apt-get install mc unzip subversion g++

verify_tool_present "vmware"
verify_tool_present "vmrun"

VM="$HOME/.vmware/ubuntu-x32/ubuntu-x32.vmx"
USER="root"
PASS="root"

log "VM=$VM"

SVN_URL="https://barchart-udt.googlecode.com/svn/trunk/barchart-udt4"

HOME_GUEST="/root"

JDK_DIR="jdk1.6.0_21"
JDK_BIN="jdk-6u21-linux-i586.bin"
JDK_BIN_HOST="/$HOME/downloads/$JDK_BIN"
JDK_BIN_GUEST="$HOME_GUEST/$JDK_BIN"

MVN_DIR="apache-maven-2.2.1"
MVN_BIN="apache-maven-2.2.1-bin.zip"
MVN_BIN_HOST="/$HOME/downloads/$MVN_BIN"
MVN_BIN_GUEST="$HOME_GUEST/$MVN_BIN"

###

VMRUN_START="vmrun -T ws start "$VM" nogui"
VMRUN_STOP="vmrun -T ws stop "$VM" soft"
#
VMRUN_VAR_IN="vmrun -T ws -gu $USER -gp $PASS readVariable $VM guestEnv"
VMRUN_VAR_OUT="vmrun -T ws -gu $USER -gp $PASS writeVariable $VM guestEnv"
VMRUN_EXISTS="vmrun -T ws -gu $USER -gp $PASS fileExistsInGuest $VM"
VMRUN_SCRIPT="vmrun -T ws -gu $USER -gp $PASS runScriptInGuest $VM"
VMRUN_PROGRAM="vmrun -T ws -gu $USER -gp $PASS runProgramInGuest $VM"
VMRUN_COPY_HOST_GUEST="vmrun -T ws -gu $USER -gp $PASS copyFileFromHostToGuest $VM"
VMRUN_COPY_GUEST_HOST="vmrun -T ws -gu $USER -gp $PASS copyFileFromGuestToHost $VM"

###

$VMRUN_START
verify_run_status "$?" "vm start"

###

$VMRUN_VAR_OUT "JAVA_HOME" "$HOME_GUEST/$JDK_DIR"
JAVA_HOME_GUEST=$($VMRUN_VAR_IN "JAVA_HOME")
log "JAVA_HOME_GUEST=$JAVA_HOME_GUEST"

$VMRUN_VAR_OUT "M2_HOME" "$HOME_GUEST/$MVN_DIR"
M2_HOME_GUEST=$($VMRUN_VAR_IN "M2_HOME")
log "M2_HOME_GUEST=$M2_HOME_GUEST"

###

$VMRUN_EXISTS "$JDK_BIN_GUEST"
if [ "$?" == "0" ]; then
	log "java found"
	$VMRUN_PROGRAM "$HOME_GUEST/$JDK_DIR/bin/java" "-version"
	verify_run_status "$?" "java test run"
else
	#
	$VMRUN_COPY_HOST_GUEST "$JDK_BIN_HOST" "$JDK_BIN_GUEST"
	verify_run_status "$?" "jdk copy"
	#
	$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST; ./$JDK_BIN"
	verify_run_status "$?" "jdk install"
	#
	log "java installed"
fi

###

$VMRUN_EXISTS "$MVN_BIN_GUEST"
if [ "$?" == "0" ]; then
	log "maven found"
	$VMRUN_PROGRAM "$HOME_GUEST/$MVN_DIR/bin/mvn" "-version"
	verify_run_status "$?" "maven test run"
else
	#
	$VMRUN_COPY_HOST_GUEST "$MVN_BIN_HOST" "$MVN_BIN_GUEST"
	verify_run_status "$?" "maven copy"
	#
	$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST; unzip $MVN_BIN"
	verify_run_status "$?" "maven install"
	#
	log "maven installed"
fi

###

# add java and maven commands to the guest path
PATH_GUEST=$($VMRUN_VAR_IN "PATH")
$VMRUN_VAR_OUT "PATH" "$JAVA_HOME_GUEST/bin:$M2_HOME_GUEST/bin:$PATH_GUEST"
PATH_GUEST=$($VMRUN_VAR_IN "PATH")
log "PATH_GUEST=$PATH_GUEST"

# make workspace
$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST; rm -r -f workspace; mkdir workspace"

# checkout source
$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST/workspace; svn checkout $SVN_URL $HOME_GUEST/workspace > svn.log"
STATUS="$?"

$VMRUN_COPY_GUEST_HOST "$HOME_GUEST/workspace/svn.log" "$HOME/svn.log"
cat "$HOME/svn.log"

verify_run_status "$STATUS" "svn checkout"


# run maven build
$VMRUN_SCRIPT "/bin/bash" "cd $HOME_GUEST/workspace; mvn -DskipTests=true -B -U compile > mvn.log"
STATUS="$?"

$VMRUN_COPY_GUEST_HOST "$HOME_GUEST/workspace/mvn.log" "$HOME/mvn.log"
cat "$HOME/mvn.log"

verify_run_status "$STATUS" "maven run"


###

$VMRUN_STOP
verify_run_status "$?" "vm stop"

###

exit 0
