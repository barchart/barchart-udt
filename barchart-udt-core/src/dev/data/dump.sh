#!/bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


echo "verstions:"

FILE="dump-with-optimizations.txt"

cat $FILE | grep 'GLIB' | \
sed 's/^.*\(GLIB[A-Z]*_[0-9\.]*\).*$/\1/' | \
sort -u
