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

verify_tool_present "vmware"
verify_tool_present "vmrun"

# relative path to vmware system image
VMX="$1"

VM="$HOME/$VMX"
USER="root"
PASS="root"

log "VM=$VM"

###

VMRUN_START="vmrun -T ws start "$VM" nogui"
VMRUN_STOP="vmrun -T ws stop "$VM" soft"

VMRUN_VAR_IN="vmrun -T ws -gu $USER -gp $PASS readVariable $VM guestEnv"
VMRUN_VAR_OUT="vmrun -T ws -gu $USER -gp $PASS writeVariable $VM guestEnv"
VMRUN_EXISTS="vmrun -T ws -gu $USER -gp $PASS fileExistsInGuest $VM"
VMRUN_SCRIPT="vmrun -T ws -gu $USER -gp $PASS runScriptInGuest $VM"
VMRUN_PROGRAM="vmrun -T ws -gu $USER -gp $PASS runProgramInGuest $VM"
VMRUN_COPY_HOST_GUEST="vmrun -T ws -gu $USER -gp $PASS copyFileFromHostToGuest $VM"
VMRUN_COPY_GUEST_HOST="vmrun -T ws -gu $USER -gp $PASS copyFileFromGuestToHost $VM"

###

$VMRUN_STOP
verify_run_status "$?" "vm stop"

###

exit 0
