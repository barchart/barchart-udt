#!/bin/sh
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


#
export CYGWIN=nodosfilewarning

# used by eclipse cdt interactive builder

SCRIPT=`basename $0`

log() {
	echo "$SCRIPT: $1"
}

toLower() {
  echo $1 | tr "[:upper:]" "[:lower:]"
}

toUpper() {
  echo $1 | tr "[:lower:]" "[:upper:]"
}

log "########################################"

# "start" or "stop"
KIND=$1

# java code expects this to match maven artifactId
LIB_NAME="barchart-udt4"

# cdt build is used by java test code only
LIB_FOLDER="$PWD/../target/test-classes"

log "current folder : $PWD"

case $KIND in
	start)
		log "pre-build task;"
		log "done"
	;;
	finish)
		log "post-build task;"
		cp -f -v *$LIB_NAME* "$LIB_FOLDER"
		log "done"
	;;
	*)
		log "error; unecpected KIND=$KIND"
		exit 1
	;;
esac

log "########################################"

