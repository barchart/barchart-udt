/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainCrashJVM {

	private static Logger log = LoggerFactory.getLogger(MainCrashJVM.class);

	public static void main(String[] args) {

		log.info("started; trying to crash jvm");

		try {

			TypeUDT type = TypeUDT.STREAM;

			SocketUDT socket = new SocketUDT(type);

			// this will kill the jvm
			socket.testCrashJVM0();

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}

}