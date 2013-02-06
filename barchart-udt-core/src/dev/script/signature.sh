#!/bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


CMD_PATH=`dirname $0`

CLASS_PATH="$JAVA_HOME/jre/lib/rt.jar"
CLASS_LIST="java.util.Set java.util.Iterator java.util.List"

# detect Cygwin
#CYGWIN=false;
#case "`uname`" in
#  CYGWIN*) CYGWIN=true;
#esac
#if $CYGWIN
#then
# CLASS_PATH=`cygpath --path --windows $CLASS_PATH`
#fi

# COMMAND="javap -s -private -classpath \"$CLASS_PATH\" $CLASS_LIST > \"$CMD_PATH/signature.txt\""

echo CLASS_PATH : $CLASS_PATH
echo CLASS_LIST : $CLASS_LIST
# echo COMMAND    : "$COMMAND"

# `$COMMAND`
javap -s -private -classpath "$CLASS_PATH" $CLASS_LIST > "$CMD_PATH/signature.txt"
 