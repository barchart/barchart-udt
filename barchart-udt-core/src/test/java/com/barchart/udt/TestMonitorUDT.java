/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static util.UnitHelp.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

public class TestMonitorUDT extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMonitor() throws Exception {

		final SocketUDT serverSocket = new SocketUDT(TypeUDT.DATAGRAM);
		final InetSocketAddress serverAddress = localSocketAddress();
		serverSocket.bind(serverAddress);
		serverSocket.listen(1);

		final SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);
		final InetSocketAddress clientAddress = localSocketAddress();
		clientSocket.bind(clientAddress);

		clientSocket.connect(serverAddress);

		final SocketUDT acceptSocket = serverSocket.accept();

		log.info("client montitor={}", clientSocket.toStringMonitor());
		log.info("accept montitor={}", acceptSocket.toStringMonitor());

	}

}
