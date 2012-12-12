#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


if [ "$THIS_PATH" == "" ] ; then
	echo "fatal: THIS_PATH must be set"
	exit 1
fi

# name of this script
THIS_FILE="$(basename $0)"


# log destination
THIS_LOG="$THIS_PATH/$THIS_FILE.log"

function log {
	local MESSAGE="$1"
	echo "### $MESSAGE"
}

log "PWD=$PWD"
log "PATH=$PATH"
log "THIS_PATH=$THIS_PATH"
log "THIS_FILE=$THIS_FILE"


function verify_root_user {

	if [ "$(id -u)" != "0" ]; then
	   echo "fatal: script must be run as root"
	   exit 1
	fi

}

function verify_tool_present {

	local TOOL="$1"

	which "$TOOL" > /dev/null
	if [ "$?" != "0" ]; then
		log "fatal: tool $TOOL must be installed"
		exit 1
	else
		log "found tool: $TOOL"
	fi

}

function verify_run_status {

	local STATUS="$1"
	local COMMENT="$2"

	if [ "$STATUS" != "0" ]; then
		log "failure: $COMMENT status=$STATUS"
		exit 1
	else
		log "success: $COMMENT"
	fi

}

VMWARE_HOME="/var/vmware"

