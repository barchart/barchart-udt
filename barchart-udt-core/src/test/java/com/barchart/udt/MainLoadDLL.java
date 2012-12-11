/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainLoadDLL {

	private static Logger log = LoggerFactory.getLogger(MainLoadDLL.class);

	/* verify mingwm10.dll is loaded properly */
	public static void main(String[] args) {

		log.info("started");

		String libraryPath = System.getProperty("java.library.path");

		log.info("libraryPath={}", libraryPath);

		try {

			// File mingwDLL = new File("src/main/resources/mingwm10.dll");
			// String absolutePath = mingwDLL.getAbsolutePath();
			// System.load(absolutePath);

			TypeUDT type = TypeUDT.STREAM;

			SocketUDT socket = new SocketUDT(type);

			boolean isOpen = socket.isOpen();

			log.info("isOpen={}", isOpen);

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

		log.info("finished");

	}

}