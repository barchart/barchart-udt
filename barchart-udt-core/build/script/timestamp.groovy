/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
// set current time property in the running maven project context 

import java.util.Date;
import java.text.MessageFormat;

def date = new Date();
def	buildtime = date.getTime();
def	timestamp = MessageFormat.format("{0,date,yyyy-MM-dd_HH-mm-ss}", date);

project.properties['buildtime'] = Long.toString(buildtime);
project.properties['timestamp'] = timestamp;
