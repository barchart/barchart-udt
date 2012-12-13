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

log "vmware host propserties"
log "VMWARE_USER=$VMWARE_USER"
log "VMWARE_HOST=$VMWARE_HOST"
log "VMWARE_HOME=$VMWARE_HOME"

log "jenkins master"
log "JENKINS_URL=$JENKINS_URL"

#
# vmware image name
#
VM_NAME="$1"
#
# image operation : start or stop
#
VM_ACTION="$2"

log "upload control script"
scp $THIS_PATH/common.sh      $VMWARE_USER@$VMWARE_HOST:$VMWARE_HOME
scp $THIS_PATH/vmrun-tools.sh $VMWARE_USER@$VMWARE_HOST:$VMWARE_HOME

log "initiate remote action"
ssh $VMWARE_USER@$VMWARE_HOST "$VMWARE_HOME/vmrun-tools.sh $VM_NAME $VM_ACTION"
verify_run_status "$?" "vm action"

log "ensure action completion"
VM_LIVE=$(jenkins_expected $VM_ACTION)
jenkins_node_wait $VM_NAME $VM_LIVE

###############
