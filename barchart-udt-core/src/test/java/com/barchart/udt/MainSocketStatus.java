/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainSocketStatus {

	private static Logger log = LoggerFactory.getLogger(MainSocketStatus.class);

	public static void main(String[] args) throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		socket.testSocketStatus0();

		InetSocketAddress localAddress1 = new InetSocketAddress(//
				"0.0.0.0", 8001);

		socket.bind(localAddress1);
		socket.testSocketStatus0();

		socket.listen(1);
		socket.testSocketStatus0();

		// socket.accept();

		socket.close();
		socket.testSocketStatus0();

		log.info("finished");

	}

}
