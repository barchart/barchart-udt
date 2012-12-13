#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#
# location of jenkins slave images on vmware host 
#
VMWARE_HOME="/var/vmware"

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


#
# node state values
#
NODE_LIVE="1"
NODE_DEAD="0"

#
# node state lookup
# https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API
# https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project
#
function jenkins_node_live {
	local NAME="$1"
	local USER="$TRIG_USER:$TRIG_PASS"
	local PAGE="$JENKINS_URL/computer/$NAME/api/json?pretty=true"
	local LIVE=$(curl --insecure --silent --user $USER $PAGE | grep '"offline"' | grep 'false' | wc -l)
	echo $LIVE
}

#
# absolute path to jenkins-specific vmware image
#
function vmware_jenkins_image {
	local NAME="$1"
	case $NAME in
		"linux" )
			echo "$VMWARE_HOME/jenkins-ubuntu/jenkins-ubuntu.vmx"
			;;
		"macosx" )
			echo "$VMWARE_HOME/jenkins-macosx/Mac_OS_X_10.6.X.vmx"
			;;
		"windows" )
			echo "$VMWARE_HOME/jenkins-windows/jenkins-windows.vmx"
			;;
				"*" )
			log "invalid VM_ACTION=$VM_ACTION"
			exit 1
			;;
	esac
}

#
# ensure jenkins node status
#
function jenkins_node_wait {
	local NAME="$1"
	local WAIT="$2"
	while [ true ] ; do
		local LIVE=$(jenkins_node_live $NAME)
		log "NAME=$NAME WAIT=$WAIT LIVE=$LIVE"
		if [ "$LIVE" == "$WAIT" ] ; then
			break
		else
			sleep 3s
		fi
	done
}

#
# expected node status based on action
#
function jenkins_expected { 
	local VM_ACTION="$1"
	case $VM_ACTION in
		"start" )
			echo "$NODE_LIVE"
			;;
		"stop" )
			echo "$NODE_DEAD"
			;;
		"*" )
			log "invalid VM_ACTION=$VM_ACTION"
			exit 1
			;;
	esac
}
