#!/bin/bash
#
# Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#
# Work around for
# http://mail-archives.apache.org/mod_mbox/maven-users/201111.mbox/%3C6511D504B746914E826A26F2FC2AE6B807689E0C@MADRID.LANGERDISPLAY.CORP%3E
#

echo "##################################"

echo "WORKSPACE=$WORKSPACE"

echo "### push commits"
git push

echo "### push tags"
git tag | grep barchart-udt | xargs git push origin tag

echo "##################################"
