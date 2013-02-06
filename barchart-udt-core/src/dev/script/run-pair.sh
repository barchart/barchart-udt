#! /bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

echo "sh-start"
#export JAVA_HOME="/usr/lib/jvm/java-6-sun-1.6.0.14"
echo JAVA_HOME="$JAVA_HOME"

#export PATH=$PATH:$JAVA_HOME/jre/bin
echo PATH="$PATH"

# export LD_PRELOAD=$JAVA_HOME/jre/lib/amd64/libjsig.so 

mvn dependency:copy-dependencies

# shared
export PWD=`pwd`
export PORT="9999"

sleep 1

export SERVER_BIND="localhost"
./run-server.sh &

sleep 1

export CLIENT_BIND="localhost"
export CLIENT_REMOTE="localhost"
./run-client.sh &

sleep 1

echo "sh-finish"
