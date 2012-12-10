#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


#
# must provide maven home via
# http://wiki.hudson-ci.org/display/HUDSON/Tool+Environment+Plugin
#

echo "### home  = $HOME"
echo "### pwd   = $PWD"

echo "### label = $label"
echo "### jdk   = $jdk"

echo "### MAVEN_OPTS=$MAVEN_OPTS"

"$APACHE_MAVEN_3_HOME/bin/mvn" $MVN_CMD_UDT


