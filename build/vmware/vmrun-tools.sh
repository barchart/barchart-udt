#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


###############

THIS_PATH="$(dirname $(readlink -f -n $0))"

source "$THIS_PATH/common.sh"

###############

log "verify tools"
verify_tool_present "vmware"
verify_tool_present "vmrun"

#
# vmware image name
#
VM_NAME="$1"
#
# image operation start or stop
#
VM_ACTION="$2"

#

VM_PATH=$(vmware_jenkins_image $VM_NAME)
VM_USER="root"
VM_PASS="root"

log "VM_PATH=$VM_PATH"

###

VMRUN_LIST="vmrun -T ws list"

VMRUN_START="vmrun -T ws start "$VM_PATH" nogui"
VMRUN_STOP="vmrun -T ws stop "$VM_PATH" soft"

VMRUN_VAR_IN="vmrun -T ws -gu $VM_USER -gp $VM_PASS readVariable $VM_PATH guestEnv"
VMRUN_VAR_OUT="vmrun -T ws -gu $VM_USER -gp $VM_PASS writeVariable $VM_PATH guestEnv"

VMRUN_EXISTS="vmrun -T ws -gu $VM_USER -gp $VM_PASS fileExistsInGuest $VM_PATH"

VMRUN_SCRIPT="vmrun -T ws -gu $VM_USER -gp $VM_PASS runScriptInGuest $VM_PATH"
VMRUN_PROGRAM="vmrun -T ws -gu $VM_USER -gp $VM_PASS runProgramInGuest $VM_PATH"

VMRUN_COPY_HOST_GUEST="vmrun -T ws -gu $VM_USER -gp $VM_PASS copyFileFromHostToGuest $VM_PATH"
VMRUN_COPY_GUEST_HOST="vmrun -T ws -gu $VM_USER -gp $VM_PASS copyFileFromGuestToHost $VM_PATH"

###

log "host PWD=$PWD"

log "guest list before"
$VMRUN_LIST

case $VM_ACTION in
	start)
		log "guest start"
		$VMRUN_START
		verify_run_status "$?" "vm start"
		;;
	stop)
		log "guest stop"
		$VMRUN_STOP
		verify_run_status "$?" "vm stop"
		;;
	*)
		log "invalid VM_ACTION=$VM_ACTION"
		exit 1
		;;
esac

log "guest list after"
$VMRUN_LIST

###

exit 0
