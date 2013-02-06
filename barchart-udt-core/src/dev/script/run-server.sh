#! /bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

# java -Djava.library.path=./lib -cp ./target/classes:./target/test-classes net.sourceforge.udt.MainTest
# export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
# export LD_DEBUG_OUTPUT=./logs/debug-server.log
# LD_DEBUG=all java -ea -Xcheck:jni -cp "$CP" $MAIN 
	
echo "SERVER BIND: $SERVER_BIND"
echo "SERVER PORT: $PORT"
echo "SERVER CURRENT DIR: $PWD"
	
# -verbose:jni 
JAVA_OPTS=" -ea -Xcheck:jni -Xms16m -Xmx64m "
MAIN_OPTS=" -Dudt.bind.address=$SERVER_BIND -Dudt.local.port=$PORT -Dudt.count.monitor=30000 "
MAIN_ARGS=""

CLASS_PATH=$PWD/target/dependency/*:$PWD/target/classes:$PWD/target/test-classes
MAIN=net.sourceforge.udt.MainServer

# detect Cygwin
CYGWIN=false;
case "`uname`" in
  CYGWIN*) CYGWIN=true;
esac

if $CYGWIN
then
CLASS_PATH=`cygpath --path --windows $CLASS_PATH`
fi

echo "SERVER CLASS PATH: $CLASS_PATH"

java $JAVA_OPTS $MAIN_OPTS -cp "$CLASS_PATH" $MAIN $MAIN_ARGS
