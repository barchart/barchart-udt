#!/bin/sh
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


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

log "PWD=$PWD"

KIND=$1

LIB_NAME="SocketUDT"
LIB_FOLDER="$PWD/../target/classes"

ARTIFACT=""
ARTIFACT_MAP=""

LIB_FILE=""
LIB_EXTENSION=""

OS=`uname -s`
OS=`toLower $OS`
ARCH=`uname -m`
ARCH=`toLower $ARCH`

makeExtension(){
	case $OS in
	linux)
		LIB_EXTENSION="so"
	;;
	cygwin* | mingw* )
		LIB_EXTENSION="dll"
	;;
	darwin )
		LIB_EXTENSION="jnilib"
	;;
	*)
		log "error: unsupported OS=$OS"
		exit 1
	;;
	esac
	log "detected LIB_EXTENSION=$LIB_EXTENSION"
}
findArtifact(){
	COUNT=`ls -1 *.$LIB_EXTENSION | wc -l`
	if [ $COUNT = 1 ] ; then
		ARTIFACT=`ls -1 *.$LIB_EXTENSION`
	else
		log "error; can not find artifact; COUNT=$COUNT"
		exit 1
	fi
}
findArtifactMap(){
	COUNT=`ls -1 *.map | wc -l`
	if [ $COUNT = 1 ] ; then
		ARTIFACT_MAP=`ls -1 *.map`
	else
		log "error; can not find artifact map; COUNT=$COUNT"
		exit 1
	fi
}
makeLibraryName() {
	case $OS in
			linux)
			case $ARCH in
				i*86)
					LIB_FILE="lib$LIB_NAME-linux-x86-32.so"
				;;
				amd64 | x86_64)
					LIB_FILE="lib$LIB_NAME-linux-x86-64.so"
				;;
				*)
					log "error; not supported ARCH=$ARCH"
					exit 1
				;;
			esac
		;;
		cygwin* | mingw* )
			case $ARCH in
				i*86)
					LIB_FILE="$LIB_NAME-windows-x86-32.dll"
				;;
				amd64 | x86_64)
					LIB_FILE="$LIB_NAME-windows-x86-64.dll"
				;;
				*)
					log "error; not supported ARCH=$ARCH"
					exit 1
				;;
			esac
		;;
		darwin )
			case $ARCH in
				i*86)
					LIB_FILE="lib$LIB_NAME-macosx-x86-32.jnilib"
				;;
				amd64 | x86_64)
					LIB_FILE="lib$LIB_NAME-macosx-x86-64.jnilib"
				;;
				*)
					log "error; not supported ARCH=$ARCH"
					exit 1
				;;
			esac
		;;
		*)
			log "error: not supported OS=$OS"
			exit 1
		;;
	esac
}
checkSupported(){
	case $OS in
		linux | cygwin* | mingw* | darwin )
			log "detected OS=$OS"
		;;
		*)
			log "error: unsupported OS=$OS"
			exit 1
		;;
	esac
	case $ARCH in
		i*86 | amd64 | x86_64 )
			log "detected ARCH=$ARCH"
		;;
		*)
			log "error: unsupported ARCH=$ARCH"
			exit 1
		;;
	esac
}

#####################################################

checkSupported

makeExtension

case $KIND in
	start)
		log "pre-build task;"
		log "done"
	;;
	finish)
		#
		log "post-build task;"
		findArtifact
		findArtifactMap
		makeLibraryName
		#
		MAP_FILE="$LIB_FILE.map"
		SYM_FILE="$LIB_FILE.sym"
		#
		TARGET="$LIB_FOLDER/$LIB_FILE"
		TARGET_MAP="$LIB_FOLDER/$MAP_FILE"
		TARGET_SYM="$LIB_FOLDER/$SYM_FILE"
		#
		log "ARTIFACT     : $ARTIFACT"
		log "ARTIFACT_MAP : $ARTIFACT_MAP"
		log "TARGET     : $TARGET"
		log "TARGET_MAP : $TARGET_MAP"
		log "TARGET_SYM : $TARGET_SYM"
		#
		cp -f -v "$ARTIFACT" "$TARGET"
		cp -f -v "$ARTIFACT_MAP" "$TARGET_MAP"

		# TODO: find macosx alternatives
		# nm --demangle --numeric-sort "$ARTIFACT" > "$TARGET_SYM"
		# ldd -r "$TARGET"
		#
		ls -l "$LIB_FOLDER"
		log "done"
	;;
	*)
		log "error; unecpected KIND=$KIND"
		exit 1
	;;
esac

exit 0

#####################################################

