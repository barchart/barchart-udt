#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

echo "##################################"

echo "WORKSPACE=$WORKSPACE"

echo "### push changes"
git push

echo "### push tags"
git tag | grep barchart-udt | xargs git push origin tag

echo "##################################"
