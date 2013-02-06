#! /bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

echo "sh-start"

echo "working directory: `pwd`"

make info
make all

# ls -las ./target/classes/*.so

echo "sh-finish"
