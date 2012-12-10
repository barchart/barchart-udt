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

public class MainEpoll {

	private static Logger log = LoggerFactory.getLogger(MainEpoll.class);

	public static void main(String[] args) throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		socket.testEpoll0();

		log.info("finished");

	}

}
