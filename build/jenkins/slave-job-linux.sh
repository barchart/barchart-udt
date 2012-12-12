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
# https://wiki.jenkins-ci.org/display/JENKINS/Tool+Environment+Plugin
#

cd barchart-udt-core

echo "### home  = $HOME"
echo "### pwd   = $PWD"

echo "### label = $label"
echo "### jdk   = $jdk"

echo "### MAVEN_OPTS=$MAVEN_OPTS"

"$MAVEN_HOME/bin/mvn" clean deploy --update-snapshots --activate-profiles nar,int
