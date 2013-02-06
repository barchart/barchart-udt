#!/bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


BASEDIR="$1"

SELECTED="$2"

echo "PWD  : $PWD"
echo "SELECTED : $SELECTED"


IMAGE_BASE="0x10000000"

SYMBOL_FILE="src/main/resources/SocketUDT-windows-x86-32.dll.sym"

LAST=

cat $SELECTED | grep '^C.*\[SocketUDT.*\].*$' | sed 's/^C.*+\(0x[0-9a-f]*\).*$/\1/' |
while read ADDR; do
	CRASH_ADDR=$(( $IMAGE_BASE + $ADDR))  
	echo $CRASH_ADDR;
	while read SYMB; do
		if [[ $SYMB =~ ^[0-9a-f]*[[:space:]][tT][[:space:]].*$ ]]; then
			SYMB_ADDR=`echo "$SYMB" | sed 's/^\([0-9a-f]*\)[[:space:]][tT][[:space:]].*$/\1/'`
			SYMB_ADDR=$((16#$SYMB_ADDR))
			# echo "SYMB_ADDR : $SYMB_ADDR"
			if (( $CRASH_ADDR < $SYMB_ADDR )); then
				echo " CRASH_ADDR : $CRASH_ADDR; LAST : $LAST;"
				continue 2
			else
				LAST=$SYMB
				continue 1
			fi
			# echo " SYMB_ADDR : $SYMB_ADDR; SYMB : $SYMB;"
		else
			continue 1
		fi
	done < $SYMBOL_FILE  
done
