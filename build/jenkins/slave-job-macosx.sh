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

echo "### pwd   = $PWD"

echo "### label = $label"
echo "### jdk   = $jdk"

case "$jdk" in
	java32)
	    export MAVEN_OPTS="-d32 $MAVEN_OPTS"
	    ;;
	java64)
	    export MAVEN_OPTS="-d64 $MAVEN_OPTS"
	    ;;
	*)
	    echo "invalid jdk"; exit 1
	    ;;
esac

echo "### MAVEN_OPTS=$MAVEN_OPTS"

"$MAVEN_HOME/bin/mvn" clean deploy --update-snapshots --activate-profiles nar,int

