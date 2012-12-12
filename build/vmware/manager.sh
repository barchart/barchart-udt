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

source "$THIS_PATH/common.sh"

###############

# relative path to vmware image
VMX="$1"
# operation type: start or stop
CMD="$2"

scp $THIS_PATH/vmrun-tools.sh $VMWARE_USER@$VMWARE_HOST:$VMWARE_HOME

ssh $VMWARE_USER@$VMWARE_HOST "$VMWARE_HOME/vmrun-tools.sh $VMX $CMD"

